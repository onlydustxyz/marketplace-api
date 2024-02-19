package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyBillingProfileRepository extends JpaRepository<CompanyBillingProfileEntity, UUID> {

    Optional<CompanyBillingProfileEntity> findByUserId(final UUID userId);

    Optional<CompanyBillingProfileEntity> findByApplicantId(final String applicantId);
}
