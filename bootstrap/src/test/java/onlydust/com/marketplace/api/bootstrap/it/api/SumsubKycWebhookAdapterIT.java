package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;
import static org.assertj.core.api.Assertions.assertThat;

public class SumsubKycWebhookAdapterIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubProperties sumsubProperties;
    @Autowired
    OutboxPort userVerificationOutbox;

    @Test
    void should_consume_simple_message() {
        // Given
        final byte[] sumsubPayload = """
                {
                  "applicantId": "64106d6b7d5a2d5159e6b01a",
                  "inspectionId": "64106d6b7d5a2d5159e6b01b",
                  "applicantType": "individual",
                  "correlationId": "req-57fed49a-07b8-4413-bdaa-a1be903769e9",
                  "levelName": "basic-kyc-level",
                  "sandboxMode": false,
                  "externalUserId": "12672",
                  "type": "applicantWorkflowCompleted",
                  "reviewResult": {
                    "reviewAnswer": "RED",
                    "rejectLabels": [
                      "AGE_REQUIREMENT_MISMATCH"
                    ],
                    "reviewRejectType": "FINAL",
                    "buttonIds": []
                  },
                  "reviewStatus": "completed",
                  "createdAtMs": "2023-03-14 12:50:27.238",
                  "clientId": "coolClientId",
                  "applicantMemberOf" : ["1","2"]
                }""".getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest = SumsubSignatureVerifier.hmac(sumsubPayload, sumsubProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest)
                .bodyValue(sumsubPayload)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final Optional<Event> event = userVerificationOutbox.peek();
        assertThat(event).isNotEmpty();
        assertThat(event.get()).isInstanceOf(SumsubWebhookEventDTO.class);
    }
}
