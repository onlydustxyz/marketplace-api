package onlydust.com.marketplace.api.domain.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(fluent = true, chain = true)
public class BillingProfile {
    final @NonNull UUID id;
    final @NonNull BillingProfileType type;
    final @NonNull String name;
    final boolean invoiceMandateAccepted;
    @NonNull Integer rewardCount = 0;


    public static BillingProfile of(CompanyBillingProfile billingProfile) {
        return new BillingProfile(billingProfile.getId(),
                BillingProfileType.COMPANY,
                billingProfile.getName(),
                billingProfile.isInvoiceMandateAccepted());
    }

    public static BillingProfile of(IndividualBillingProfile billingProfile) {
        return new BillingProfile(billingProfile.getId(),
                BillingProfileType.INDIVIDUAL,
                "Personal",
                billingProfile.isInvoiceMandateAccepted());
    }
}
