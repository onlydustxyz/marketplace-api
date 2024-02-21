package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.OldIndividualBillingProfile;

public interface UserVerificationStoragePort {
    OldCompanyBillingProfile updateCompanyVerification(OldCompanyBillingProfile companyBillingProfile);

    OldIndividualBillingProfile updateIndividualVerification(OldIndividualBillingProfile individualBillingProfile);
}
