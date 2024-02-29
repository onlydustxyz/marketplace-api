package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributionRewardViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContributionRewardViewEntityRepository extends JpaRepository<ContributionRewardViewEntity, UUID> {

    @Query(value = """
            SELECT r.id,
                   r.requested_at,
                   rsd.paid_at,
                   r.amount,
                   r.currency_id,
                   rsd.amount_usd_equivalent                           AS dollars_equivalent,
                   rs.status_for_user                                  AS status,
                   requestor.login                                     AS requestor_login,
                   user_avatar_url(requestor.id, requestor.avatar_url) AS requestor_avatar_url,
                   requestor.id                                        AS requestor_id,
                   recipient.login                                     AS recipient_login,
                   user_avatar_url(recipient.id, recipient.avatar_url) AS recipient_avatar_url,
                   recipient.id                                        AS recipient_id
            FROM indexer_exp.contributions c
                     JOIN reward_items ri ON ri.id = COALESCE(CAST(c.pull_request_id AS TEXT), CAST(c.issue_id AS TEXT), c.code_review_id)
                     JOIN rewards r ON r.id = ri.reward_id AND r.recipient_id = c.contributor_id
                     JOIN accounting.reward_statuses rs ON rs.reward_id = r.id
                     JOIN accounting.reward_status_data rsd ON rsd.reward_id = r.id
                     JOIN iam.users u ON u.id = r.requestor_id
                     JOIN indexer_exp.github_accounts requestor ON requestor.id = u.github_user_id
                     JOIN indexer_exp.github_accounts recipient ON recipient.id = r.recipient_id
            WHERE c.id = :contributionId
              AND r.project_id = :projectId
             """, nativeQuery = true)
    List<ContributionRewardViewEntity> listByContributionId(UUID projectId, String contributionId);
}
