package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;

import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingProfileVerificationStatusUpdatedEventDTO {

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

    public static BillingProfileVerificationStatusUpdatedEventDTO of(BillingProfileVerificationUpdated event, String environment) {
        return BillingProfileVerificationStatusUpdatedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .userId(event.getUserId().value())
                        .verificationStatus(event.getVerificationStatus().name())
                        .build())
                .build();
    }
}
