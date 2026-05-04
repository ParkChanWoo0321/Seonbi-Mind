package youngju.seonbimind.classic.progress.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import youngju.seonbimind.auth.dto.TodaySentenceResponse;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.auth.service.CurrentMemberService;
import youngju.seonbimind.classic.problem.entity.ProblemSession;
import youngju.seonbimind.classic.problem.repository.ProblemSessionRepository;
import youngju.seonbimind.classic.progress.dto.ProblemHistoryResponse;
import youngju.seonbimind.classic.progress.dto.ProgressResponse;
import youngju.seonbimind.classic.progress.entity.LearningRank;
import youngju.seonbimind.classic.progress.entity.SolvedProblemHistory;
import youngju.seonbimind.classic.progress.entity.UserProgress;
import youngju.seonbimind.classic.progress.repository.SolvedProblemHistoryRepository;
import youngju.seonbimind.classic.progress.repository.UserProgressRepository;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final CurrentMemberService currentMemberService;
    private final UserProgressRepository userProgressRepository;
    private final SolvedProblemHistoryRepository solvedProblemHistoryRepository;
    private final ProblemSessionRepository problemSessionRepository;

    @Transactional
    public void recordCompletedProblem(AuthMember member, ProblemSession session) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        UserProgress progress = userProgressRepository.findByMember(member)
                .orElseGet(() -> userProgressRepository.save(UserProgress.create(member)));

        ensureHistoryIds(member);
        if (session.getHistoryId() == null) {
            session.assignHistoryId(getMaxCompletedHistoryId(member) + 1);
        }
        if (solvedProblemHistoryRepository.existsByMemberAndHistoryId(member, session.getHistoryId())) {
            return;
        }

        progress.recordSolve(today);
        solvedProblemHistoryRepository.save(SolvedProblemHistory.from(session, session.getGptReason()));
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(AuthMember member) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);

        return userProgressRepository.findByMember(member)
                .map(progress -> toResponse(progress.getTotalSolvedCount(), progress.getDisplayCurrentStreak(today)))
                .orElseGet(() -> toResponse(0, 0));
    }

    @Transactional(readOnly = true)
    public TodaySentenceResponse getTodaySentence(AuthMember member) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);
        return problemSessionRepository
                .findFirstByMemberAndCompletedTrueAndCompletedAtBetweenOrderByCompletedAtAsc(
                        member,
                        today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay()
                )
                .map(ProblemSession::getSentence)
                .map(TodaySentenceResponse::from)
                .orElse(null);
    }

    @Transactional
    public List<ProblemHistoryResponse> getMyProblemHistory() {
        AuthMember member = currentMemberService.getCurrentMember();
        ensureHistoryIds(member);
        return solvedProblemHistoryRepository.findByMemberOrderBySolvedAtDesc(member)
                .stream()
                .map(ProblemHistoryResponse::from)
                .toList();
    }

    @Transactional
    public void ensureHistoryIds(AuthMember member) {
        List<SolvedProblemHistory> histories = solvedProblemHistoryRepository.findByMemberOrderBySolvedAtAsc(member);
        Set<Long> usedHistoryIds = new HashSet<>();

        for (SolvedProblemHistory history : histories) {
            Long historyId = history.getHistoryId();
            if (historyId != null && historyId > 0) {
                usedHistoryIds.add(historyId);
            }
        }

        long nextHistoryId = 1;
        for (SolvedProblemHistory history : histories) {
            Long historyId = history.getHistoryId();
            if (historyId != null && historyId > 0) {
                continue;
            }

            while (usedHistoryIds.contains(nextHistoryId)) {
                nextHistoryId++;
            }

            history.assignHistoryId(nextHistoryId);
            usedHistoryIds.add(nextHistoryId);
        }
    }

    @Transactional(readOnly = true)
    public long getMaxCompletedHistoryId(AuthMember member) {
        Long maxHistoryId = solvedProblemHistoryRepository.findMaxHistoryIdByMember(member);
        return maxHistoryId == null ? 0 : maxHistoryId;
    }

    @Transactional(readOnly = true)
    public Optional<SolvedProblemHistory> findCompletedHistory(AuthMember member, Long historyId) {
        return solvedProblemHistoryRepository.findByMemberAndHistoryId(member, historyId);
    }

    private ProgressResponse toResponse(int totalSolvedCount, int currentStreak) {
        LearningRank currentRank = LearningRank.fromTotalSolvedCount(totalSolvedCount);
        LearningRank nextRank = currentRank.next();

        return new ProgressResponse(
                totalSolvedCount,
                currentStreak,
                currentRank.getLabel(),
                nextRank == null ? null : nextRank.getLabel(),
                nextRank == null ? 0 : Math.max(0, nextRank.getThreshold() - totalSolvedCount)
        );
    }

}
