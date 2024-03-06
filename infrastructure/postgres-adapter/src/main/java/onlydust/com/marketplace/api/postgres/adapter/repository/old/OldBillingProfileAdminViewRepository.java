package onlydust.com.marketplace.api.postgres.adapter.repository.old;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.OldBillingProfileAdminViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OldBillingProfileAdminViewRepository extends JpaRepository<OldBillingProfileAdminViewEntity, UUID> {


    @Query(nativeQuery = true, value = """
            select u.id,
                   u.email,
                   u.github_login,
                   upi.first_name
            from (select coalesce(cbp.user_id, ibc.user_id) user_id
                  from accounting.invoices i
                           left join company_billing_profiles cbp on cbp.id = i.billing_profile_id
                           left join individual_billing_profiles ibc on ibc.id = i.billing_profile_id
                  where i.id = :invoiceId) sb
                     join iam.users u on u.id = sb.user_id
                     left join user_profile_info upi on upi.id = u.id
            """)
    Optional<OldBillingProfileAdminViewEntity> findByInvoiceId(UUID invoiceId);
}
