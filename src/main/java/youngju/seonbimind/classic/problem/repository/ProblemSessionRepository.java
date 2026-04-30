package youngju.seonbimind.classic.problem.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.problem.entity.ProblemSession;

public interface ProblemSessionRepository extends JpaRepository<ProblemSession, Long> {

    Optional<ProblemSession> findByIdAndMember(Long id, AuthMember member);

    boolean existsByMemberAndTodaySentenceTrueAndCreatedAtBetween(
            AuthMember member,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<ProblemSession> findFirstByMemberAndTodaySentenceTrueAndCreatedAtBetweenOrderByCreatedAtAsc(
            AuthMember member,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<ProblemSession> findFirstByMemberAndCompletedTrueAndCompletedAtBetweenOrderByCompletedAtAsc(
            AuthMember member,
            LocalDateTime start,
            LocalDateTime end
    );
}
