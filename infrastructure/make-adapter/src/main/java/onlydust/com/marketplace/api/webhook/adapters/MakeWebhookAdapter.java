package onlydust.com.marketplace.api.webhook.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.events.InvoiceUploaded;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.MakeWebhookHttpClient;
import onlydust.com.marketplace.api.webhook.dto.*;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.project.domain.model.notification.*;
import onlydust.com.marketplace.kernel.port.output.WebhookPort;

@AllArgsConstructor
public class MakeWebhookAdapter implements WebhookPort {

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
            makeWebhookHttpClient.post(UserBillingProfileVerificationStatusUpdatedEventDTO.of(billingProfileVerificationUpdated, config.getEnvironment()));
        } else if (event instanceof InvoiceUploaded invoiceUploaded) {
            makeWebhookHttpClient.post(InvoiceUploadedEventDTO.of(invoiceUploaded, config.getEnvironment()));
        } else if (event instanceof InvoiceRejected invoiceRejected) {
            makeWebhookHttpClient.post(InvoiceRejectedEventDTO.fromEvent(invoiceRejected), config.getSendRejectedInvoiceEmailUrl(), config.getApiKey());
        } else {
            throw new IllegalArgumentException("Unknown notification type %s".formatted(event));
        }
    }
}
