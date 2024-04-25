package com.onlydust.customer.io.adapter.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class CustomerIOProperties {
    @NonNull
    String baseUri;
    @NonNull
    String apiKey;
    @NonNull
    String onlyDustAdminEmail;
    @NonNull
    Integer rewardsPaidEmailId;
    @NonNull
    Integer rewardCanceledEmailId;
    @NonNull
    Integer newRewardReceivedEmailId;
    @NonNull
    Integer invoiceRejectedEmailId;
    @NonNull
    Integer verificationFailedEmailId;
}
