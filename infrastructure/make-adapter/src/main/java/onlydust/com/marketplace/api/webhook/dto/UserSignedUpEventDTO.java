package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.notification.UserSignedUp;

import java.util.Date;
import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignedUpEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "User";

    @JsonProperty("event_name")
    String eventName = "SignedUp";

    @JsonProperty("environment")
    String environment;

    @JsonProperty("payload")
    Payload payload;

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class Payload {
        @JsonProperty("user_id")
        UUID userId;

        @JsonProperty("github_user_id")
        Long githubUserId;

        @JsonProperty("signed_up_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        Date signedUpAt;
    }

    public static UserSignedUpEventDTO of(UserSignedUp userSignedUp, String environment) {
        return UserSignedUpEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .userId(userSignedUp.getUserId())
                        .githubUserId(userSignedUp.getGithubUserId())
                        .signedUpAt(userSignedUp.getSignedUpAt())
                        .build())
                .build();
    }
}
