package youngju.seonbimind.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import youngju.seonbimind.auth.entity.AuthMember;

public interface AuthMemberRepository extends JpaRepository<AuthMember, Long> {

    Optional<AuthMember> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
}
