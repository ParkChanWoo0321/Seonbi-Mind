package youngju.seonbimind.classic.problem.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.auth.service.CurrentMemberService;
import youngju.seonbimind.classic.gpt.dto.MeaningEvaluationResult;
import youngju.seonbimind.classic.gpt.service.GptMeaningEvaluationService;
import youngju.seonbimind.classic.problem.dto.ProblemAnswerResponse;
import youngju.seonbimind.classic.problem.dto.ProblemStartResponse;
import youngju.seonbimind.classic.problem.entity.ProblemSession;
import youngju.seonbimind.classic.problem.entity.ProblemSessionStage;
import youngju.seonbimind.classic.problem.entity.UserSentenceUsage;
import youngju.seonbimind.classic.problem.repository.ProblemSessionRepository;
import youngju.seonbimind.classic.problem.repository.UserSentenceUsageRepository;
import youngju.seonbimind.classic.progress.service.ProgressService;
import youngju.seonbimind.classic.sentence.entity.ClassicSentence;
import youngju.seonbimind.classic.sentence.repository.ClassicSentenceRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final String WORD_SEPARATOR = "\n";

    private final CurrentMemberService currentMemberService;
    private final ClassicSentenceRepository classicSentenceRepository;
    private final ProblemSessionRepository problemSessionRepository;
    private final UserSentenceUsageRepository userSentenceUsageRepository;
    private final GptMeaningEvaluationService gptMeaningEvaluationService;
    private final ProgressService progressService;

    @Transactional
    public ProblemStartResponse startProblem() {
        AuthMember member = currentMemberService.getCurrentMember();
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        boolean hasTodaySentence = problemSessionRepository.existsByMemberAndTodaySentenceTrueAndCreatedAtBetween(
                member,
                startOfToday,
                startOfTomorrow
        );

        Long todaySentenceId = null;
        if (hasTodaySentence) {
            todaySentenceId = problemSessionRepository
                    .findFirstByMemberAndTodaySentenceTrueAndCreatedAtBetweenOrderByCreatedAtAsc(
                            member,
                            startOfToday,
                            startOfTomorrow
                    )
                    .map(session -> session.getSentence().getId())
                    .orElse(null);
        }

        ClassicSentence sentence = pickSentence(member, todaySentenceId);
        List<String> shuffledWords = shuffleMeaningWords(sentence.getMeaning());
        ProblemSession session = problemSessionRepository.save(ProblemSession.start(
                member,
                sentence,
                !hasTodaySentence,
                encodeWords(shuffledWords)
        ));

        return toStartResponse(session, shuffledWords);
    }

    @Transactional
    public ProblemAnswerResponse submitOrderAnswer(Long problemId, String answer) {
        requireNotBlank(answer, "answer");
        ProblemSession session = getMyProblemSession(problemId);
        validateStage(session, ProblemSessionStage.ORDER);

        boolean correct = isSameSentence(session.getSentence().getMeaning(), answer);
        session.submitOrderAnswer(answer, correct);

        return new ProblemAnswerResponse(
                session.getId(),
                correct,
                correct ? ProblemSessionStage.COPY_TYPING.name() : ProblemSessionStage.ORDER.name(),
                false,
                null,
                null
        );
    }

    @Transactional
    public ProblemAnswerResponse submitCopyTypingAnswer(Long problemId, String answer) {
        requireNotBlank(answer, "answer");
        ProblemSession session = getMyProblemSession(problemId);
        validateStage(session, ProblemSessionStage.COPY_TYPING);

        boolean correct = isSameSentence(session.getSentence().getMeaning(), answer);
        session.submitCopyTypingAnswer(answer, correct);

        return new ProblemAnswerResponse(
                session.getId(),
                correct,
                correct ? ProblemSessionStage.BLIND_TYPING.name() : ProblemSessionStage.COPY_TYPING.name(),
                false,
                null,
                null
        );
    }

    @Transactional
    public ProblemAnswerResponse submitBlindTypingAnswer(Long problemId, String answer) {
        requireNotBlank(answer, "answer");
        ProblemSession session = getMyProblemSession(problemId);
        validateStage(session, ProblemSessionStage.BLIND_TYPING);

        MeaningEvaluationResult result = gptMeaningEvaluationService.evaluate(session.getSentence().getMeaning(), answer);
        session.submitBlindTypingAnswer(answer, result.isCorrect(), result.reason());

        if (result.isCorrect()) {
            completeProblem(session);
        }

        return new ProblemAnswerResponse(
                session.getId(),
                result.isCorrect(),
                result.isCorrect() ? ProblemSessionStage.COMPLETED.name() : ProblemSessionStage.BLIND_TYPING.name(),
                result.isCorrect(),
                result.isCorrect(),
                result.reason()
        );
    }

    private ClassicSentence pickSentence(AuthMember member, Long excludedSentenceId) {
        List<ClassicSentence> candidates = excludedSentenceId == null
                ? classicSentenceRepository.findUnsolvedByMember(member)
                : classicSentenceRepository.findUnsolvedByMemberExcluding(member, excludedSentenceId);

        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No available classic sentence.");
        }

        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    private List<String> shuffleMeaningWords(String meaning) {
        String[] splitWords = meaning.trim().split("\\s+");
        List<String> words = new ArrayList<>();
        for (String word : splitWords) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }

        List<String> shuffledWords = new ArrayList<>(words);
        Collections.shuffle(shuffledWords);
        if (shuffledWords.size() > 1 && shuffledWords.equals(words)) {
            Collections.rotate(shuffledWords, 1);
        }
        return shuffledWords;
    }

    private ProblemSession getMyProblemSession(Long problemId) {
        AuthMember member = currentMemberService.getCurrentMember();
        return problemSessionRepository.findByIdAndMember(problemId, member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem session was not found."));
    }

    private void validateStage(ProblemSession session, ProblemSessionStage expectedStage) {
        if (session.isCompleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Problem session is already completed.");
        }
        if (session.getStage() != expectedStage) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Problem session is not in " + expectedStage + " stage.");
        }
    }

    private void completeProblem(ProblemSession session) {
        ClassicSentence sentence = session.getSentence();
        AuthMember member = session.getMember();

        sentence.markUsed();
        if (!userSentenceUsageRepository.existsByMemberAndSentence(member, sentence)) {
            userSentenceUsageRepository.save(UserSentenceUsage.create(member, sentence));
        }
        progressService.recordCompletedProblem(member, session);
    }

    private ProblemStartResponse toStartResponse(ProblemSession session, List<String> shuffledWords) {
        ClassicSentence sentence = session.getSentence();
        return new ProblemStartResponse(
                session.getId(),
                sentence.getId(),
                session.isTodaySentence(),
                sentence.getOriginalText(),
                sentence.getReadingText(),
                sentence.getMeaning(),
                shuffledWords
        );
    }

    private String encodeWords(List<String> words) {
        return String.join(WORD_SEPARATOR, words);
    }

    private boolean isSameSentence(String expected, String actual) {
        return normalize(expected).equals(normalize(actual));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
    }
}
