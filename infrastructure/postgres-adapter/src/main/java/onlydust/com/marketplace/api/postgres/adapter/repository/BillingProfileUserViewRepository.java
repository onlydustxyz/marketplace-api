package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserViewEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileUserViewRepository extends JpaRepository<BillingProfileUserViewEntity, BillingProfileUserViewEntity.PrimaryKey> {


    @Language("PostgreSQL")
    String SELECT_COWORKER = """
            SELECT
                bpu.billing_profile_id,
                u.github_user_id,
                bpu.role,
                bpu.user_id,
                u.email,
                COALESCE(ga.login, u.github_login) as github_login,
                user_avatar_url(u.github_user_id, COALESCE(ga.avatar_url, u.github_avatar_url)) as github_avatar_url,
                ga.html_url as github_html_url,
                kyc.first_name,
                kyc.last_name,
                kyc.country,
                bpu.joined_at,
                NULL as invited_at,
                COALESCE(reward_count.user_reward_count, 0) as reward_count,
                COALESCE(admin_count.billing_profile_admin_count, 0) as billing_profile_admin_count
            FROM accounting.billing_profiles_users bpu
            JOIN iam.users u ON u.id = bpu.user_id
            LEFT JOIN indexer_exp.github_accounts ga ON ga.id = u.github_user_id
            LEFT JOIN accounting.kyc kyc ON kyc.owner_id = u.id AND kyc.verification_status = 'VERIFIED'
            LEFT JOIN LATERAL (
                SELECT COUNT(r.id) as user_reward_count
                FROM accounting.invoices i
                LEFT JOIN rewards r ON r.recipient_id = u.github_user_id AND r.invoice_id = i.id
                WHERE i.billing_profile_id = bpu.billing_profile_id) reward_count ON true
            LEFT JOIN LATERAL (
                SELECT COUNT(admins.user_id) as billing_profile_admin_count
                FROM accounting.billing_profiles_users admins
                WHERE admins.billing_profile_id = bpu.billing_profile_id AND admins.role = 'ADMIN') admin_count ON true
            """;

    @Language("PostgreSQL")
    String SELECT_INVITED_COWORKER = """
            SELECT
                bpui.billing_profile_id,
                bpui.github_user_id,
                bpui.role,
                u.id as user_id,
                u.email,
                ga.login as github_login,
                user_avatar_url(bpui.github_user_id, COALESCE(ga.avatar_url, u.github_avatar_url)) as github_avatar_url,
                ga.html_url as github_html_url,
                kyc.first_name,
                kyc.last_name,
                kyc.country,
                NULL as joined_at,
                bpui.invited_at,
                NULL as reward_count,
                NULL as billing_profile_admin_count
            FROM accounting.billing_profiles_user_invitations bpui
            LEFT JOIN iam.users u ON u.github_user_id = bpui.github_user_id
            LEFT JOIN indexer_exp.github_accounts ga ON ga.id = bpui.github_user_id
            LEFT JOIN accounting.kyc kyc ON kyc.owner_id = u.id AND kyc.verification_status = 'VERIFIED'
            """;

    @Query(value = SELECT_COWORKER +
                   " WHERE bpu.billing_profile_id = :billingProfileId " +
                   " AND (coalesce(roles) IS NULL OR cast(bpu.role as text) IN (:roles)) " +
                   " UNION " +
                   SELECT_INVITED_COWORKER +
                   " WHERE bpui.billing_profile_id = :billingProfileId " +
                   " AND (coalesce(roles) IS NULL OR cast(bpui.role as text) IN (:roles)) ", nativeQuery = true)
    Page<BillingProfileUserViewEntity> findByBillingProfileId(UUID billingProfileId, List<String> roles, Pageable pageable);

    @Query(value = SELECT_INVITED_COWORKER +
                   " WHERE bpui.billing_profile_id = :billingProfileId AND bpui.github_user_id = :githubUserId ", nativeQuery = true)
    Optional<BillingProfileUserViewEntity> findInvitedUserByBillingProfileIdAndGithubId(UUID billingProfileId, Long githubUserId);

    @Query(value = SELECT_COWORKER +
                   " WHERE bpu.billing_profile_id = :billingProfileId AND u.github_user_id = :githubUserId " +
                   " UNION " +
                   SELECT_INVITED_COWORKER +
                   " WHERE bpui.billing_profile_id = :billingProfileId AND bpui.github_user_id = :githubUserId ", nativeQuery = true)
    Optional<BillingProfileUserViewEntity> findUserByBillingProfileIdAndGithubId(UUID billingProfileId, Long githubUserId);

    @Query(value = SELECT_COWORKER +
                   " WHERE u.id = :userId AND bpu.billing_profile_id = :billingProfileId AND bpu.role = 'ADMIN' ", nativeQuery = true)
    Optional<BillingProfileUserViewEntity> findBillingProfileAdminById(UUID userId, UUID billingProfileId);
}