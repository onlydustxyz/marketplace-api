package onlydust.com.marketplace.project.domain.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
public class OldBillingProfile {
    final @NonNull UUID id;
    final @NonNull OldBillingProfileType type;
    final @NonNull String name;
    final boolean invoiceMandateAccepted;
    @NonNull Integer rewardCount = 0;


    public static OldBillingProfile of(OldCompanyBillingProfile billingProfile) {
        return new OldBillingProfile(billingProfile.getId(),
                OldBillingProfileType.COMPANY,
                billingProfile.getName(),
                billingProfile.isInvoiceMandateAccepted());
    }

    public static OldBillingProfile of(OldIndividualBillingProfile billingProfile) {
        return new OldBillingProfile(billingProfile.getId(),
                OldBillingProfileType.INDIVIDUAL,
                "Personal",
                billingProfile.isInvoiceMandateAccepted());
    }
}
