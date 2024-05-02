package com.onlydust.marketplace.api.cron.properties;

import lombok.Data;

@Data
public class CronProperties {
    Long notificationJobDelay;
    Long indexerSyncJobDelay;
    Long updateProjectsRanking;
    Long updateProjectsTags;
    Long refreshCurrencyQuotes;
    Long refreshActiveUserProfiles;
    Long activeUserProfilesRefreshPeriodInDays;
    Long trackingJobDelay;
    Long billingProfileVerificationJob;
    Long refreshRewardUsdEquivalentsJobDelay;
}
