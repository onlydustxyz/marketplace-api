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
    @NonNull
    Integer issueCreatedEmailId;
    @NonNull
    Integer fundsAllocatedToProgramEmailId;
    @NonNull
    Integer fundsUnallocatedFromProgramEmailId;
    @NonNull
    Integer fundsUngrantedFromProjectEmailId;
    @NonNull
    Integer depositApprovedEmailId;
    @NonNull
    Integer depositRejectedEmailId;
    @NonNull
    String trackingBaseUri;
    @NonNull
    String trackingSiteId;
    @NonNull
    String trackingApiKey;
    @NonNull
    Integer marketingTopicId;
}
