package youngju.seonbimind.classic.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.problem.entity.ProblemSession;
import youngju.seonbimind.classic.sentence.entity.ClassicSentence;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "solved_problem_histories")
public class SolvedProblemHistory {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private AuthMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sentence_id", nullable = false)
    private ClassicSentence sentence;

    @Lob
    @Column(nullable = false)
    private String originalText;

    @Lob
    @Column(nullable = false)
    private String readingText;

    @Lob
    @Column(nullable = false)
    private String meaning;

    @Lob
    @Column(nullable = false)
    private String shuffledWords;

    @Lob
    private String userOrderedAnswer;

    @Lob
    private String userCopyTypingAnswer;

    @Lob
    private String userBlindTypingAnswer;

    private Boolean gptCorrect;

    @Lob
    private String gptReason;

    @Column(nullable = false)
    private boolean correct;

    @Lob
    private String explanation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime solvedAt;

    private SolvedProblemHistory(
            AuthMember member,
            ClassicSentence sentence,
            String originalText,
            String readingText,
            String meaning,
            String shuffledWords,
            String userOrderedAnswer,
            String userCopyTypingAnswer,
            String userBlindTypingAnswer,
            Boolean gptCorrect,
            String gptReason,
            boolean correct,
            String explanation
    ) {
        this.member = member;
        this.sentence = sentence;
        this.originalText = originalText;
        this.readingText = readingText;
        this.meaning = meaning;
        this.shuffledWords = shuffledWords;
        this.userOrderedAnswer = userOrderedAnswer;
        this.userCopyTypingAnswer = userCopyTypingAnswer;
        this.userBlindTypingAnswer = userBlindTypingAnswer;
        this.gptCorrect = gptCorrect;
        this.gptReason = gptReason;
        this.correct = correct;
        this.explanation = explanation;
    }

    public static SolvedProblemHistory from(ProblemSession session, String explanation) {
        ClassicSentence sentence = session.getSentence();
        return new SolvedProblemHistory(
                session.getMember(),
                sentence,
                sentence.getOriginalText(),
                sentence.getReadingText(),
                sentence.getMeaning(),
                session.getShuffledWords(),
                session.getUserOrderedAnswer(),
                session.getUserCopyTypingAnswer(),
                session.getUserBlindTypingAnswer(),
                session.getGptCorrect(),
                session.getGptReason(),
                session.isCorrect(),
                explanation
        );
    }

    @PrePersist
    void prePersist() {
        this.solvedAt = LocalDateTime.now(SEOUL_ZONE);
    }
}
