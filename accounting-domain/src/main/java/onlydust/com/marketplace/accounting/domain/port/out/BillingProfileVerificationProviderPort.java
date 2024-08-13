package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualKycIdentity;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;

import java.util.Optional;

public interface BillingProfileVerificationProviderPort {
    Kyc getUpdatedKyc(Kyc kyc);

    Kyb getUpdatedKyb(Kyb kyb);

    Optional<IndividualKycIdentity> getIndividualIdentityForKycId(@NonNull String externalApplicantId);

    Optional<String> getExternalVerificationLink(@NonNull String externalApplicantId);
}
