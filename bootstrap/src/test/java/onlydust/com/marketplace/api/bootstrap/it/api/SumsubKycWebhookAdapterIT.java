package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.configuration.SumsubWebhookConfiguration;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.LegalVerificationFacadePort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.nio.charset.StandardCharsets;

import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;
import static org.junit.Assert.assertEquals;

public class SumsubKycWebhookAdapterIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubProperties sumsubProperties;
    @Autowired
    LegalVerificationFacadePort legalVerificationFacadePort;

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
                  "createdAt": "2023-03-14 12:50:27+0000",
                  "createdAtMs": "2023-03-14 12:50:27.238",
                  "clientId": "coolClientId"
                }""".getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest = SumsubSignatureVerifier.hmac(sumsubPayload, sumsubProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/kyc/webhook"))
                .header(X_OD_API, sumsubProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest)
                .bodyValue(sumsubPayload)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        final SumsubWebhookConfiguration.LegalVerificationFacadePortSpy spy =
                (SumsubWebhookConfiguration.LegalVerificationFacadePortSpy) legalVerificationFacadePort;
        Assertions.assertEquals("64106d6b7d5a2d5159e6b01a", spy.getSumsubWebhookDTO().getApplicantId());
    }
}
