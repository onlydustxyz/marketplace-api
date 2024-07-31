package com.onlydust.marketplace.api.cron;

import com.onlydust.marketplace.api.cron.properties.NodeGuardiansBoostProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusService;
import onlydust.com.marketplace.kernel.jobs.OutboxConsumerJob;
import onlydust.com.marketplace.project.domain.job.ApplicationMailNotifier;
import onlydust.com.marketplace.project.domain.job.ApplicationsCleaner;
import onlydust.com.marketplace.project.domain.model.GlobalConfig;
import onlydust.com.marketplace.project.domain.port.input.BoostNodeGuardiansRewardsPort;
import onlydust.com.marketplace.project.domain.port.input.LanguageFacadePort;
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
    private final OutboxConsumerJob indexingEventsOutboxJob;
    private final ProjectFacadePort projectFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final UserFacadePort userFacadePort;
    private final GlobalConfig globalConfig;
    private final OutboxConsumerJob billingProfileVerificationOutboxJob;
    private final RewardStatusService rewardStatusService;
    private final OutboxConsumerJob accountingMailOutboxJob;
    private final BoostNodeGuardiansRewardsPort boostNodeGuardiansRewardsPort;
    private final NodeGuardiansBoostProperties nodeGuardiansBoostProperties;
    private final OutboxConsumerJob nodeGuardiansOutboxJob;
    private final OutboxConsumerJob projectMailOutboxJob;
    private final LanguageFacadePort languageFacadePort;
    private final ApplicationsCleaner applicationsCleaner;
    private final ApplicationMailNotifier applicationMailNotifier;

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

    @Scheduled(fixedDelayString = "${application.cron.indexing-event-job-delay}")
    public void processPendingIndexingEvents() {
        LOGGER.info("Processing indexing events");
        indexingEventsOutboxJob.run();
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
        userFacadePort.refreshActiveUserProfiles(ZonedDateTime.now().minusDays(globalConfig.getActiveUserProfilesRefreshPeriodInDays()));
    }

    @Scheduled(fixedDelayString = "${application.cron.billing-profile-verification-job}")
    public void verifyBillingProfile() {
        LOGGER.debug("Verifying billing profiles");
        billingProfileVerificationOutboxJob.run();
    }

    @Scheduled(fixedDelayString = "${application.cron.refresh-reward-usd-equivalents-job-delay}")
    public void refreshRewardUsdEquivalents() {
        LOGGER.info("Refreshing reward USD equivalents");
        rewardStatusService.refreshRewardsUsdEquivalents();
    }

    @Scheduled(fixedDelayString = "${application.cron.send-emails}")
    public void sendEmails() {
        LOGGER.info("Sending emails");
        accountingMailOutboxJob.run();
        projectMailOutboxJob.run();
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

    @Scheduled(fixedDelayString = "${application.cron.refresh-user-ranks}")
    public void refreshUserRanksAndStats() {
        LOGGER.info("Refreshing user ranks");
        userFacadePort.refreshUserRanksAndStats();
    }

    @Scheduled(cron = "${application.cron.historize-user-ranks-cron-expression}")
    public void historizeUserRanks() {
        LOGGER.info("Historizing user ranks");
        userFacadePort.historizeUserRanks();
    }

    @Scheduled(fixedDelayString = "${application.cron.update-projects-languages}")
    public void updateProjectLanguages() {
        LOGGER.info("Update projects languages");
        languageFacadePort.updateProjectsLanguages();
    }

    @Scheduled(fixedDelayString = "${application.cron.cleanup-obsolete-applications}")
    public void cleanUpObsoleteApplications() {
        LOGGER.info("Cleanup obsolete applications");
        applicationsCleaner.cleanUp();
    }

    //    @Scheduled(cron = "${application.cron.send-application-mail-cron-expression}")
    public void sendApplicationMailNotifications() {
        LOGGER.info("Sending application email notifications to project leads");
        applicationMailNotifier.notifyProjectApplicationsToReview();
    }

}
