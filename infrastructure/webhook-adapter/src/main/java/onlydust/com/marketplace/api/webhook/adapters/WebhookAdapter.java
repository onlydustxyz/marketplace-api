package onlydust.com.marketplace.api.webhook.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.*;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.WebhookHttpClient;
import onlydust.com.marketplace.api.webhook.dto.*;

@AllArgsConstructor
public class WebhookAdapter implements WebhookPort {

    private final WebhookHttpClient webhookHttpClient;
    private final Config config;

    @Override
    public void send(Event event) {
        if (event instanceof ProjectCreated projectCreated) {
            webhookHttpClient.post(ProjectCreatedEventDTO.of(projectCreated, config.getEnvironment()));
        } else if (event instanceof ProjectUpdated projectUpdated) {
            webhookHttpClient.post(ProjectUpdatedEventDTO.of(projectUpdated, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderAssigned projectLeaderAssigned) {
            webhookHttpClient.post(ProjectLeaderAssignedEventDTO.of(projectLeaderAssigned, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderUnassigned projectLeaderUnassigned) {
            webhookHttpClient.post(ProjectLeaderUnassignedEventDTO.of(projectLeaderUnassigned,
                    config.getEnvironment()));
        } else if (event instanceof UserAppliedOnProject userAppliedOnProject) {
            webhookHttpClient.post(UserAppliedOnProjectEventDTO.of(userAppliedOnProject, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderInvited projectLeaderInvited) {
            webhookHttpClient.post(ProjectLeaderInvitedEventDTO.of(projectLeaderInvited, config.getEnvironment()));
        } else if (event instanceof ProjectLeaderInvitationCancelled projectLeaderInvitationCancelled) {
            webhookHttpClient.post(ProjectLeaderInvitationCancelledEventDTO.of(projectLeaderInvitationCancelled,
                    config.getEnvironment()));
        } else {
            throw new IllegalArgumentException("Unknown notification type %s".formatted(event));
        }
    }
}
