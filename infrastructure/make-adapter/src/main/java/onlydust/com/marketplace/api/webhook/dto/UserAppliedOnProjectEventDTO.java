package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.notification.UserAppliedOnProject;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAppliedOnProjectEventDTO {


    @JsonProperty("aggregate_name")
    String aggregateName = "Application";

    @JsonProperty("event_name")
    String eventName = "Received";

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
        UUID id;

        @JsonProperty("project_id")
        UUID projectId;

        @JsonProperty("applicant_id")
        UUID applicantId;

        @JsonProperty("received_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date receivedAt;
    }

    public static UserAppliedOnProjectEventDTO of(UserAppliedOnProject userAppliedOnProject, String environment) {
        return UserAppliedOnProjectEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .id(userAppliedOnProject.getApplicationId())
                        .projectId(userAppliedOnProject.getProjectId())
                        .applicantId(userAppliedOnProject.getUserId())
                        .receivedAt(userAppliedOnProject.getAppliedAt())
                        .build())
                .build();
    }
}
