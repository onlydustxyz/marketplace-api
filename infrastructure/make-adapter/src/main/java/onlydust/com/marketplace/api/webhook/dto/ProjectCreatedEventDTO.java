package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.notification.ProjectCreated;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectCreatedEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "Project";

    @JsonProperty("event_name")
    String eventName = "Created";

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

        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date createdAt;
    }

    public static ProjectCreatedEventDTO of(ProjectCreated projectCreated, String environment) {
        return ProjectCreatedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .projectId(projectCreated.getProjectId())
                        .createdAt(projectCreated.getCreatedAt())
                        .build())
                .build();
    }
}
