package youngju.seonbimind.classic.progress.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.progress.entity.SolvedProblemHistory;

public interface SolvedProblemHistoryRepository extends JpaRepository<SolvedProblemHistory, Long> {

    List<SolvedProblemHistory> findByMemberOrderBySolvedAtDesc(AuthMember member);

    List<SolvedProblemHistory> findByMemberOrderBySolvedAtAsc(AuthMember member);

    Optional<SolvedProblemHistory> findByMemberAndHistoryId(AuthMember member, Long historyId);

    boolean existsByMemberAndHistoryId(AuthMember member, Long historyId);

    @Query("""
            select coalesce(max(history.historyId), 0)
            from SolvedProblemHistory history
            where history.member = :member
            """)
    Long findMaxHistoryIdByMember(@Param("member") AuthMember member);
}
