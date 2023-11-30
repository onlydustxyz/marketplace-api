package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderAssigned;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectLeaderAssignedEventDTO {


    @JsonProperty("LeaderAssigned")
    LeaderAssigned leaderAssigned;

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class LeaderAssigned {
        @JsonProperty("id")
        UUID projectId;

        @JsonProperty("leader_id")
        UUID leaderId;

        @JsonProperty("assigned_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date assignedAt;
    }

    public static ProjectLeaderAssignedEventDTO of(ProjectLeaderAssigned projectLeaderAssigned) {
        return ProjectLeaderAssignedEventDTO.builder()
                .leaderAssigned(LeaderAssigned.builder()
                        .projectId(projectLeaderAssigned.getProjectId())
                        .leaderId(projectLeaderAssigned.getLeaderId())
                        .assignedAt(projectLeaderAssigned.getAssignedAt())
                        .build())
                .build();
    }
}
