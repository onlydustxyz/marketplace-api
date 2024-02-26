package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;

public interface UserVerificationStoragePort {
    CompanyBillingProfile updateCompanyVerification(CompanyBillingProfile companyBillingProfile);

    IndividualBillingProfile updateIndividualVerification(IndividualBillingProfile individualBillingProfile);

    VerificationStatus getCompanyVerificationStatus(CompanyBillingProfile companyBillingProfile);
}
