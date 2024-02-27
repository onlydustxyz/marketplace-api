package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;

public interface BillingProfileVerificationProviderPort {
    Kyc getUpdatedKyc(Kyc kyc);

    Kyb getUpdatedKyb(Kyb kyb);
}
