package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;

public interface UserVerificationStoragePort {
    OldCompanyBillingProfile updateCompanyVerification(OldCompanyBillingProfile companyBillingProfile);

    OldIndividualBillingProfile updateIndividualVerification(OldIndividualBillingProfile individualBillingProfile);
}
