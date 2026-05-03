package youngju.seonbimind.classic.sentence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.classic.sentence.entity.ClassicSentence;

public interface ClassicSentenceRepository extends JpaRepository<ClassicSentence, Long> {

    @Query("""
            select sentence
            from ClassicSentence sentence
            where not exists (
                select usage.id
                from UserSentenceUsage usage
                where usage.member = :member
                  and usage.sentence = sentence
            )
            """)
    List<ClassicSentence> findUnsolvedByMember(@Param("member") AuthMember member);

    @Query("""
            select sentence
            from ClassicSentence sentence
            where sentence.id <> :excludedSentenceId
              and not exists (
                select usage.id
                from UserSentenceUsage usage
                where usage.member = :member
                  and usage.sentence = sentence
            )
            """)
    List<ClassicSentence> findUnsolvedByMemberExcluding(
            @Param("member") AuthMember member,
            @Param("excludedSentenceId") Long excludedSentenceId
    );
}
