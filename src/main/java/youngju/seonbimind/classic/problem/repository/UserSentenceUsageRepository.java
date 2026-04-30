package youngju.seonbimind.classic.problem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.problem.entity.UserSentenceUsage;
import youngju.seonbimind.classic.sentence.entity.ClassicSentence;

public interface UserSentenceUsageRepository extends JpaRepository<UserSentenceUsage, Long> {

    boolean existsByMemberAndSentence(AuthMember member, ClassicSentence sentence);
}
