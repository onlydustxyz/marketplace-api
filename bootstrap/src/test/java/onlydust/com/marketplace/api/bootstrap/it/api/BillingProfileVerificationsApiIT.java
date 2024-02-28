package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.onlydust.api.sumsub.api.client.adapter.SumsubApiClientAdapter;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import onlydust.com.marketplace.api.bootstrap.helper.SlackNotificationStub;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BillingProfileVerificationsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubWebhookProperties sumsubWebhookProperties;
    @Autowired
    SumsubClientProperties sumsubClientProperties;
    @Autowired
    SlackNotificationStub slackNotificationStub;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    BillingProfileRepository billingProfileRepository;

    @Test
    @Order(1)
    void should_verify_individual_billing_profile() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "individual",
                          "type": "INDIVIDUAL"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("individual")
                .jsonPath("$.type").isEqualTo("INDIVIDUAL")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.kyc.id").isNotEmpty()
                .jsonPath("$.kyb").isEmpty()
                .jsonPath("$.id").isNotEmpty();

        final UUID billingProfileId = billingProfileRepository.findBillingProfilesForUserId(userId).stream()
                .filter(billingProfileEntity -> billingProfileEntity.getType().equals(BillingProfileEntity.Type.INDIVIDUAL))
                .findFirst()
                .orElseThrow()
                .getId();

        final UUID kycId = kycRepository.findByBillingProfileId(billingProfileId).orElseThrow().getId();


        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kycId.toString());
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_INDIVIDUAL_RESPONSE_JSON)));

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
                }""", kycId).getBytes(StandardCharsets.UTF_8);
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

        billingProfileVerificationOutboxJob.run();
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId.toString()))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.kyc.address").isEqualTo("25 AVENUE SAINT LOUIS, ETAGE 2 APT, ST MAUR DES FOSSES, France, 94210")
                .jsonPath("$.kyc.firstName").isEqualTo("ALEXIS")
                .jsonPath("$.kyc.lastName").isEqualTo("BENOLIEL")
                .jsonPath("$.kyc.country").isEqualTo("France")
                .jsonPath("$.kyc.birthdate").isEqualTo("1995-09-19T00:00:00Z")
                .jsonPath("$.kyc.idDocumentNumber").isEqualTo("15AC05169")
                .jsonPath("$.kyc.idDocumentType").isEqualTo("PASSPORT")
                .jsonPath("$.kyc.idDocumentCountryCode").isEqualTo("FRA")
                .jsonPath("$.kyc.validUntil").isEqualTo("2025-04-19T00:00:00Z")
                .jsonPath("$.kyc.usCitizen").isEqualTo(false)
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.id").isNotEmpty();

        assertEquals(1, slackNotificationStub.getNotifications().size());


        final String reviewMessage = "We could not verify your profile. If you have any questions, please contact the Company where you try to verify your " +
                                     "profile tech@onlydust.xyz\\n\\nTemporary we could not verify your profile via doc-free method. Please try again " +
                                     "later or contact the company where you're verifying your profile tech@onlydust.xyz, if error persists.";
        final byte[] sumsubPayloadRejection = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyc-level",
                  "applicantId": "65bd228a2a99e9196014ccc5",
                  "createdAtMs": "2024-02-02 17:23:16.368",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": {
                      "moderationComment": "%s",
                      "clientComment": "User was misled/forced to create this account by a third party\\n\\nThe government database is currently unavailable. We couldn't verify user data with this method.",
                      "reviewAnswer": "RED",
                      "rejectLabels": [
                        "FRAUDULENT_PATTERNS",
                        "CHECK_UNAVAILABLE"
                      ],
                      "reviewRejectType": "FINAL",
                      "buttonIds": [
                        "videoIdentFinalRejection_forcedVerification",
                        "ekycRetry",
                        "videoIdentFinalRejection",
                        "ekycRetry_checkUnavailable"
                      ]
                    },
                  "reviewStatus": "pending",
                  "applicantType": "individual",
                  "correlationId": "0727edcd2fd452f64b7dd9f76516d815",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", reviewMessage, kycId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestRejection = SumsubSignatureVerifier.hmac(sumsubPayloadRejection, sumsubWebhookProperties.getSecret());

        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestRejection)
                .bodyValue(sumsubPayloadRejection)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.kyc.address").isEqualTo("25 AVENUE SAINT LOUIS, ETAGE 2 APT, ST MAUR DES FOSSES, France, 94210")
                .jsonPath("$.kyc.firstName").isEqualTo("ALEXIS")
                .jsonPath("$.kyc.lastName").isEqualTo("BENOLIEL")
                .jsonPath("$.kyc.country").isEqualTo("France")
                .jsonPath("$.kyc.birthdate").isEqualTo("1995-09-19T00:00:00Z")
                .jsonPath("$.kyc.idDocumentNumber").isEqualTo("15AC05169")
                .jsonPath("$.kyc.idDocumentType").isEqualTo("PASSPORT")
                .jsonPath("$.kyc.idDocumentCountryCode").isEqualTo("FRA")
                .jsonPath("$.kyc.validUntil").isEqualTo("2025-04-19T00:00:00Z")
                .jsonPath("$.kyc.usCitizen").isEqualTo(false);
        assertEquals(2, slackNotificationStub.getNotifications().size());
    }

    @Test
    @Order(2)
    void should_verify_company_billing_profile() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();
        final String applicantId = "kyb-" + faker.number().randomNumber();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "company",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.kyb.id").isNotEmpty()
                .jsonPath("$.kyc").isEmpty()
                .jsonPath("$.id").isNotEmpty();

        final UUID billingProfileId = billingProfileRepository.findBillingProfilesForUserId(userId).stream()
                .filter(billingProfileEntity -> billingProfileEntity.getType().equals(BillingProfileEntity.Type.COMPANY))
                .findFirst()
                .orElseThrow()
                .getId();

        final UUID kybId = kybRepository.findByBillingProfileId(billingProfileId).orElseThrow().getId();

        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kybId.toString());
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs(Scenario.STARTED)
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("UNDER_REVIEW_1"));
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("UNDER_REVIEW_1")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("UNDER_REVIEW_2"));
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("UNDER_REVIEW_2")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("VERIFIED"));
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("VERIFIED")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_VERIFIED_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("DONE"));


        final String sumsubApiChecksPath = String.format("/resources/checks/latest?type=COMPANY&applicantId=%s",
                "65bcb9e271117f5b7d4ea23e");
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiChecksPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_CHECKS_RESPONSE_JSON)));


        final byte[] sumsubPayload = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyb-level",
                  "applicantId": "%s",
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
                }""", applicantId, kybId).getBytes(StandardCharsets.UTF_8);
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

        billingProfileVerificationOutboxJob.run();
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.kyb.address").isEqualTo("54 rue du Faubourg Montmartre, 75009 Paris")
                .jsonPath("$.kyb.registrationDate").isEqualTo("2021-12-15T00:00:00Z")
                .jsonPath("$.kyb.country").isEqualTo("France")
                .jsonPath("$.kyb.registrationNumber").isEqualTo("908233638")
                .jsonPath("$.kyb.subjectToEuropeVAT").isEqualTo(true)
                .jsonPath("$.kyb.euVATNumber").isEqualTo("FR26908233638")
                .jsonPath("$.kyb.usEntity").isEqualTo(false);
        assertEquals(3, slackNotificationStub.getNotifications().size());

        final String reviewMessage = "Enter your date of birth exactly as it is on your identity document.\\n\\n - Tax number is incorrect. Provide a correct" +
                                     " tax number.\\n - SSN is incorrect. Provide a correct SSN.";
        final byte[] sumsubPayloadRejection = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyb-level",
                  "applicantId": "%s",
                  "createdAtMs": "2024-02-02 17:23:16.368",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": {
                      "moderationComment": "%s",
                      "clientComment": "The date of birth on the profile does not match document data.\\n\\n - Incorrect tax number. A new tax number has been requested.\\n - Incorrect SSN. A new SSN has been requested.",
                      "reviewAnswer": "RED",
                      "rejectLabels": [
                        "PROBLEMATIC_APPLICANT_DATA",
                        "INCORRECT_SOCIAL_NUMBER"
                      ],
                      "reviewRejectType": "RETRY",
                      "buttonIds": [
                        "dbNetChecks_incorrectTin",
                        "dbNetChecks_incorrectSsn",
                        "dataMismatch",
                        "dataMismatch_dateOfBirth",
                        "dbNetChecks"
                      ]
                    },
                  "reviewStatus": "pending",
                  "applicantType": "company",
                  "correlationId": "0727edcd2fd452f64b7dd9f76516d815",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", applicantId, reviewMessage, kybId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestRejection = SumsubSignatureVerifier.hmac(sumsubPayloadRejection, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestRejection)
                .bodyValue(sumsubPayloadRejection)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.kyb.address").isEqualTo("54 rue du Faubourg Montmartre, 75009 Paris")
                .jsonPath("$.kyb.registrationDate").isEqualTo("2021-12-15T00:00:00Z")
                .jsonPath("$.kyb.country").isEqualTo("France")
                .jsonPath("$.kyb.registrationNumber").isEqualTo("908233638")
                .jsonPath("$.kyb.subjectToEuropeVAT").isEqualTo(true)
                .jsonPath("$.kyb.euVATNumber").isEqualTo("FR26908233638")
                .jsonPath("$.kyb.usEntity").isEqualTo(false);
        assertEquals(4, slackNotificationStub.getNotifications().size());

        final byte[] sumsubPayloadChildrenKycUnderReview = String.format("""
                {
                  "applicantId": "65d34febc2b0cd19e02aa971",
                  "inspectionId": "65d34febc2b0cd19e02aa972",
                  "applicantType": "individual",
                  "applicantMemberOf": [
                    {
                      "applicantId": "%s"
                    }
                  ],
                  "correlationId": "6a444c189bf5b6252e5dc486220f7bd8",
                  "levelName": "od-kyc-production",
                  "sandboxMode": false,
                  "externalUserId": "beneficiary-random-92a161bd-56b9-4043-9c3c-9d24b94691c6",
                  "type": "applicantPending",
                  "reviewStatus": "pending",
                  "createdAt": "2024-02-19 13:13:19+0000",
                  "createdAtMs": "2024-02-19 13:13:19.411",
                  "sourceKey": "production",
                  "clientId": "onlydust"
                }""", applicantId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestChildrenKycUnderReview = SumsubSignatureVerifier.hmac(sumsubPayloadChildrenKycUnderReview,
                sumsubWebhookProperties.getSecret());

        final byte[] sumsubPayloadChildrenKycRejection = String.format("""
                {
                  "applicantId": "75d34febc2b0cd19e02aa971",
                  "inspectionId": "65d34febc2b0cd19e02aa972",
                  "applicantType": "individual",
                  "applicantMemberOf": [
                    {
                      "applicantId": "%s"
                    }
                  ],
                  "correlationId": "6a444c189bf5b6252e5dc486220f7bd8",
                  "levelName": "od-kyc-production",
                  "sandboxMode": false,
                  "externalUserId": "beneficiary-random-92a161bd-56b9-4043-9c3c-9d24b94691c6",
                  "type": "applicantReviewed",
                    "reviewResult": {
                      "reviewAnswer": "RED",
                      "rejectLabels": [
                        "BAD_PROOF_OF_ADDRESS"
                      ],
                      "reviewRejectType": "RETRY",
                      "buttonIds": [
                        "proofOfAddress_issueDate",
                        "proofOfAddress_listOfDocs",
                        "proofOfAddress",
                        "proofOfAddress_fullAddress"
                      ]
                    },
                   "reviewStatus": "completed",
                  "createdAt": "2024-02-19 13:13:19+0000",
                  "createdAtMs": "2024-02-19 13:13:19.411",
                  "sourceKey": "production",
                  "clientId": "onlydust"
                }""", applicantId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestChildrenKycRejection = SumsubSignatureVerifier.hmac(sumsubPayloadChildrenKycRejection,
                sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestChildrenKycRejection)
                .bodyValue(sumsubPayloadChildrenKycRejection)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestChildrenKycUnderReview)
                .bodyValue(sumsubPayloadChildrenKycUnderReview)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();

        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("REJECTED");
        assertEquals(6, slackNotificationStub.getNotifications().size());
    }

    @Test
    @Order(10)
    void should_get_company_billing_profile_given_a_closed_children_kyc_fixed() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();
        final String applicantId = "kyb-" + faker.number().randomNumber();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "company2",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("company2")
                .jsonPath("$.type").isEqualTo("COMPANY")
                .jsonPath("$.status").isEqualTo("NOT_STARTED")
                .jsonPath("$.kyb.id").isNotEmpty()
                .jsonPath("$.kyc").isEmpty()
                .jsonPath("$.id").isNotEmpty();

        final UUID billingProfileId = billingProfileRepository.findBillingProfilesForUserId(userId).stream()
                .filter(billingProfileEntity -> billingProfileEntity.getType().equals(BillingProfileEntity.Type.COMPANY))
                .findFirst()
                .orElseThrow()
                .getId();

        final UUID kybId = kybRepository.findByBillingProfileId(billingProfileId).orElseThrow().getId();
        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kybId.toString());
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
//                .inScenario("KYB")
//                .whenScenarioStateIs(Scenario.STARTED)
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted(applicantId))));
//                .willSetStateTo("UNDER_REVIEW_1"));
//        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
//                .inScenario("KYB")
//                .whenScenarioStateIs("UNDER_REVIEW_1")
//                .withHeader("Content-Type", equalTo("application/json"))
//                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
//                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted(applicantId)))
//                .willSetStateTo("VERIFIED"));
//
//        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
//                .inScenario("KYB")
//                .whenScenarioStateIs("VERIFIED")
//                .withHeader("Content-Type", equalTo("application/json"))
//                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
//                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_VERIFIED_RESPONSE_JSON.formatted(applicantId)))
//                .willSetStateTo("DONE"));

        final String sumsubApiChecksPath = String.format("/resources/checks/latest?type=COMPANY&applicantId=%s",
                applicantId);
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiChecksPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_CHECKS_RESPONSE_JSON)));


        final byte[] sumsubPayload = String.format("""
                {
                  "type": "applicantPending",
                  "clientId": "onlydust",
                  "levelName": "basic-kyb-level",
                  "applicantId": "%s",
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
                }""", applicantId, kybId).getBytes(StandardCharsets.UTF_8);
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

        billingProfileVerificationOutboxJob.run();
        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.kyb.address").isEqualTo("54 rue du Faubourg Montmartre, 75009 Paris")
                .jsonPath("$.kyb.registrationDate").isEqualTo("2021-12-15T00:00:00Z")
                .jsonPath("$.kyb.country").isEqualTo("France")
                .jsonPath("$.kyb.registrationNumber").isEqualTo("908233638")
                .jsonPath("$.kyb.subjectToEuropeVAT").isEqualTo(true)
                .jsonPath("$.kyb.euVATNumber").isEqualTo("FR26908233638")
                .jsonPath("$.kyb.usEntity").isEqualTo(false);

        final byte[] sumsubPayloadChildrenKycClosed = String.format("""
                {
                     "type": "applicantReviewed",
                     "clientId": "onlydust",
                     "className": "onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO",
                     "levelName": "od-kyc-production",
                     "applicantId": "65d8aef77522922410a6d55c",
                     "createdAtMs": "2024-02-23 15:21:13.509",
                     "sandboxMode": false,
                     "inspectionId": "65d8aef77522922410a6d55d",
                     "reviewResult": {
                       "buttonIds": [
                         "spam",
                         "dataMismatch_dateOfBirth",
                         "dataMismatch"
                       ],
                       "rejectLabels": [
                         "SPAM",
                         "PROBLEMATIC_APPLICANT_DATA"
                       ],
                       "reviewAnswer": "RED",
                       "clientComment": "Verification rejected due to excessive file uploads.\\n\\nThe date of birth on the profile does not match document data.",
                       "reviewRejectType": "FINAL",
                       "moderationComment": "You have exceeded the number of times to upload your documents. If you have any questions, please contact the Company where you try to verify your profile tech@onlydust.xyz\\n\\nEnter your date of birth exactly as it is on your identity document."
                     },
                     "reviewStatus": "completed",
                     "applicantType": "individual",
                     "correlationId": "983acfc725ea215c64666516a4781125",
                     "externalUserId": "beneficiary-random-d9a96660-8d32-4958-a6f7-40c19aa946cc",
                     "applicantActionId": null,
                     "applicantMemberOf": [
                       {
                         "applicantId": "%s"
                       }
                     ],
                     "previousLevelName": null,
                     "videoIdentReviewStatus": null,
                     "externalApplicantActionId": null
                 }""", applicantId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestChildrenKycClosed = SumsubSignatureVerifier.hmac(sumsubPayloadChildrenKycClosed,
                sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestChildrenKycClosed)
                .bodyValue(sumsubPayloadChildrenKycClosed)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();

        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("CLOSED");

        final byte[] sumsubPayloadChildrenKycVerified = String.format("""
                {
                     "type": "applicantReviewed",
                     "clientId": "onlydust",
                     "className": "onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO",
                     "levelName": "od-kyc-production",
                     "applicantId": "65d8aef77522922410a6d55c",
                     "createdAtMs": "2024-02-23 15:48:59.184",
                     "sandboxMode": false,
                     "inspectionId": "65d8aef77522922410a6d55d",
                     "reviewResult": {
                       "buttonIds": null,
                       "rejectLabels": null,
                       "reviewAnswer": "GREEN",
                       "clientComment": null,
                       "reviewRejectType": null,
                       "moderationComment": null
                     },
                     "reviewStatus": "completed",
                     "applicantType": "individual",
                     "correlationId": "e0c67145782e997a26544572285b3abd",
                     "externalUserId": "beneficiary-random-d9a96660-8d32-4958-a6f7-40c19aa946cc",
                     "applicantActionId": null,
                     "applicantMemberOf": [
                       {
                         "applicantId": "%s"
                       }
                     ],
                     "previousLevelName": null,
                     "videoIdentReviewStatus": null,
                     "externalApplicantActionId": null
                 }""", applicantId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestChildrenKycVerified = SumsubSignatureVerifier.hmac(sumsubPayloadChildrenKycVerified,
                sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestChildrenKycVerified)
                .bodyValue(sumsubPayloadChildrenKycVerified)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();

        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW");

        final byte[] sumsubPayloadKybVerified = String.format("""
                {
                  "type": "applicantReviewed",
                  "clientId": "onlydust",
                  "levelName": "basic-kyb-level",
                  "applicantId": "%s",
                  "createdAtMs": "2024-02-02 17:23:16.368",
                  "sandboxMode": false,
                  "inspectionId": "65bd228a2a99e9196014ccc6",
                  "reviewResult": {
                       "buttonIds": null,
                       "rejectLabels": null,
                       "reviewAnswer": "GREEN",
                       "clientComment": null,
                       "reviewRejectType": null,
                       "moderationComment": null
                     },
                  "reviewStatus": "completed",
                  "applicantType": "company",
                  "correlationId": "0727edcd2fd452f64b7dd9f76516d815",
                  "externalUserId": "%s",
                  "applicantActionId": null,
                  "applicantMemberOf": null,
                  "previousLevelName": null,
                  "videoIdentReviewStatus": null,
                  "externalApplicantActionId": null
                }""", applicantId, kybId).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestKybVerified = SumsubSignatureVerifier.hmac(sumsubPayloadKybVerified, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestKybVerified)
                .bodyValue(sumsubPayloadKybVerified)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        billingProfileVerificationOutboxJob.run();

        client.get()
                .uri(BILLING_PROFILES_GET_BY_ID.formatted(billingProfileId))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("VERIFIED");

    }

    private static final String SUMSUB_INDIVIDUAL_RESPONSE_JSON = """
            {
                "id": "65bd228a2a99e9196014ccc5",
                "createdAt": "2024-02-02 17:12:42",
                "key": "CFLGUPVDEXFEAS",
                "clientId": "onlydust",
                "inspectionId": "65bd228a2a99e9196014ccc6",
                "externalUserId": "9d2e92fd-451d-45c3-840a-7fb2290d0e92",
                "info": {
                    "firstName": "ALEXIS",
                    "firstNameEn": "ALEXIS",
                    "lastName": "BENOLIEL",
                    "lastNameEn": "BENOLIEL",
                    "dob": "1995-09-19",
                    "country": "FRA",
                    "addresses": [
                        {
                            "subStreet": "ETAGE 2 APT",
                            "subStreetEn": "ETAGE 2 APT",
                            "street": "25 AVENUE SAINT LOUIS",
                            "streetEn": "25 AVENUE SAINT LOUIS",
                            "town": "ST MAUR DES FOSSES",
                            "townEn": "ST MAUR DES FOSSES",
                            "postCode": "94210",
                            "country": "FRA",
                            "formattedAddress": "25 AVENUE SAINT LOUIS, ETAGE 2 APT, ST MAUR DES FOSSES, France, 94210"
                        }
                    ],
                    "idDocs": [
                        {
                            "idDocType": "PASSPORT",
                            "country": "FRA",
                            "firstName": "ALEXIS",
                            "firstNameEn": "ALEXIS",
                            "lastName": "BENOLIEL",
                            "lastNameEn": "BENOLIEL",
                            "issuedDate": "2015-04-20",
                            "validUntil": "2025-04-19",
                            "number": "15AC05169",
                            "dob": "1995-09-19",
                            "ocrDocTypes": null,
                            "imageFieldsInfo": null,
                            "mrzLine1": "P<FRABENOLIEL<<ALEXIS<<<<<<<<<<<<<<<<<<<<<<<",
                            "mrzLine2": "15AC051695FRA9509193M2504199<<<<<<<<<<<<<<06"
                        },
                        {
                            "idDocType": "UTILITY_BILL",
                            "country": "FRA",
                            "firstName": "Alexis Benoliel",
                            "firstNameEn": "Alexis Benoliel",
                            "issuedDate": "2024-02-02",
                            "address": {
                                "subStreet": "ETAGE 2 APT",
                                "subStreetEn": "ETAGE 2 APT",
                                "street": "25 AVENUE SAINT LOUIS",
                                "streetEn": "25 AVENUE SAINT LOUIS",
                                "town": "ST MAUR DES FOSSES",
                                "townEn": "ST MAUR DES FOSSES",
                                "postCode": "94210",
                                "country": "FRA",
                                "formattedAddress": "25 AVENUE SAINT LOUIS, ETAGE 2 APT, ST MAUR DES FOSSES, France, 94210",
                                "metadata": null
                            }
                        }
                    ]
                },
                "applicantPlatform": "Web",
                "ipCountry": "FRA",
                "authCode": "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MDcyMTA3ODEsImV4cCI6MTcwNzIxMTM4MSwic3ViIjoiOWQyZTkyZmQtNDUxZC00NWMzLTg0MGEtN2ZiMjI5MGQwZTkyIiwiYXVkIjoib25seWR1c3QiLCJpc3MiOiJTdW1zdWIiLCJhcHBsaWNhbnRJZCI6IjY1YmQyMjhhMmE5OWU5MTk2MDE0Y2NjNSIsImFwcGxpY2FudE5hbWUiOiJBTEVYSVMgQkVOT0xJRUwifQ.nU8YZdevEdjLcxf313dMh9s9GirWgSR2tNHnsCEADtE",
                "agreement": {
                    "createdAt": "2024-02-02 17:12:52",
                    "source": "WebSDK",
                    "targets": [
                        "constConsentEn_v9"
                    ],
                    "privacyNoticeUrl": "https://sumsub.com/privacy-notice-service/"
                },
                "requiredIdDocs": {
                    "docSets": [
                        {
                            "idDocSetType": "IDENTITY",
                            "types": [
                                "ID_CARD",
                                "PASSPORT",
                                "RESIDENCE_PERMIT",
                                "DRIVERS"
                            ],
                            "subTypes": [
                                "FRONT_SIDE",
                                "BACK_SIDE"
                            ]
                        },
                        {
                            "idDocSetType": "SELFIE",
                            "types": [
                                "SELFIE"
                            ],
                            "videoRequired": "passiveLiveness"
                        },
                        {
                            "idDocSetType": "PROOF_OF_RESIDENCE",
                            "types": [
                                "UTILITY_BILL"
                            ],
                            "poaStepSettingsId": "65a64728ee76e62ddabf1a62"
                        },
                        {
                            "idDocSetType": "QUESTIONNAIRE",
                            "questionnaireDefId": "kycCustom"
                        }
                    ]
                },
                "review": {
                    "reviewId": "EBtPv",
                    "attemptId": "EMFPc",
                    "attemptCnt": 1,
                    "elapsedSincePendingMs": 4120816,
                    "elapsedSinceQueuedMs": 4120816,
                    "reprocessing": true,
                    "levelName": "basic-kyc-level",
                    "createDate": "2024-02-02 17:23:16+0000",
                    "reviewDate": "2024-02-02 18:31:57+0000",
                    "reviewResult": {
                        "reviewAnswer": "GREEN"
                    },
                    "reviewStatus": "completed",
                    "priority": 0,
                    "moderatorNames": null
                },
                "lang": "fr",
                "type": "individual",
                "questionnaires": [
                    {
                        "id": "odKycUsPersonDevelop",
                        "sections": {
                            "personalStatusVerifi": {
                                "items": {
                                    "areYouConsideredAUsP": {
                                        "value": "no"
                                    }
                                }
                            }
                        }
                    }
                ],
                "riskLabels": {
                    "attemptId": "dvGnB",
                    "createdAt": "2024-02-02 18:31:55"
                }
            }
            """;

    private static final String SUMSUB_COMPANY_VERIFIED_RESPONSE_JSON = """
            {
                "id": "%s",
                "createdAt": "2024-02-02 09:46:10",
                "key": "CFLGUPVDEXFEAS",
                "clientId": "onlydust",
                "inspectionId": "65bcb9e271117f5b7d4ea23f",
                "externalUserId": "level-5bf2c215-028f-4d34-84bd-8e4d1c3c9c84",
                "info": {
                    "companyInfo": {
                        "companyName": "WAGMI",
                        "registrationNumber": "908233638",
                        "country": "FRA",
                        "address": {},
                        "beneficiaries": [
                            {
                                "applicantId": "65bcbd198405da4bad4565e6",
                                "positions": [
                                    "shareholder",
                                    "director"
                                ],
                                "type": "ubo",
                                "inRegistry": false,
                                "imageIds": null,
                                "applicant": null,
                                "shareSize": null
                            },
                            {
                                "applicantId": "65bcbd47a039895f4c2f9c15",
                                "positions": [
                                    "shareholder",
                                    "director"
                                ],
                                "type": "ubo",
                                "inRegistry": false,
                                "imageIds": null,
                                "applicant": null,
                                "shareSize": null
                            }
                        ]
                    }
                },
                "applicantPlatform": "Web",
                "ipCountry": "FRA",
                "authCode": "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MDcyMzU3ODMsImV4cCI6MTcwNzIzNjM4Mywic3ViIjoibGV2ZWwtNWJmMmMyMTUtMDI4Zi00ZDM0LTg0YmQtOGU0ZDFjM2M5Yzg0IiwiYXVkIjoib25seWR1c3QiLCJpc3MiOiJTdW1zdWIiLCJhcHBsaWNhbnRJZCI6IjY1YmNiOWUyNzExMTdmNWI3ZDRlYTIzZSIsImFwcGxpY2FudE5hbWUiOiJXQUdNSSJ9.VbUEvzvFY7sjMCDcYf4kdE-sRBDMPboa3mnpUuZhMks",
                "agreement": {
                    "createdAt": "2024-02-02 09:46:14",
                    "source": "WebSDK",
                    "targets": [
                        "constConsentEn_v9"
                    ],
                    "privacyNoticeUrl": "https://sumsub.com/privacy-notice-service/"
                },
                "requiredIdDocs": {
                    "docSets": [
                        {
                            "idDocSetType": "COMPANY",
                            "types": [
                                "COMPANY_DOC"
                            ],
                            "steps": [
                                {
                                    "name": "company",
                                    "minDocsCnt": 1,
                                    "applicantLevelName": "basic-kyb-level",
                                    "idDocTypes": [
                                        "COMPANY_DOC"
                                    ],
                                    "idDocSubTypes": [
                                        "INCORPORATION_CERT"
                                    ],
                                    "fields": [
                                        {
                                            "name": "companyName",
                                            "required": true
                                        },
                                        {
                                            "name": "country",
                                            "required": true
                                        },
                                        {
                                            "name": "registrationNumber",
                                            "required": true
                                        }
                                    ],
                                    "customFields": null,
                                    "captureMode": null
                                },
                                {
                                    "name": "ubos",
                                    "minDocsCnt": 0,
                                    "applicantLevelName": "basic-kyc-level",
                                    "idDocTypes": null,
                                    "idDocSubTypes": null,
                                    "fields": [
                                        {
                                            "name": "firstName",
                                            "required": true
                                        },
                                        {
                                            "name": "lastName",
                                            "required": true
                                        },
                                        {
                                            "name": "middleName",
                                            "required": false
                                        },
                                        {
                                            "name": "dob",
                                            "required": false
                                        },
                                        {
                                            "name": "email",
                                            "required": true
                                        },
                                        {
                                            "name": "phone",
                                            "required": true
                                        }
                                    ],
                                    "customFields": null,
                                    "captureMode": null
                                }
                            ]
                        },
                        {
                            "idDocSetType": "QUESTIONNAIRE",
                            "questionnaireDefId": "kybCustom"
                        }
                    ]
                },
                "review": {
                    "reviewId": "AVhry",
                    "attemptId": "JmlAH",
                    "attemptCnt": 2,
                    "elapsedSincePendingMs": 86184382,
                    "elapsedSinceQueuedMs": 2411178,
                    "reprocessing": true,
                    "levelName": "basic-kyb-level",
                    "createDate": "2024-02-05 11:40:27+0000",
                    "reviewDate": "2024-02-06 11:36:51+0000",
                    "reviewResult": {
                        "reviewAnswer": "GREEN"
                    },
                    "reviewStatus": "completed",
                    "priority": 0,
                    "moderatorNames": null
                },
                "lang": "en",
                "type": "company",
                "questionnaires": [
                    {
                        "id": "odKybFormDevelop",
                        "sections": {
                            "usAndEuropeanComplia": {
                                "items": {
                                    "whatIsYourEuVatRegis": {
                                        "value": "FR26908233638"
                                    },
                                    "isYourEntityAUsPerso": {
                                        "value": "no"
                                    },
                                    "isYourCompanySubject": {
                                        "value": "yes"
                                    }
                                }
                            }
                        }
                    }
                ]
            }""";
    private static final String SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON = """
            {
                "id": "%s",
                "createdAt": "2024-02-02 09:46:10",
                "key": "CFLGUPVDEXFEAS",
                "clientId": "onlydust",
                "inspectionId": "65bcb9e271117f5b7d4ea23f",
                "externalUserId": "level-5bf2c215-028f-4d34-84bd-8e4d1c3c9c84",
                "info": {
                    "companyInfo": {
                        "companyName": "WAGMI",
                        "registrationNumber": "908233638",
                        "country": "FRA",
                        "address": {},
                        "beneficiaries": [
                            {
                                "applicantId": "65bcbd198405da4bad4565e6",
                                "positions": [
                                    "shareholder",
                                    "director"
                                ],
                                "type": "ubo",
                                "inRegistry": false,
                                "imageIds": null,
                                "applicant": null,
                                "shareSize": null
                            },
                            {
                                "applicantId": "65bcbd47a039895f4c2f9c15",
                                "positions": [
                                    "shareholder",
                                    "director"
                                ],
                                "type": "ubo",
                                "inRegistry": false,
                                "imageIds": null,
                                "applicant": null,
                                "shareSize": null
                            }
                        ]
                    }
                },
                "applicantPlatform": "Web",
                "ipCountry": "FRA",
                "authCode": "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MDcyMzU3ODMsImV4cCI6MTcwNzIzNjM4Mywic3ViIjoibGV2ZWwtNWJmMmMyMTUtMDI4Zi00ZDM0LTg0YmQtOGU0ZDFjM2M5Yzg0IiwiYXVkIjoib25seWR1c3QiLCJpc3MiOiJTdW1zdWIiLCJhcHBsaWNhbnRJZCI6IjY1YmNiOWUyNzExMTdmNWI3ZDRlYTIzZSIsImFwcGxpY2FudE5hbWUiOiJXQUdNSSJ9.VbUEvzvFY7sjMCDcYf4kdE-sRBDMPboa3mnpUuZhMks",
                "agreement": {
                    "createdAt": "2024-02-02 09:46:14",
                    "source": "WebSDK",
                    "targets": [
                        "constConsentEn_v9"
                    ],
                    "privacyNoticeUrl": "https://sumsub.com/privacy-notice-service/"
                },
                "requiredIdDocs": {
                    "docSets": [
                        {
                            "idDocSetType": "COMPANY",
                            "types": [
                                "COMPANY_DOC"
                            ],
                            "steps": [
                                {
                                    "name": "company",
                                    "minDocsCnt": 1,
                                    "applicantLevelName": "basic-kyb-level",
                                    "idDocTypes": [
                                        "COMPANY_DOC"
                                    ],
                                    "idDocSubTypes": [
                                        "INCORPORATION_CERT"
                                    ],
                                    "fields": [
                                        {
                                            "name": "companyName",
                                            "required": true
                                        },
                                        {
                                            "name": "country",
                                            "required": true
                                        },
                                        {
                                            "name": "registrationNumber",
                                            "required": true
                                        }
                                    ],
                                    "customFields": null,
                                    "captureMode": null
                                },
                                {
                                    "name": "ubos",
                                    "minDocsCnt": 0,
                                    "applicantLevelName": "basic-kyc-level",
                                    "idDocTypes": null,
                                    "idDocSubTypes": null,
                                    "fields": [
                                        {
                                            "name": "firstName",
                                            "required": true
                                        },
                                        {
                                            "name": "lastName",
                                            "required": true
                                        },
                                        {
                                            "name": "middleName",
                                            "required": false
                                        },
                                        {
                                            "name": "dob",
                                            "required": false
                                        },
                                        {
                                            "name": "email",
                                            "required": true
                                        },
                                        {
                                            "name": "phone",
                                            "required": true
                                        }
                                    ],
                                    "customFields": null,
                                    "captureMode": null
                                }
                            ]
                        },
                        {
                            "idDocSetType": "QUESTIONNAIRE",
                            "questionnaireDefId": "kybCustom"
                        }
                    ]
                },
                "review": {
                    "reviewId": "AVhry",
                    "attemptId": "JmlAH",
                    "attemptCnt": 2,
                    "elapsedSincePendingMs": 86184382,
                    "elapsedSinceQueuedMs": 2411178,
                    "reprocessing": true,
                    "levelName": "basic-kyb-level",
                    "createDate": "2024-02-05 11:40:27+0000",
                    "reviewDate": "2024-02-06 11:36:51+0000",
                    "reviewResult": null,
                    "reviewStatus": "pending",
                    "priority": 0,
                    "moderatorNames": null
                },
                "lang": "en",
                "type": "company",
                "questionnaires": [
                    {
                        "id": "odKybFormDevelop",
                        "sections": {
                            "usAndEuropeanComplia": {
                                "items": {
                                    "whatIsYourEuVatRegis": {
                                        "value": "FR26908233638"
                                    },
                                    "isYourEntityAUsPerso": {
                                        "value": "no"
                                    },
                                    "isYourCompanySubject": {
                                        "value": "yes"
                                    }
                                }
                            }
                        }
                    }
                ]
            }""";

    private static final String SUMSUB_COMPANY_CHECKS_RESPONSE_JSON = """
            {
                "checks": [
                    {
                        "answer": "GREEN",
                        "checkType": "COMPANY",
                        "createdAt": "2024-02-05 10:11:23",
                        "id": "46440094-7be0-4cd4-baff-eb547bc2e199",
                        "companyCheckInfo": {
                            "companyName": "WAGMI",
                            "companyNumber": "SIREN: 908 233 638 / SIRET: 908 233 638 00026",
                            "status": "Active",
                            "type": "Société par actions simplifiée (simplified joint-stock company)",
                            "incorporatedOn": "2021-12-15 00:00:00",
                            "officeAddress": "54 rue du Faubourg Montmartre, 75009 Paris",
                            "licenseInfo": {
                                "issuedDate": "2021-12-15 00:00:00"
                            }
                        }
                    }
                ]
            }""";
}
