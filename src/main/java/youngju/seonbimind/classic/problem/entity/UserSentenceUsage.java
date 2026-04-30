package youngju.seonbimind.classic.problem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
        name = "user_sentence_usages",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_sentence_usage", columnNames = {"member_id", "sentence_id"})
)
public class UserSentenceUsage {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime usedAt;

    private UserSentenceUsage(AuthMember member, ClassicSentence sentence) {
        this.member = member;
        this.sentence = sentence;
    }

    public static UserSentenceUsage create(AuthMember member, ClassicSentence sentence) {
        return new UserSentenceUsage(member, sentence);
    }

    @PrePersist
    void prePersist() {
        this.usedAt = LocalDateTime.now(SEOUL_ZONE);
    }
}
