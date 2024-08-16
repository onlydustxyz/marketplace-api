package com.onlydust.customer.io.adapter.dto;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;

public record KycIdentityVerificationDTO(@NonNull String username, @NonNull String title, @NonNull String description, @NonNull ButtonDTO button,
                                         @NonNull Boolean hasMoreInformation) {

    private static final String DESCRIPTION = "We need to verify your beneficiary identity to validate your company's billing profile %s";

    public static KycIdentityVerificationDTO from(@NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {
        return new KycIdentityVerificationDTO(
                billingProfileChildrenKycVerification.individualKycIdentity().firstName() + " " +
                billingProfileChildrenKycVerification.individualKycIdentity().lastName(),
                "Company beneficiary",
                DESCRIPTION.formatted(billingProfileChildrenKycVerification.billingProfileName()),
                new ButtonDTO("Proceed to verification", billingProfileChildrenKycVerification.externalLinkForVerification()),
                true
        );
    }
}
