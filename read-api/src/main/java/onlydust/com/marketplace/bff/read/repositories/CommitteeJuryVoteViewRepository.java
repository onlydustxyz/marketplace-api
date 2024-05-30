package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.CommitteeJuryVoteViewEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommitteeJuryVoteViewRepository extends JpaRepository<CommitteeJuryVoteViewEntity, CommitteeJuryVoteViewEntity.PrimaryKey> {

    @Language("PostgreSQL")
    String SELECT = """
                select cjv.*,
                       cjc.criteria,
                       u.github_user_id user_github_id,
                       u.github_login user_github_login,
                       u.github_avatar_url user_github_avatar_url
                from committee_jury_votes cjv
                join iam.users u on u.id = cjv.user_id
                join committee_jury_criteria cjc on cjv.criteria_id = cjc.id
            """;

    @Query(nativeQuery = true, value = SELECT + """
                where cjv.committee_id = :committeeId and cjv.project_id = :projectId
            """)
    List<CommitteeJuryVoteViewEntity> findAllByCommitteeIdAndProjectId(UUID committeeId, UUID projectId);

    @Query(nativeQuery = true, value = SELECT + """
                where cjv.committee_id = :committeeId and cjv.user_id = :juryUserId
            """)
    List<CommitteeJuryVoteViewEntity> findAllByCommitteeIdAndUserId(UUID committeeId, UUID juryUserId);


    @Query(nativeQuery = true, value = SELECT + """
                where cjv.committee_id = :committeeId and cjv.project_id = :projectId and cjv.user_id = :juryUserId
            """)
    List<CommitteeJuryVoteViewEntity> findAllByCommitteeIdAndProjectIdAndUserId(UUID committeeId, UUID projectId, UUID juryUserId);
}
