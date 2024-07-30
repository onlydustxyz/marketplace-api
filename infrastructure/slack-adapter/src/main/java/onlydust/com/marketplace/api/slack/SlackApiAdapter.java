package onlydust.com.marketplace.api.slack;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;
import onlydust.com.marketplace.api.slack.mapper.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.HackathonObserverPort;
import onlydust.com.marketplace.project.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.project.domain.port.output.ApplicationObserverPort;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

import java.util.Set;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
public class SlackApiAdapter implements BillingProfileObserverPort, ProjectObserverPort, HackathonObserverPort, ApplicationObserverPort {

    private final SlackProperties slackProperties;
    private final SlackApiClient slackApiClient;
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;
    private final HackathonStoragePort hackathonStoragePort;

    @Override
    public void onUserRegistration(Hackathon.Id hackathonId, UUID userId) {
        final UserProfileView userProfileView = userStoragePort.getProfileById(userId);

        final Hackathon hackathon = hackathonStoragePort.findById(hackathonId).orElseThrow(() -> OnlyDustException.internalServerError(
                "Hackathon %s not found".formatted(hackathonId.value())));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New user registration on hackathon",
                UserRegisteredOnHackathonEventMapper.mapToSlackBlock(userProfileView, hackathon, slackProperties.getEnvironment()));
    }

    @Override
    public void onProjectCreated(UUID projectId, UUID projectLeadId) {
        final User user = userStoragePort.getRegisteredUserById(projectLeadId)
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(projectLeadId)));
        final var project = projectStoragePort.getById(projectId)
                .orElseThrow(() -> OnlyDustException.notFound("Project not found %s".formatted(projectId)));
        slackApiClient.sendNotification(slackProperties.getDevRelChannel(), "New project created", ProjectCreatedEventMapper.mapToSlackBlock(user,
                project, slackProperties.getEnvironment()));
    }

    @Override
    public void onBillingProfileUpdated(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        final User user = userStoragePort.getRegisteredUserById(billingProfileVerificationUpdated.getUserId().value())
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
                UserAppliedOnProjectEventMapper.mapToSlackBlock(user.getGithubLogin(),
                        project, slackProperties.getEnvironment()));
    }

    @Override
    public void onApplicationAccepted(Application application) {
    }

    @Override
    public void onHackathonExternalApplicationDetected(GithubIssue issue, Long applicantId, Hackathon hackathon) {
    }

    @Override
    public void onProjectCategorySuggested(String categoryName, UUID userId) {
        final User user = userStoragePort.getRegisteredUserById(userId)
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
    public void onLinkedReposChanged(UUID projectId, Set<Long> linkedRepoIds, Set<Long> unlinkedRepoIds) {
    }

    @Override
    public void onRewardSettingsChanged(UUID projectId) {
    }
}
