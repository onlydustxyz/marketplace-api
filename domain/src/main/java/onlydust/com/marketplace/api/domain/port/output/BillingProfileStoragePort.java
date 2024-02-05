package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;

import java.util.Optional;
import java.util.UUID;

public interface BillingProfileStoragePort {
    Optional<CompanyBillingProfile> findCompanyProfileForUser(UUID userId);

    void saveCompanyProfileForUser(UUID userId, CompanyBillingProfile companyBillingProfile);

    Optional<IndividualBillingProfile> findIndividualBillingProfile(UUID userId);

    void saveIndividualProfileForUser(UUID userId,IndividualBillingProfile individualBillingProfile);
}
