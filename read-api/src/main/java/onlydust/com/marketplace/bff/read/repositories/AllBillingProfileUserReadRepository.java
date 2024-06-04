package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AllBillingProfileUserReadRepository extends JpaRepository<AllBillingProfileUserReadEntity, AllBillingProfileUserReadEntity.PrimaryKey> {
    List<AllBillingProfileUserReadEntity> findAllByUserId(UUID userId);
}
