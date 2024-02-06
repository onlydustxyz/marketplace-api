package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;
import static org.assertj.core.api.Assertions.assertThat;

public class SumsubWebhookAdapterIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubWebhookProperties sumsubWebhookProperties;
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
        final String sumsubDigest = SumsubSignatureVerifier.hmac(sumsubPayload, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
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

    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;

    @Test
    void should_update_company_billing_profile_to_pending() throws InterruptedException {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        // When
        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");

        final CompanyBillingProfileEntity companyBillingProfileEntity = companyBillingProfileRepository.findByUserId(userId).orElseThrow();

        final byte[] sumsubPayload = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyc-level",
                  "applicantId": "65bd228a2a99e9196014ccc5",
                  "createdAtMs": "2024-02-02 17:23:16.368",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": null,
                  "reviewStatus": "pending",
                  "applicantType": "company",
                  "correlationId": "0727edcd2fd452f64b7dd9f76516d815",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", companyBillingProfileEntity.getId().toString()).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest = SumsubSignatureVerifier.hmac(sumsubPayload, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest)
                .bodyValue(sumsubPayload)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        waitAtLeastOneCycleOfOutboxEventProcessing();

        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW");

        final byte[] sumsubPayload2 = String.format("""
                {
                  "type": "applicantReviewed",
                  "clientId": "onlydust",
                  "levelName": "basic-kyc-level",
                  "applicantId": "65bd228a2a99e9196014ccc5",
                  "createdAtMs": "2024-02-02 18:31:57.182",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": {
                    "reviewAnswer": "GREEN"
                  },
                  "reviewStatus": "completed",
                  "applicantType": "company",
                  "correlationId": "2e95d53348840f5f85bddc32a20f6abd",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", companyBillingProfileEntity.getId().toString()).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest2 = SumsubSignatureVerifier.hmac(sumsubPayload2, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest2)
                .bodyValue(sumsubPayload2)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        waitAtLeastOneCycleOfOutboxEventProcessing();

        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("VERIFIED");
    }

    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

    @Test
    void should_update_individual_billing_profile_to_pending() throws InterruptedException {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        // When
        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");

        final IndividualBillingProfileEntity individualBillingProfileEntity = individualBillingProfileRepository.findByUserId(userId).orElseThrow();

        final byte[] sumsubPayload = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyc-level",
                  "applicantId": "65bd228a2a99e9196014ccc5",
                  "createdAtMs": "2024-02-02 17:23:16.368",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": null,
                  "reviewStatus": "pending",
                  "applicantType": "individual",
                  "correlationId": "0727edcd2fd452f64b7dd9f76516d815",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", individualBillingProfileEntity.getId().toString()).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest = SumsubSignatureVerifier.hmac(sumsubPayload, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest)
                .bodyValue(sumsubPayload)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        waitAtLeastOneCycleOfOutboxEventProcessing();

        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW");

        final byte[] sumsubPayload2 = String.format("""
                {
                  "type": "applicantReviewed",
                  "clientId": "onlydust",
                  "levelName": "basic-kyc-level",
                  "applicantId": "65bd228a2a99e9196014ccc5",
                  "createdAtMs": "2024-02-02 18:31:57.182",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": {
                    "reviewAnswer": "GREEN"
                  },
                  "reviewStatus": "completed",
                  "applicantType": "individual",
                  "correlationId": "2e95d53348840f5f85bddc32a20f6abd",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", individualBillingProfileEntity.getId().toString()).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigest2 = SumsubSignatureVerifier.hmac(sumsubPayload2, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigest2)
                .bodyValue(sumsubPayload2)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        waitAtLeastOneCycleOfOutboxEventProcessing();

        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("VERIFIED");
    }


}
