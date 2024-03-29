package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.notification.ProjectLeaderAssigned;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectLeaderAssignedEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "Project";

    @JsonProperty("event_name")
    String eventName = "LeaderAssigned";

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

        @JsonProperty("leader_id")
        UUID leaderId;

        @JsonProperty("assigned_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date assignedAt;
    }

    public static ProjectLeaderAssignedEventDTO of(ProjectLeaderAssigned projectLeaderAssigned, String environment) {
        return ProjectLeaderAssignedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .projectId(projectLeaderAssigned.getProjectId())
                        .leaderId(projectLeaderAssigned.getLeaderId())
                        .assignedAt(projectLeaderAssigned.getAssignedAt())
                        .build())
                .build();
    }
}
