package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvitationCancelled;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectLeaderInvitationCancelledEventDTO {

  @JsonProperty("aggregate_name")
  String aggregateName = "Project";

  @JsonProperty("event_name")
  String eventName = "LeaderInvitationCancelled";

  @JsonProperty("environment")
  String environment;

  @JsonProperty("payload")
  Payload payload;

  @Value
  @Builder(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @NoArgsConstructor(force = true)
  public static class Payload {

    @JsonProperty("id")
    UUID projectId;

    @JsonProperty("github_user_id")
    Long githubUserId;

    @JsonProperty("cancelled_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date cancelledAt;
  }

  public static ProjectLeaderInvitationCancelledEventDTO of(ProjectLeaderInvitationCancelled projectLeaderInvitationCancelled,
      String environment) {
    return ProjectLeaderInvitationCancelledEventDTO.builder()
        .environment(environment)
        .payload(Payload.builder()
            .projectId(projectLeaderInvitationCancelled.getProjectId())
            .githubUserId(projectLeaderInvitationCancelled.getGithubUserId())
            .cancelledAt(projectLeaderInvitationCancelled.getCancelledAt())
            .build())
        .build();
  }
}
