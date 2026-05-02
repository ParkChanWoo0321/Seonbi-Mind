package youngju.seonbimind.classic.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import youngju.seonbimind.auth.entity.AuthMember;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_progress_member", columnNames = "member_id")
)
public class UserProgress {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private AuthMember member;

    @Column(nullable = false)
    private int totalSolvedCount;

    @Column(nullable = false)
    private int currentStreak;

    private LocalDate lastSolvedDate;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UserProgress(AuthMember member) {
        this.member = member;
        this.totalSolvedCount = 0;
        this.currentStreak = 0;
    }

    public static UserProgress create(AuthMember member) {
        return new UserProgress(member);
    }

    public void recordSolve(LocalDate today) {
        this.totalSolvedCount++;

        if (lastSolvedDate == null) {
            this.currentStreak = 1;
        } else if (lastSolvedDate.isEqual(today)) {
            return;
        } else if (lastSolvedDate.isEqual(today.minusDays(1))) {
            this.currentStreak++;
        } else {
            this.currentStreak = 1;
        }

        this.lastSolvedDate = today;
    }

    public int getDisplayCurrentStreak(LocalDate today) {
        if (lastSolvedDate == null || lastSolvedDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        return currentStreak;
    }

    @PrePersist
    void prePersist() {
        this.updatedAt = LocalDateTime.now(SEOUL_ZONE);
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(SEOUL_ZONE);
    }
}
