package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = """
                delete from accounting.bank_accounts where billing_profile_id = :billingProfileId
            """)
    void deleteByBillingProfileId(UUID billingProfileId);
}
