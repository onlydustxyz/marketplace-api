package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.BoPaymentEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BoPaymentRepository extends JpaRepository<BoPaymentEntity, UUID> {

    @Query(value = """
            SELECT
            	r.id,
            	r.project_id,
            	r.amount,
            	r.currency_id as currency_id,
            	r.recipient_id,
            	r.requestor_id,
            	Items.urls AS items,
            	r.requested_at,
            	Counters.*,
            	user_wallets.wallets as recipient_wallets,
            	ba.number as recipient_iban,
            	ba.bic as recipient_bic
            FROM
            	rewards r
            	LEFT JOIN iam.users u ON u.github_user_id = r.recipient_id
            	LEFT JOIN accounting.payout_preferences pp on pp.project_id = r.project_id AND pp.user_id = u.id
            	LEFT JOIN accounting.bank_accounts ba ON ba.billing_profile_id = pp.billing_profile_id
            	LEFT JOIN (
            	    SELECT
            	        billing_profile_id,
            	        jsonb_agg(jsonb_build_object('network', network, 'type', type, 'address', address)) AS wallets
                    FROM
                        accounting.wallets
                    GROUP BY 
                        billing_profile_id
            	) user_wallets ON user_wallets.billing_profile_id = pp.billing_profile_id
            	INNER JOIN (
            		SELECT
            			jsonb_agg(coalesce(pr.html_url, i.html_url)) AS urls,
            			ri.reward_id
            		FROM
            			reward_items ri
            			LEFT JOIN indexer_exp.github_pull_requests pr on pr.repo_id = ri.repo_id AND pr.number = ri.number
            			LEFT JOIN indexer_exp.github_issues i on i.repo_id = ri.repo_id AND i.number = ri.number
            		GROUP BY
            			ri.reward_id
            	) Items ON (r.id = Items.reward_id)
            	INNER JOIN (
            		SELECT
            			reward_id,
            			count(prs) AS pull_requests_count,
            			count(issues) AS issues_count,
            			count(dusty_issues) AS dusty_issues_count,
            			count(code_reviews) AS code_reviews_count
            		FROM
            			reward_items
            			LEFT JOIN indexer_exp.github_pull_requests prs ON reward_items.id = CAST(prs.id AS TEXT)
            			LEFT JOIN indexer_exp.github_issues issues ON reward_items.id = CAST(issues.id AS TEXT) AND issues.author_id != 129528947
            			LEFT JOIN indexer_exp.github_issues dusty_issues ON reward_items.id = CAST(dusty_issues.id AS TEXT) AND dusty_issues.author_id = 129528947
            			LEFT JOIN indexer_exp.github_code_reviews code_reviews ON reward_items.id = code_reviews.id
            		GROUP BY
            			reward_id
            	) Counters ON (r.id = Counters.reward_id)
            WHERE 
                (COALESCE(:projectIds) IS NULL OR r.project_id in (:projectIds)) AND
                (COALESCE(:paymentIds) IS NULL OR r.id in (:paymentIds))
            """, nativeQuery = true)
    @NotNull
    Page<BoPaymentEntity> findAll(final List<UUID> projectIds, final List<UUID> paymentIds,
                                  final @NotNull Pageable pageable);
}
