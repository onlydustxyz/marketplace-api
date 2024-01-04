package onlydust.com.marketplace.api.webhook.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.model.notification.ProjectCreated;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvitationCancelled;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvited;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderUnassigned;
import onlydust.com.marketplace.api.domain.model.notification.ProjectUpdated;
import onlydust.com.marketplace.api.domain.model.notification.UserAppliedOnProject;
import onlydust.com.marketplace.api.domain.port.output.WebhookPort;
import onlydust.com.marketplace.api.webhook.Config;
import onlydust.com.marketplace.api.webhook.WebhookHttpClient;
import onlydust.com.marketplace.api.webhook.dto.ProjectCreatedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderAssignedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderInvitationCancelledEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderInvitedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectLeaderUnassignedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.ProjectUpdatedEventDTO;
import onlydust.com.marketplace.api.webhook.dto.UserAppliedOnProjectEventDTO;

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
