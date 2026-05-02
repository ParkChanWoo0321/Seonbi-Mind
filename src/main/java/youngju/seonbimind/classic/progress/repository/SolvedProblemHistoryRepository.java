package youngju.seonbimind.classic.progress.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.progress.entity.SolvedProblemHistory;

public interface SolvedProblemHistoryRepository extends JpaRepository<SolvedProblemHistory, Long> {

    List<SolvedProblemHistory> findByMemberOrderBySolvedAtDesc(AuthMember member);
}
