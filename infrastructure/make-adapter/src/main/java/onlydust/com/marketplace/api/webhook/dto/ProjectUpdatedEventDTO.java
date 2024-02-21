package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.notification.ProjectUpdated;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectUpdatedEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "Project";

    @JsonProperty("event_name")
    String eventName = "Updated";

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

        @JsonProperty("updated_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date updatedAt;
    }

    public static ProjectUpdatedEventDTO of(ProjectUpdated projectUpdated, String environment) {
        return ProjectUpdatedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .projectId(projectUpdated.getProjectId())
                        .updatedAt(projectUpdated.getUpdatedAt())
                        .build())
                .build();
    }
}
