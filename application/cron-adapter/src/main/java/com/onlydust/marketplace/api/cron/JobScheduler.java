package com.onlydust.marketplace.api.cron;

import com.onlydust.marketplace.api.cron.properties.CronProperties;
import com.onlydust.marketplace.api.cron.properties.NodeGuardiansBoostProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.service.AccountingObserver;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.project.domain.port.input.BoostNodeGuardiansRewardsPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;

@Slf4j
@AllArgsConstructor
@Profile("jobs")
public class JobScheduler {
    private final OutboxConsumerJob indexerOutboxJob;
    private final OutboxConsumerJob trackingOutboxJob;
    private final ProjectFacadePort projectFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final UserFacadePort userFacadePort;
    private final CronProperties cronProperties;
    private final OutboxConsumerJob billingProfileVerificationOutboxJob;
    private final AccountingObserver accountingObserver;
    private final OutboxConsumerJob accountingMailOutboxJob;
    private final BoostNodeGuardiansRewardsPort boostNodeGuardiansRewardsPort;
    private final NodeGuardiansBoostProperties nodeGuardiansBoostProperties;
    private final OutboxConsumerJob nodeGuardiansOutboxJob;

    @Scheduled(fixedDelayString = "${application.cron.indexer-sync-job-delay}")
    public void processPendingIndexerApiCalls() {
        LOGGER.info("Performing pending indexer API calls");
        indexerOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.tracking-job-delay}")
    public void processPendingTrackingEvents() {
        LOGGER.info("Sending pending tracking events");
        trackingOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-ranking}")
    public void updateProjectsRanking() {
        LOGGER.info("Updating projects ranking");
        projectFacadePort.updateProjectsRanking();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-tags}")
    public void updateProjectsTags() {
        LOGGER.info("Updating projects tags");
        projectFacadePort.updateProjectsTags();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-currency-quotes}")
    public void refreshCurrencyQuotes() {
        LOGGER.info("Refreshing currency quotes");
        currencyFacadePort.refreshQuotes();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-active-user-profiles}")
    public void refreshActiveUserProfiles() {
        LOGGER.info("Refreshing active user profiles");
        userFacadePort.refreshActiveUserProfiles(ZonedDateTime.now().minusDays(cronProperties.getActiveUserProfilesRefreshPeriodInDays()));
    }

    @Scheduled(fixedDelayString = "${application.cron.billing-profile-verification-job}")
    public void verifyBillingProfile() {
        LOGGER.debug("Verifying billing profiles");
        billingProfileVerificationOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-reward-usd-equivalents-job-delay}")
    public void refreshRewardUsdEquivalents() {
        LOGGER.info("Refreshing reward USD equivalents");
        accountingObserver.refreshRewardsUsdEquivalents();
    }

    @Scheduled(fixedDelayString = "${application.cron.send-emails}")
    public void sendEmails() {
        LOGGER.info("Sending emails");
        accountingMailOutboxJob.run();
    }

    @Scheduled(cron = "${application.cron.boost-rewards-cron-expression}")
    public void boostNodeGuardianRewards() {
        LOGGER.info("Boost rewards for NodeGuardians");
        boostNodeGuardiansRewardsPort.boostProject(nodeGuardiansBoostProperties.getProjectId(), nodeGuardiansBoostProperties.getProjectLeadId(),
                nodeGuardiansBoostProperties.getGithubRepoId(), nodeGuardiansBoostProperties.getEcosystemId());
    }

    @Scheduled(cron = "${application.cron.process-boosted-rewards-cron-expression}")
    public void processNodeGuardianRewards() {
        LOGGER.info("Processing NodeGuardians rewards boosts");
        nodeGuardiansOutboxJob.run();
    }

}
