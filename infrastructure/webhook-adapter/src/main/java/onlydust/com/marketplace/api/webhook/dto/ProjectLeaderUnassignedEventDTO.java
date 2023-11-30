package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.notification.ProjectLeaderUnassigned;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectLeaderUnassignedEventDTO {

    @JsonProperty("Project")
    Project project;

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class Project {
        @JsonProperty("LeaderUnassigned")
        LeaderUnassigned leaderUnassigned;
    }

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class LeaderUnassigned {
        @JsonProperty("id")
        UUID projectId;

        @JsonProperty("leader_id")
        UUID leaderId;

        @JsonProperty("assigned_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date unassignedAt;
    }

    public static ProjectLeaderUnassignedEventDTO of(ProjectLeaderUnassigned projectLeaderUnassigned) {
        return ProjectLeaderUnassignedEventDTO.builder()
                .project(Project.builder()
                        .leaderUnassigned(LeaderUnassigned.builder()
                                .projectId(projectLeaderUnassigned.getProjectId())
                                .leaderId(projectLeaderUnassigned.getLeaderId())
                                .unassignedAt(projectLeaderUnassigned.getUnassignedAt())
                                .build())
                        .build())
                .build();
    }
}
