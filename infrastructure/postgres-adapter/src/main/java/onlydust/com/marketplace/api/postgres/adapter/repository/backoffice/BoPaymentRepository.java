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
            	pr.id,
            	pr.project_id,
            	b.id as budget_id,
            	pr.amount,
            	pr.currency,
            	pr.recipient_id,
            	pr.requestor_id,
            	Items.urls AS items,
            	pr.requested_at,
            	Payments.processed_at,
            	Counters.*
            FROM
            	payment_requests pr
            	INNER JOIN projects_budgets pb on pb.project_id = pr.project_id
            	INNER JOIN budgets b on b.id = pb.budget_id AND b.currency = pr.currency
            	LEFT JOIN (
            	    SELECT 
            	        request_id,
            	        max(processed_at) as processed_at
                    FROM
                        payments
                    GROUP BY 
                        request_id
            	) Payments on (pr.id = Payments.request_id)
            	INNER JOIN (
            		SELECT
            			jsonb_agg(coalesce(pr.html_url, i.html_url)) AS urls,
            			wi.payment_id
            		FROM
            			work_items wi
            			LEFT JOIN indexer_exp.github_pull_requests pr on pr.repo_id = wi.repo_id AND pr.number = wi.number
            			LEFT JOIN indexer_exp.github_issues i on i.repo_id = wi.repo_id AND i.number = wi.number
            		GROUP BY
            			wi.payment_id
            	) Items ON (pr.id = Items.payment_id)
            	INNER JOIN (
            		SELECT
            			payment_id,
            			count(prs) AS pull_requests_count,
            			count(issues) AS issues_count,
            			count(dusty_issues) AS dusty_issues_count,
            			count(code_reviews) AS code_reviews_count
            		FROM
            			work_items
            			LEFT JOIN indexer_exp.github_pull_requests prs ON work_items.id = CAST(prs.id AS TEXT)
            			LEFT JOIN indexer_exp.github_issues issues ON work_items.id = CAST(issues.id AS TEXT) AND issues.author_id != 129528947
            			LEFT JOIN indexer_exp.github_issues dusty_issues ON work_items.id = CAST(dusty_issues.id AS TEXT) AND dusty_issues.author_id = 129528947
            			LEFT JOIN indexer_exp.github_code_reviews code_reviews ON work_items.id = code_reviews.id
            		GROUP BY
            			payment_id
            	) Counters ON (pr.id = Counters.payment_id)
            WHERE 
                (COALESCE(:projectIds) IS NULL OR pr.project_id in (:projectIds))
            """, nativeQuery = true)
    @NotNull
    Page<BoPaymentEntity> findAll(final List<UUID> projectIds, final @NotNull Pageable pageable);
}
