package onlydust.com.marketplace.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.events.InvoiceUploaded;

import java.util.UUID;


@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceUploadedEventDTO {

    @JsonProperty("aggregate_name")
    String aggregateName = "BillingProfile";

    @JsonProperty("event_name")
    String eventName = "InvoiceUploaded";

    @JsonProperty("environment")
    String environment;

    @JsonProperty("payload")
    Payload payload;

    @Value
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class Payload {
        @JsonProperty("billing_profile_id")
        UUID billingProfileId;

        @JsonProperty("invoice_id")
        UUID invoiceId;

        @JsonProperty("is_external")
        boolean isExternal;
    }

    public static InvoiceUploadedEventDTO of(InvoiceUploaded event, String environment) {
        return InvoiceUploadedEventDTO.builder()
                .environment(environment)
                .payload(Payload.builder()
                        .billingProfileId(event.billingProfileId().value())
                        .invoiceId(event.invoiceId().value())
                        .isExternal(event.isExternal())
                        .build())
                .build();
    }
}
