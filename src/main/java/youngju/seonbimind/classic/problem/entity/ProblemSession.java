package youngju.seonbimind.classic.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.sentence.entity.ClassicSentence;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "problem_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uk_problem_session_member_history", columnNames = {"member_id", "history_id"})
)
public class ProblemSession {

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

    @Column(name = "history_id")
    private Long historyId;

    @Column(nullable = false)
    private boolean todaySentence;

    @Lob
    @Column(nullable = false)
    private String shuffledWords;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProblemSessionStage stage;

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

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private ProblemSession(AuthMember member, ClassicSentence sentence, Long historyId, boolean todaySentence, String shuffledWords) {
        this.member = member;
        this.sentence = sentence;
        this.historyId = historyId;
        this.todaySentence = todaySentence;
        this.shuffledWords = shuffledWords;
        this.stage = ProblemSessionStage.ORDER;
        this.correct = false;
        this.completed = false;
    }

    public static ProblemSession start(
            AuthMember member,
            ClassicSentence sentence,
            Long historyId,
            boolean todaySentence,
            String shuffledWords
    ) {
        return new ProblemSession(member, sentence, historyId, todaySentence, shuffledWords);
    }

    public void assignHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public void submitOrderAnswer(String answer, boolean correct) {
        this.userOrderedAnswer = answer;
        if (correct) {
            this.stage = ProblemSessionStage.COPY_TYPING;
        }
    }

    public void submitCopyTypingAnswer(String answer, boolean correct) {
        this.userCopyTypingAnswer = answer;
        if (correct) {
            this.stage = ProblemSessionStage.BLIND_TYPING;
        }
    }

    public void submitBlindTypingAnswer(String answer, boolean gptCorrect, String gptReason) {
        this.userBlindTypingAnswer = answer;
        this.gptCorrect = gptCorrect;
        this.gptReason = gptReason;
        if (gptCorrect) {
            this.correct = true;
            this.completed = true;
            this.stage = ProblemSessionStage.COMPLETED;
            this.completedAt = LocalDateTime.now(SEOUL_ZONE);
        }
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(SEOUL_ZONE);
    }
}
