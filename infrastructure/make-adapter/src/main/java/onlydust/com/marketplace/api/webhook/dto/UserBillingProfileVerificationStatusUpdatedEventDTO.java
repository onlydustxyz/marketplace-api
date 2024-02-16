package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.UserSignedUp;

import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBillingProfileVerificationStatusUpdatedEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "User";

    @JsonProperty("event_name")
    String eventName = "BillingProfileVerificationStatusUpdated";

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
        @JsonProperty("verification_status")
        String verificationStatus;
    }

    public static UserBillingProfileVerificationStatusUpdatedEventDTO of(BillingProfileUpdated billingProfileUpdated, String environment) {
        return UserBillingProfileVerificationStatusUpdatedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .userId(billingProfileUpdated.getUserId())
                        .verificationStatus(billingProfileUpdated.getVerificationStatus().name())
                        .build())
                .build();
    }
}
