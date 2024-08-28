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
    String onlyDustMarketingEmail;
    @NonNull
    Integer rewardsPaidEmailId;
    @NonNull
    Integer rewardCanceledEmailId;
    @NonNull
    Integer newRewardReceivedEmailId;
    @NonNull
    Integer invoiceRejectedEmailId;
    @NonNull
    Integer verificationClosedEmailId;
    @NonNull
    Integer verificationRejectedEmailId;
    @NonNull
    Integer newCommitteeApplicationEmailId;
    @NonNull
    Integer projectApplicationsToReviewByUserEmailId;
    @NonNull
    Integer projectApplicationAcceptedEmailId;
    @NonNull
    String environment;
    @NonNull
    Integer kycIndividualVerificationEmailId;
    @NonNull
    Integer completeYourBillingProfileEmailId;
    @NonNull
    Integer weeklyNotificationsEmailId;
    @NonNull
    Integer projectApplicationRefusedEmailId;
}
