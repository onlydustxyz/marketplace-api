package onlydust.com.marketplace.api.slack;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfileChildrenKycVerification;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.DepositObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.DepositStoragePort;
import onlydust.com.marketplace.api.slack.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.HackathonObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.*;
import onlydust.com.marketplace.project.domain.view.GithubUserWithTelegramView;

import java.util.Set;

import static onlydust.com.marketplace.api.slack.mapper.DepositSubmittedOnSponsorMapper.mapToSlackBlock;

@Slf4j
@AllArgsConstructor
public class SlackApiAdapter implements BillingProfileObserverPort, ProjectObserverPort, HackathonObserverPort, ApplicationObserverPort, DepositObserverPort {

    private final SlackProperties slackProperties;
    private final SlackApiClient slackApiClient;
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final HackathonStoragePort hackathonStoragePort;
    private final DepositStoragePort depositStoragePort;
    private final SponsorStoragePort sponsorStoragePort;
    private final MetaBlockExplorer blockExplorer;

    @Override
    public void onUserRegistration(Hackathon.Id hackathonId, UserId userId) {
        final GithubUserWithTelegramView githubUserWithTelegramView = userStoragePort.findGithubUserWithTelegram(userId)
                .orElseThrow(() -> OnlyDustException.internalServerError("User %s not found".formatted(userId)));

        final Hackathon hackathon = hackathonStoragePort.findById(hackathonId).orElseThrow(() -> OnlyDustException.internalServerError(
                "Hackathon %s not found".formatted(hackathonId.value())));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New user registration on hackathon",
                UserRegisteredOnHackathonEventMapper.mapToSlackBlock(userId, githubUserWithTelegramView, hackathon, slackProperties.getEnvironment()));
    }

    @Override
    public void onProjectCreated(ProjectId projectId, UserId projectLeadId) {
        final var user = userStoragePort.getRegisteredUserById(projectLeadId)
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(projectLeadId)));
        final var project = projectStoragePort.getById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound("Project not found %s".formatted(projectId)));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New project created", ProjectCreatedEventMapper.mapToSlackBlock(user,
                project, slackProperties.getEnvironment()));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        final var user = userStoragePort.getRegisteredUserById(billingProfileVerificationUpdated.getUserId())
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(billingProfileVerificationUpdated.getUserId().value())));
        slackApiClient.sendNotification(slackProperties.getKycKybChannel(), "New KYC/KYB event",
                BillingProfileVerificationEventMapper.mapToSlackBlock(billingProfileVerificationUpdated, user,
                        slackProperties.getTagAllChannel()));
    }

    @Override
    public void onApplicationCreated(Application application) {
        final var user = userStoragePort.getIndexedUserByGithubId(application.applicantId())
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(application.applicantId())));
        final var project = projectStoragePort.getById(application.projectId())
                .orElseThrow(() -> OnlyDustException.notFound("Project not found %s".formatted(application.projectId())));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New user application on project",
                UserAppliedOnProjectEventMapper.mapToSlackBlock(user.login(),
                        project, slackProperties.getEnvironment()));
    }

    @Override
    public void onApplicationAccepted(Application application, UserId projectLeadId) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UserId userId) {
        final var user = userStoragePort.getRegisteredUserById(userId)
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(userId)));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New project category suggested",
                ProjectCategorySuggestionEventMapper.mapToSlackBlock(user,
                        categoryName, slackProperties.getEnvironment()));
    }

    @Override
    public void onInvoiceUploaded(BillingProfile.Id billingProfileId, Invoice.Id invoiceId, boolean isExternal) {
    }

    @Override
    public void onInvoiceRejected(Invoice.@NonNull Id invoiceId, @NonNull String rejectionReason) {
    }

    @Override
    public void onLinkedReposChanged(ProjectId projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
    }

    @Override
    public void onRewardSettingsChanged(ProjectId projectId) {
    }

    @Override
    public void onBillingProfileExternalVerificationRequested(@NonNull BillingProfileChildrenKycVerification billingProfileChildrenKycVerification) {

    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {

    }

    @Override
    public void onApplicationRefused(Application application) {

    }

    @Override
    public void onDepositSubmittedByUser(UserId userId, Deposit.Id depositId) {
        final var user = userStoragePort.getRegisteredUserById(userId)
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(userId.value())));
        final Deposit deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> OnlyDustException.notFound("Deposit not found %s".formatted(depositId.value())));
        final Sponsor sponsor = sponsorStoragePort.get(deposit.sponsorId())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor not found %s".formatted(deposit.sponsorId().value())));

        slackApiClient.sendNotification(
                slackProperties.getFinanceChannel(),
                "New deposit submitted on sponsor %s".formatted(sponsor.name()),
                mapToSlackBlock(user, sponsor, deposit, slackProperties.getEnvironment(), blockExplorer.url(deposit.transaction()))
        );
    }

    @Override
    public void onDepositRejected(Deposit.Id depositId) {

    }

    @Override
    public void onDepositApproved(Deposit.Id depositId) {

    }
}
