package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRewardViewEntityRepository extends JpaRepository<ContributionRewardViewEntity, UUID> {

    @Query(value = """
            SELECT r.id                                                AS id,
                   r.requested_at                                      AS requested_at,
                   r.amount                                            AS amount,
                   r.currency_id                                       AS currency_id,
                   r.billing_profile_id                                AS billing_profile_id,
                   r.project_id                                        AS project_id,
                   requestor.login                                     AS requestor_login,
                   user_avatar_url(requestor.id, requestor.avatar_url) AS requestor_avatar_url,
                   requestor.id                                        AS requestor_id,
                   recipient.login                                     AS recipient_login,
                   user_avatar_url(recipient.id, recipient.avatar_url) AS recipient_avatar_url,
                   recipient.id                                        AS recipient_id
            FROM indexer_exp.contributions c
                     JOIN reward_items ri ON ri.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id)
                     JOIN rewards r ON r.id = ri.reward_id AND r.recipient_id = c.contributor_id
                     JOIN iam.users u ON u.id = r.requestor_id
                     JOIN indexer_exp.github_accounts requestor ON requestor.id = u.github_user_id
                     JOIN indexer_exp.github_accounts recipient ON recipient.id = r.recipient_id
            WHERE c.id = :contributionId
              AND r.project_id = :projectId
             """, nativeQuery = true)
    List<ContributionRewardViewEntity> listByContributionId(UUID projectId, String contributionId);
}
