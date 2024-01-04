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
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderInvited;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectLeaderInvitedEventDTO {

  @JsonProperty("aggregate_name")
  String aggregateName = "Project";

  @JsonProperty("event_name")
  String eventName = "LeaderInvited";

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

    @JsonProperty("invited_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    Date invitedAt;
  }

  public static ProjectLeaderInvitedEventDTO of(ProjectLeaderInvited projectLeaderInvited, String environment) {
    return ProjectLeaderInvitedEventDTO.builder()
        .environment(environment)
        .payload(Payload.builder()
            .projectId(projectLeaderInvited.getProjectId())
            .githubUserId(projectLeaderInvited.getGithubUserId())
            .invitedAt(projectLeaderInvited.getInvitedAt())
            .build())
        .build();
  }
}
