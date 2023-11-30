package onlydust.com.marketplace.api.webhook.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Notification;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderUnassigned;
import onlydust.com.marketplace.api.domain.model.notification.UserAppliedOnProject;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.WebhookHttpClient;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderAssignedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderUnassignedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.UserAppliedOnProjectEventDTO;

@AllArgsConstructor
public class WebhookAdapter implements WebhookPort {

    private final WebhookHttpClient webhookHttpClient;
    private final Config config;

    @Override
    public void send(Notification notification) {
        if (notification instanceof ProjectLeaderAssigned projectLeaderAssigned) {
            webhookHttpClient.post(ProjectLeaderAssignedEventDTO.of(projectLeaderAssigned, config.getEnvironment()));
        } else if (notification instanceof ProjectLeaderUnassigned projectLeaderUnassigned) {
            webhookHttpClient.post(ProjectLeaderUnassignedEventDTO.of(projectLeaderUnassigned,
                    config.getEnvironment()));
        } else if (notification instanceof UserAppliedOnProject userAppliedOnProject) {
            webhookHttpClient.post(UserAppliedOnProjectEventDTO.of(userAppliedOnProject, config.getEnvironment()));
        } else {
            throw new IllegalArgumentException("Unknown notification type %s".formatted(notification));
        }
    }
}
