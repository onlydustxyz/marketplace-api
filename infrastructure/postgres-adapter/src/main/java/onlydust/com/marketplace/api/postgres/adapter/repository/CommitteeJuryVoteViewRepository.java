package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommitteeJuryVoteViewRepository extends JpaRepository<CommitteeJuryVoteViewEntity, CommitteeJuryVoteViewEntity.PrimaryKey> {

    @Query(nativeQuery = true, value = """
                select cjv.*,
                       cjc.criteria,
                       u.github_user_id user_github_id,
                       u.github_login user_github_login,
                       u.github_avatar_url user_github_avatar_url
                from committee_jury_votes cjv
                join iam.users u on u.id = cjv.user_id
                join committee_jury_criteria cjc on cjv.criteria_id = cjc.id
                where cjv.committee_id = :committeeId and cjv.project_id = :projectId
            """)
    List<CommitteeJuryVoteViewEntity> findAllByCommitteeIdAndProjectId(UUID committeeId, UUID projectId);

    @Query(nativeQuery = true, value = """
                select cjv.*,
                       cjc.criteria,
                       u.github_user_id user_github_id,
                       u.github_login user_github_login,
                       u.github_avatar_url user_github_avatar_url
                from committee_jury_votes cjv
                join iam.users u on u.id = cjv.user_id
                join committee_jury_criteria cjc on cjv.criteria_id = cjc.id
                where cjv.committee_id = :committeeId and cjv.user_id = :juryUserId
            """)
    List<CommitteeJuryVoteViewEntity> findAllByCommitteeIdAndUserId(UUID committeeId, UUID juryUserId);
}
