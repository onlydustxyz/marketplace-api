package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BillingProfileUserViewRepository extends JpaRepository<BillingProfileUserViewEntity, BillingProfileUserViewEntity.PrimaryKey> {

    @Query(value = """
            SELECT
                bpu.billing_profile_id,
                bpu.user_id,
                bpu.role,
                u.github_user_id,
                COALESCE(ga.login, u.github_login) as github_login,
                user_avatar_url(u.github_user_id, COALESCE(ga.avatar_url, u.github_avatar_url)) as github_avatar_url,
                ga.html_url as github_html_url,
                bpu.joined_at,
                bpu.invited_at,
                COALESCE(COUNT(r.id), 0) as reward_count
            FROM accounting.billing_profiles_users bpu
            JOIN iam.users u ON u.id = bpu.user_id
            LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
            LEFT JOIN accounting.invoices i ON i.billing_profile_id = bpu.billing_profile_id AND i.status != 'DRAFT' AND i.status != 'REJECTED'
            LEFT JOIN rewards r ON r.recipient_id = u.github_user_id AND r.invoice_id = i.id
            WHERE bpu.billing_profile_id = :billingProfileId
            GROUP BY bpu.billing_profile_id, bpu.user_id, bpu.role, u.github_user_id, ga.login, u.github_login, ga.avatar_url, u.github_avatar_url, ga.html_url, bpu.joined_at, bpu.invited_at
            """, nativeQuery = true)
    Page<BillingProfileUserViewEntity> findByBillingProfileId(UUID billingProfileId, Pageable pageable);
}