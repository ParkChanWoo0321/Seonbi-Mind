package youngju.seonbimind.classic.progress.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.progress.entity.UserProgress;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByMember(AuthMember member);
}
