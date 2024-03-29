package onlydust.com.marketplace.api.webhook.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.events.InvoiceUploaded;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.MakeWebhookHttpClient;
import onlydust.com.marketplace.api.webhook.dto.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.WebhookPort;
import onlydust.com.marketplace.project.domain.model.notification.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class MakeWebhookAdapter implements WebhookPort, MailNotificationPort {

    private final MakeWebhookHttpClient makeWebhookHttpClient;
    private final Config config;

    @Override
    public void send(Event event) {
        if (event instanceof ProjectCreated projectCreated) {
            makeWebhookHttpClient.post(ProjectCreatedEventDTO.of(projectCreated, config.getEnvironment()));
        } else if (event instanceof ProjectUpdated projectUpdated) {
            makeWebhookHttpClient.post(ProjectUpdatedEventDTO.of(projectUpdated, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderAssigned projectLeaderAssigned) {
            makeWebhookHttpClient.post(ProjectLeaderAssignedEventDTO.of(projectLeaderAssigned, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderUnassigned projectLeaderUnassigned) {
            makeWebhookHttpClient.post(ProjectLeaderUnassignedEventDTO.of(projectLeaderUnassigned,
                    config.getEnvironment()));
        } else if (event instanceof UserAppliedOnProject userAppliedOnProject) {
            makeWebhookHttpClient.post(UserAppliedOnProjectEventDTO.of(userAppliedOnProject, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderInvited projectLeaderInvited) {
            makeWebhookHttpClient.post(ProjectLeaderInvitedEventDTO.of(projectLeaderInvited, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderInvitationCancelled projectLeaderInvitationCancelled) {
            makeWebhookHttpClient.post(ProjectLeaderInvitationCancelledEventDTO.of(projectLeaderInvitationCancelled,
                    config.getEnvironment()));
        } else if (event instanceof UserSignedUp userSignedUp) {
            makeWebhookHttpClient.post(UserSignedUpEventDTO.of(userSignedUp, config.getEnvironment()));
        } else if (event instanceof BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
            makeWebhookHttpClient.post(BillingProfileVerificationStatusUpdatedEventDTO.of(billingProfileVerificationUpdated, config.getEnvironment()));
        } else if (event instanceof InvoiceUploaded invoiceUploaded) {
            makeWebhookHttpClient.post(InvoiceUploadedEventDTO.of(invoiceUploaded, config.getEnvironment()));
        } else if (event instanceof InvoiceRejected invoiceRejected) {
            makeWebhookHttpClient.post(InvoiceRejectedEventDTO.fromEvent(invoiceRejected), config.getSendRejectedInvoiceMailUrl(), config.getApiKey());
        } else {
            throw new IllegalArgumentException("Unknown notification type %s".formatted(event));
        }
    }

    @Override
    public void sendRewardsPaidMail(@NotNull final String email, @NotNull final List<RewardDetailsView> rewardViews) {
        makeWebhookHttpClient.post(RewardsPaidEmailDTO.from(email, rewardViews), config.getSendRewardsPaidMailUrl(), config.getApiKey());
    }
}
