package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.onlydust.api.sumsub.api.client.adapter.SumsubApiClientAdapter;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.KybRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.KycRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper.SumsubMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;

@TagAccounting
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BillingProfileVerificationsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubWebhookProperties sumsubWebhookProperties;
    @Autowired
    SumsubClientProperties sumsubClientProperties;
    @Autowired
    SlackApiAdapter slackApiAdapter;
    @Autowired
    KycRepository kycRepository;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    CustomerIOProperties customerIOProperties;

    @Test
    @Order(1)
    void should_verify_individual_billing_profile() throws InterruptedException {
        // Given
        Mockito.reset(slackApiAdapter);
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false);
        final var userId = authenticatedUser.user().getId();
        final String jwt = authenticatedUser.jwt();

        MutableObject<UUID> billingProfileId = new MutableObject<>();

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
                .jsonPath("$.id").value(id -> billingProfileId.setValue(UUID.fromString(id)), String.class);

        final UUID kycId = kycRepository.findByBillingProfileId(billingProfileId.getValue()).orElseThrow().id();

        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kycId.toString());
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
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

        Mockito.verify(slackApiAdapter).onBillingProfileUpdated(Mockito.any());

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
                  "reviewStatus": "completed",
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
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("CLOSED")
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
        Mockito.verify(slackApiAdapter, times(2)).onBillingProfileUpdated(Mockito.any());

        Thread.sleep(1000L);

        final String title = "Billing profile individual verification closed";
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getVerificationClosedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(userId.toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.title",
                                equalTo(title)))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo("We regret to inform you that we cannot proceed with your " +
                                                                                                "verification request " +
                                                                                                "on your billing profile individual, as it has failed." +
                                                                                                " If you require further information or assistance, please " +
                                                                                                "do" +
                                                                                                " not hesitate to contact us.")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(login)))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Contact us")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link", equalTo("https://develop-app.onlydust.com/" + ("settings/billing/%s" +
                                                                                                                                       "/general-information"
                        ).formatted(billingProfileId))))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo(title)))
        );
    }

    @Test
    @Order(2)
    void should_verify_company_billing_profile() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false);
        final String jwt = authenticatedUser.jwt();
        final String applicantId = "kyb-" + faker.number().randomNumber();

        MutableObject<UUID> billingProfileId = new MutableObject<>();

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
                .jsonPath("$.id").value(id -> billingProfileId.setValue(UUID.fromString(id)), String.class);

        final UUID kybId = kybRepository.findByBillingProfileId(billingProfileId.getValue()).orElseThrow().id();

        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kybId.toString());
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs(Scenario.STARTED)
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("UNDER_REVIEW_1"));
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("UNDER_REVIEW_1")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("UNDER_REVIEW_2"));
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("UNDER_REVIEW_2")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("VERIFIED"));
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
                .inScenario("KYB")
                .whenScenarioStateIs("VERIFIED")
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_VERIFIED_RESPONSE_JSON.formatted("65bcb9e271117f5b7d4ea23e")))
                .willSetStateTo("DONE"));


        final String sumsubApiChecksPath = String.format("/resources/checks/latest?type=COMPANY&applicantId=%s",
                "65bcb9e271117f5b7d4ea23e");
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiChecksPath))
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
        Mockito.verify(slackApiAdapter, times(3)).onBillingProfileUpdated(Mockito.any());

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
        Mockito.verify(slackApiAdapter, times(4)).onBillingProfileUpdated(Mockito.any());

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
                      "moderationComment": "%s",
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
                }""", applicantId, reviewMessage).getBytes(StandardCharsets.UTF_8);
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
        billingProfileVerificationOutboxJob.run();
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
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("REJECTED");
        Mockito.verify(slackApiAdapter, times(6)).onBillingProfileUpdated(Mockito.any());

        final byte[] sumsubPayloadKybRejection = String.format("""
                {
                  "applicantId": "%s",
                  "inspectionId": "65d34febc2b0cd19e02aa972",
                  "applicantType": "company",
                  "applicantMemberOf": null,
                  "correlationId": "6a444c189bf5b6252e5dc486220f7bd8",
                  "levelName": "od-kyb-production",
                  "sandboxMode": false,
                  "externalUserId": "%s",
                  "type": "applicantReviewed",
                    "reviewResult": {
                      "moderationComment": "%s",
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
                }""", "65bcb9e271117f5b7d4ea23e", kybId, reviewMessage).getBytes(StandardCharsets.UTF_8);
        final String sumsubDigestKybRejection = SumsubSignatureVerifier.hmac(sumsubPayloadKybRejection,
                sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, sumsubDigestKybRejection)
                .bodyValue(sumsubPayloadKybRejection)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        billingProfileVerificationOutboxJob.run();

        final String title = "Billing profile company verification rejected";
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getVerificationRejectedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(authenticatedUser.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.title",
                                equalTo(title)))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo("We regret to inform you that your billing named " +
                                                                                                "<b>\"company\"</b> has been rejected." +
                                                                                                " We require additional actions from you t for verification " +
                                                                                                "in order to complete the verification:")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(login)))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("Resume verification")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link", equalTo("https://develop-app.onlydust.com/" + ("settings/billing/%s" +
                                                                                                                                       "/general-information"
                        ).formatted(billingProfileId))))
                        .withRequestBody(matchingJsonPath("$.message_data.reason", equalTo("""
                                Enter your date of birth exactly as it is on your identity document.
                                
                                 - Tax number is incorrect. Provide a correct tax number.
                                 - SSN is incorrect. Provide a correct SSN.""")))
                        .withRequestBody(matchingJsonPath("$.message_data.hasMoreInformation", equalTo("true")))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(authenticatedUser.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo(title)))
        );
    }

    @Test
    @Order(10)
    void should_get_company_billing_profile_given_a_closed_children_kyc_fixed() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();
        final String applicantId = "kyb-" + faker.number().randomNumber();
        MutableObject<UUID> billingProfileId = new MutableObject<>();

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
                .jsonPath("$.id").value(id -> billingProfileId.setValue(UUID.fromString(id)), String.class);

        final UUID kybId = kybRepository.findByBillingProfileId(billingProfileId.getValue()).orElseThrow().id();
        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kybId.toString());
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
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
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiChecksPath))
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

    @Test
    void should_send_kyc_verification_email_to_kyb_ubo() throws InterruptedException {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final String jwt = userAuthHelper.signUpUser(githubUserId, login, avatarUrl, false).jwt();
        final String applicantId = "kyc-" + faker.number().randomNumber();
        MutableObject<UUID> billingProfileId = new MutableObject<>();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwt)
                .bodyValue("""
                        {
                          "name": "company3",
                          "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").value(id -> billingProfileId.setValue(UUID.fromString(id)), String.class);

        final UUID kybId = kybRepository.findByBillingProfileId(billingProfileId.getValue()).orElseThrow().id();

        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                kybId.toString());
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_UNDER_REVIEW_RESPONSE_JSON.formatted("66b9b153689cf127e92d4e12"))));

        final String sumsubApiChecksPath = String.format("/resources/checks/latest?type=COMPANY&applicantId=%s",
                "66b9b153689cf127e92d4e12");
        sumsubWireMockServer.stubFor(get(urlEqualTo(sumsubApiChecksPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_COMPANY_CHECKS_RESPONSE_JSON)));

        final byte[] parentSumsubPayload = String.format("""
                {
                    "type": "applicantCreated",
                    "@type": "SumsubWebhookEventDTO",
                    "clientId": "onlydust",
                    "levelName": "od-kyb-staging",
                    "applicantId": "66b9b153689cf127e92d4e12",
                    "createdAtMs": "2024-08-12 06:53:07.672",
                    "sandboxMode": true,
                    "inspectionId": "66b9b153689cf127e92d4e12",
                    "reviewResult": null,
                    "reviewStatus": "init",
                    "applicantType": "company",
                    "correlationId": "81690bd7a3c547e2e876802cd8ab64c6",
                    "externalUserId": "%s",
                    "applicantActionId": null,
                    "applicantMemberOf": null,
                    "previousLevelName": null,
                    "videoIdentReviewStatus": null,
                    "externalApplicantActionId": null
                }""", kybId.toString()).getBytes(StandardCharsets.UTF_8);
        final String parentSumsubDigest = SumsubSignatureVerifier.hmac(parentSumsubPayload, sumsubWebhookProperties.getSecret());

        // When
        client.post()
                .uri(getApiURI("/api/v1/sumsub/webhook"))
                .header(X_OD_API, sumsubWebhookProperties.getOdApiHeader())
                .header(X_SUMSUB_PAYLOAD_DIGEST, parentSumsubDigest)
                .bodyValue(parentSumsubPayload)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();


        // Given
        sumsubWireMockServer.stubFor(get(urlEqualTo("/resources/applicants/%s/one".formatted(applicantId)))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_GET_APPLICANT_BY_APPLICANT_ID_JSON))
        );
        sumsubWireMockServer.stubFor(post(urlEqualTo("/resources/sdkIntegrations/levels/%s/websdkLink?ttlInSecs=7257600&externalUserId=%s&lang=en"
                .formatted(sumsubClientProperties.getKycLevel(), "beneficiary-random-73b1d9ad-cd47-4e0b-9e4c-b64b9b61da5e")))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_GET_KYC_SDK_LINK))
        );


        final byte[] sumsubPayload = String.format("""
                {
                     "type": "applicantCreated",
                     "@type": "SumsubWebhookEventDTO",
                     "clientId": "onlydust",
                     "levelName": "od-kyc-staging",
                     "applicantId": "%s",
                     "createdAtMs": "2024-08-12 06:54:14.537",
                     "sandboxMode": true,
                     "inspectionId": "66b9b196689cf127e92d52e9",
                     "reviewResult": null,
                     "reviewStatus": "init",
                     "applicantType": "individual",
                     "correlationId": "70e28ade84e1fac76535e814b6de2777",
                     "externalUserId": "beneficiary-random-73b1d9ad-cd47-4e0b-9e4c-b64b9b61da5e",
                     "applicantActionId": null,
                     "applicantMemberOf": [
                       {
                         "applicantId": "66b9b153689cf127e92d4e12"
                       }
                     ],
                     "previousLevelName": null,
                     "videoIdentReviewStatus": null,
                     "externalApplicantActionId": null
                 }""", applicantId).getBytes(StandardCharsets.UTF_8);
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
        Thread.sleep(1000L);

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id",
                                equalTo(customerIOProperties.getKycIndividualVerificationEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data", equalToJson("""
                                                              {
                                                               "title" : "Company beneficiary",
                                                                "description" : "We need to verify your beneficiary identity to validate your company's billing profile company3",
                                                                "username" : "Jean Michel",
                                                                "button": {
                                                                  "text": "Proceed to verification",
                                                                  "link": "https://in.sumsub.com/websdk/p/sbx_MKsda5Lftfbug2Gg"
                                                                },
                                                               "hasMoreInformation": true
                                                              }
                                """, true, false)))
                        .withRequestBody(matchingJsonPath("$.to", equalTo("test@onlydust.xyz")))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Verify your identity to validate your company")))
        );
    }

    @Autowired
    SumsubMapper sumsubMapper;

    @Test
    void should_map_every_rejection_reasons() {
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(null, null)).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(null, List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_expiredId"), null)).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "additionalPages_anotherSide"), List.of(
                "DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_copyOfIdDoc"), List.of("BAD_PROOF_OF_IDENTITY"
        ))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_damagedId"), List.of("DOCUMENT_DAMAGED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_digitalId"), List.of("DIGITAL_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_expiredId"), List.of("EXPIRATION_DATE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_invalidId"), List.of("ID_INVALID"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_notFullNameOrDob"), List.of(
                "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_withoutFace"), List.of("BAD_PROOF_OF_IDENTITY"
        ))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_wrongType"),
                List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument_notFullDob", "badDocument"),
                List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_blackAndWhite"), List.of("BLACK_AND_WHITE",
        "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_dataNotVisible"), List.of("UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR",
        "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor"), List.of("GRAPHIC_EDITOR",
        "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_screenshot"), List.of("SCREENSHOTS",
                "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("company", "company_moreDocs"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("company", "company_notDeteminedBeneficiaries"), List.of(
                "COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("dataMismatch", "dataMismatch_fullName"), List.of(
                "PROBLEMATIC_APPLICANT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("dataMismatch_dateOfBirth", "dataMismatch"), List.of(
                "PROBLEMATIC_APPLICANT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybArticlesOfAssociation"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybLegalBond"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybLegalExistence"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybOfficialPosition"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybIncorrectOwnershipStructure",
        "kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybOther", "kybOther_kybSoleEntrepreneur"), List.of("DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybOther", "kybOther_kybSoleEntrepreneur"), List.of("DOCUMENT_MISSING",
        "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument", "kybUnsuitableDocument_kybIncompleteDocument"),
         List.of("INCOMPLETE_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"),
         List.of("UNFILLED_ID"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"),
         List.of("UNSUITABLE_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybExpirationDate", "kybUnsuitableDocument"),
         List.of("EXPIRATION_DATE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullAddress"), List.of(
                "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullName"), List.of(
                "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress"), List.of(
                "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress"), List.of(
                "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie", "selfie_selfieLiveness"), List.of("BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "selfie"), List.of("BAD_FACE_MATCHING",
"BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument", "badDocument_expiredId", "badDocument_wrongType"),
         List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badDocument_notFullDob", "badDocument", "badDocument_expiredId"),
         List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor", "badPhoto_screenshot"), List.of(
                "SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor", "badPhoto_screenshot"), List.of(
                "GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_screenshot", "badPhoto_dataNotVisible"), List.of(
                "SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_screenshot", "badPhoto_dataNotVisible"), List.of(
                "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_screenshot", "badPhoto_editedPoa"), List.of(
                "SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "GRAPHIC_EDITOR"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto_sticker", "badPhoto", "badPhoto_screenshot"), List.of(
                "GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybCertificateOfGoodStanding", "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"
        ))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybCertificateOfGoodStanding"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybLegalExistence"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of(
                "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybAdditionalDocuments_kybLegalExistence"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument",
        "kybUnsuitableDocument_kybIncompleteDocument"), List.of("INCOMPLETE_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybIncorrectOwnershipStructure",
        "company", "kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_DEFINED_OWNERSHIP_STRUCTURE", "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs", "company",
"kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybControlAndOwnershipStructure_kybIncorrectOwnershipStructure", "kybControlAndOwnershipStructure"), List.of("COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybOther", "kybOther_kybSoleEntrepreneur",
        "kybOther_kybIncorrectRegistrationNumber"), List.of("COMPANY_INCORRECT_DATA", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument", "company", "company_moreDocs"), List.of(
                "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybExpirationDate", "kybUnsuitableDocument",
        "company"), List.of("EXPIRATION_DATE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullName",
         "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullName", "regulationsViolations"),
         List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress", "proofOfAddress_fullAddress"
        ), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress", "proofOfAddress_fullName"),
         List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs",
         "proofOfAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "fraudulentPatterns", "proofOfAddress"),
         List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress",
        "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "proofOfAddress_fullName")
        , List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate",
         "proofOfAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "selfie", "selfie_selfieLiveness"), List.of(
                "BAD_FACE_MATCHING", "BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "additionalPages_anotherSide", "ekycRetry",
        "ekycRetry_checkUnavailable"), List.of("CHECK_UNAVAILABLE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "badPhoto", "additionalPages_anotherSide",
        "badPhoto_dataNotVisible"), List.of("UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "badPhoto", "additionalPages_anotherSide",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress", "additionalPages_anotherSide",
        "proofOfAddress_fullName"), List.of("DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress", "proofOfAddress_fullAddress",
        "additionalPages_mainPageId"), List.of("DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress_issueDate", "proofOfAddress",
        "additionalPages_anotherSide"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress_listOfDocs", "proofOfAddress",
        "additionalPages_anotherSide"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress_listOfDocs", "proofOfAddress",
        "additionalPages_anotherSide"), List.of("DOCUMENT_PAGE_MISSING", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badDocument_expiredId",
        "badPhoto_dataNotVisible"), List.of("UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badDocument_withoutFace",
        "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_dataNotVisible",
        "badDocument_notFullNameOrDob"), List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_dataNotVisible",
        "badDocument_notFullNameOrDob"), List.of("UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_imageEditor",
        "badDocument_withoutFace"), List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_screenshot",
        "badDocument_notFullNameOrDob"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_screenshot",
         "badDocument_wrongType"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor", "badPhoto_screenshot",
        "badPhoto_dataNotVisible"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor", "proofOfAddress",
        "proofOfAddress_fullName"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badPhoto_imageEditor", "selfie", "selfie_selfieLiveness"),
         List.of("BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_imageEditor",
        "proofOfAddress_fullAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_imageEditor",
        "proofOfAddress_fullName"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_screenshot",
        "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullAddress",
        "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullAddress",
        "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullName",
        "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullName",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "badPhoto_imageEditor",
        "proofOfAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress",
        "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "badPhoto_imageEditor",
        "proofOfAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "badPhoto_screenshot"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto_sticker", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto_sticker", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybCertificateOfGoodStanding", "kybAdditionalDocuments_kybLegalExistence"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybAdditionalDocuments_kybLegalExistence"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybOther", "kybAdditionalDocuments_kybLegalExistence"), List.of("ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybNoDate", "kybUnsuitableDocument"), List.of("INCOMPLETE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybGraphicsEditingSoftware"), List.of(
                "GRAPHIC_EDITOR", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"), List.of("UNFILLED_ID"
, "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
                "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"), List.of(
                "UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument"), List.of("EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybPersonalDocument",
        "kybUnsuitableDocument", "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "NOT_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress", "kybControlAndOwnershipStructure"), List.of("ADDITIONAL_DOCUMENT_REQUIRED",
        "COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybAdditionalDocuments", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybControlAndOwnershipStructure"), List.of(
                "ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument", "kybControlAndOwnershipStructure"), List.of("UNFILLED_ID",
        "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybOther", "company", "company_moreDocs",
        "kybOther_kybIncorrectRegistrationNumber"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_INCORRECT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybOther_kybIncorrectCompanyName", "kybOther", "company",
"company_moreDocs"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_INCORRECT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument", "company", "company_moreDocs",
        "kybUnsuitableDocument_kybUnsignedDocument"), List.of("UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybExpirationDate", "kybOther",
"kybUnsuitableDocument", "kybOther_kybSoleEntrepreneur"), List.of("EXPIRATION_DATE", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybExpirationDate", "kybUnsuitableDocument",
        "company", "company_moreDocs"), List.of("EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullName", "dataMismatch",
        "dataMismatch_fullName"), List.of("BAD_PROOF_OF_ADDRESS", "PROBLEMATIC_APPLICANT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "proofOfAddress_fullName", "dataMismatch_fullName",
        "dataMismatch"), List.of("BAD_PROOF_OF_ADDRESS", "PROBLEMATIC_APPLICANT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "regulationsViolations", "proofOfAddress_fullAddress",
        "regulationsViolations_age"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress", "selfie", "proofOfAddress_fullAddress",
        "selfie_selfieLiveness"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "additionalPages", "proofOfAddress",
        "additionalPages_anotherSide"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "additionalPages", "proofOfAddress",
        "additionalPages_anotherSide"), List.of("DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress", "proofOfAddress_fullName",
        "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "proofOfAddress"
        , "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "proofOfAddress"
        , "proofOfAddress_fullName"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badDocument", "proofOfAddress",
        "badDocument_invalidId"), List.of("BAD_PROOF_OF_ADDRESS", "ID_INVALID"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
                "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
                "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "dataMismatch",
        "dataMismatch_fullName"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "proofOfAddress_fullName",
         "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "selfie",
        "selfie_selfieLiveness"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "proofOfAddress"
        , "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "proofOfAddress"
        , "proofOfAddress_fullName"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "badDocument", "badDocument_withoutFace",
        "selfie"), List.of("BAD_FACE_MATCHING", "BAD_SELFIE", "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "badDocument", "badDocument_withoutFace",
        "selfie"), List.of("BAD_PROOF_OF_IDENTITY", "BAD_FACE_MATCHING", "BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress", "selfie",
        "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_issueDate", "proofOfAddress",
         "selfie"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_listOfDocs", "proofOfAddress"
        , "selfie"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress_listOfDocs", "proofOfAddress_issueDate"
        , "proofOfAddress", "additionalPages_anotherSide"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "proofOfAddress_listOfDocs", "proofOfAddress_issueDate"
        , "proofOfAddress", "additionalPages_anotherSide"), List.of("DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badDocument_expiredId", "badPhoto_screenshot"
        , "badDocument_notFullNameOrDob"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_screenshot", "badDocument_wrongType"
        , "badDocument_notFullNameOrDob"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_screenshot", "badDocument_wrongType"
        , "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "badDocument", "badPhoto_screenshot", "badDocument_wrongType"
        , "badPhoto_dataNotVisible"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_imageEditor",
        "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_screenshot",
        "proofOfAddress_fullAddress", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullName",
        "badPhoto_screenshot", "badPhoto_dataNotVisible"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress",
        "badPhoto_imageEditor", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress_listOfDocs",
        "proofOfAddress", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "badPhoto_imageEditor",
        "proofOfAddress", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "badPhoto_imageEditor",
        "proofOfAddress", "proofOfAddress_fullName"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "proofOfAddress_fullAddress", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "proofOfAddress_fullName", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybLowQuality"), List.of("LOW_QUALITY",
        "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybExpirationDate", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument"), List.of(
                "EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybExpirationDate", "kybUnsuitableDocument", "kybAdditionalDocuments_kybCertificateOfGoodStanding"), List.of("EXPIRATION_DATE"
        , "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybAdditionalDocuments_kybCertificateOfGoodStanding",
        "kybUnsuitableDocument_kybUnsignedDocument"), List.of("UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybAdditionalDocuments_kybLegalExistence",
        "kybUnsuitableDocument_kybUnsignedDocument"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "UNSUITABLE_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybOther",
 "kybAdditionalDocuments_kybCertificateOfGoodStanding", "kybAdditionalDocuments_kybLegalExistence", "kybOther_kybIncorrectRegistrationNumber"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_INCORRECT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybOther",
                "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybOther_kybSoleEntrepreneur", "kybAdditionalDocuments_kybLegalExistence"), List.of(
                "ADDITIONAL_DOCUMENT_REQUIRED", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybAdditionalDocuments_kybLegalExistence"), List.of(
                "EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybIncompleteDocument"), List.of(
                "EXPIRATION_DATE", "INCOMPLETE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybLowQuality"), List.of("EXPIRATION_DATE",
         "LOW_QUALITY", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"), List.of(
                "EXPIRATION_DATE", "UNFILLED_ID", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument"), List.of(
                "EXPIRATION_DATE", "UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress", "kybAdditionalDocuments_kybCertificateOfIncorporation",
        "kybControlAndOwnershipStructure"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybControlAndOwnershipStructure_kybIncorrectOwnershipStructure", "kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybControlAndOwnershipStructure"), List.of("ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_NOT_DEFINED_OWNERSHIP_STRUCTURE",
        "COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor", "proofOfAddress_fullAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_incomplete", "badPhoto_editedPoa"), List.of("INCOMPLETE_DOCUMENT", "UNSATISFACTORY_PHOTOS", "GRAPHIC_EDITOR"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot", "badPhoto_incomplete"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "INCOMPLETE_DOCUMENT"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
        "proofOfAddress_fullName", "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "additionalPages"
        , "proofOfAddress", "additionalPages_anotherSide"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "additionalPages"
        , "proofOfAddress", "additionalPages_anotherSide"), List.of("DOCUMENT_PAGE_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress", "badPhoto_imageEditor"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress", "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor", "proofOfAddress_fullAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor", "proofOfAddress_fullName"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "fraudulentPatterns", "proofOfAddress",
        "fraudulentPatterns_selfieMismatch", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "proofOfAddress_fullName",
         "dataMismatch_fullName", "dataMismatch"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "proofOfAddress_fullName",
         "dataMismatch_fullName", "dataMismatch"), List.of("BAD_PROOF_OF_ADDRESS", "PROBLEMATIC_APPLICANT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress", "regulationsViolations",
        "proofOfAddress_fullAddress", "regulationsViolations_age"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress", "badPhoto_imageEditor"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress", "badPhoto_imageEditor"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
 "proofOfAddress", "badPhoto_screenshot"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "proofOfAddress"
        , "ekycRetry", "ekycRetry_checkUnavailable"), List.of("BAD_PROOF_OF_ADDRESS", "CHECK_UNAVAILABLE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "proofOfAddress"
                , "proofOfAddress_fullName", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "badDocument", "badDocument_withoutFace",
                "selfie", "selfie_selfieLiveness"), List.of("BAD_PROOF_OF_IDENTITY", "BAD_FACE_MATCHING", "BAD_SELFIE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_issueDate", "proofOfAddress",
                "selfie", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_listOfDocs", "proofOfAddress"
        , "selfie", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "proofOfAddress_fullName",
                "badPhoto_screenshot", "dataMismatch_fullName", "dataMismatch"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress",
        "badPhoto_screenshot", "proofOfAddress_fullAddress", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress_listOfDocs",
        "proofOfAddress", "badPhoto_imageEditor", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_issueDate", "proofOfAddress_listOfDocs",
        "proofOfAddress", "proofOfAddress_fullName", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress",
        "badPhoto_screenshot", "proofOfAddress_fullAddress", "badPhoto_dataNotVisible"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "proofOfAddress_issueDate",
        "proofOfAddress", "proofOfAddress_fullName", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto_sticker", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress", "selfie", "selfie_selfieLiveness"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybOther", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybOther_kybSoleEntrepreneur", "kybAdditionalDocuments_kybLegalExistence"),
         List.of("ADDITIONAL_DOCUMENT_REQUIRED", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
                "kybUnsuitableDocument_kybPersonalDocument", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
        "kybAdditionalDocuments_kybLegalExistence"), List.of("UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybOther_kybIncorrectCompanyName",
        "kybUnsuitableDocument_kybExpirationDate", "kybOther", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument"), List.of(
                "EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_INCORRECT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybControlAndOwnershipStructure_kybMissingKYCForDirectors",
                "kybControlAndOwnershipStructure"), List.of("EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybOther", "kybUnsuitableDocument", "kybOther_kybSoleEntrepreneur", "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of(
                "EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybNotADocument",
        "kybUnsuitableDocument_kybNoDate", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
        "kybUnsuitableDocument_kybUnsignedDocument"), List.of("INCOMPLETE_DOCUMENT", "UNFILLED_ID", "NOT_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybAdditionalDocuments", "kybControlAndOwnershipStructure_kybMissingKYCForDirectors", "kybControlAndOwnershipStructure_kybIncorrectControlStructure"
        , "kybControlAndOwnershipStructure", "kybAdditionalDocuments_kybOfficialPosition"), List.of("ADDITIONAL_DOCUMENT_REQUIRED",
        "COMPANY_NOT_DEFINED_STRUCTURE", "COMPANY_NOT_VALIDATED_BENEFICIAL_OWNERS", "COMPANY_NOT_VALIDATED_DIRECTORS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybMissingTranslation", "kybAdditionalDocuments",
        "kybAdditionalDocuments_kybCompanyRegisterExcerpt", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument"), List.of("EXPIRATION_DATE", "UNSUITABLE_DOCUMENT",
"ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
 "badPhoto_imageEditor", "proofOfAddress_fullAddress", "badPhoto_incomplete"), List.of("INCOMPLETE_DOCUMENT", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress",
 "proofOfAddress_fullName", "proofOfAddress_fullAddress", "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress", "badPhoto_imageEditor", "badPhoto_screenshot"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS", "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "badPhoto",
"proofOfAddress", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "badPhoto",
        "proofOfAddress", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress",
        "badPhoto_imageEditor", "selfie", "selfie_selfieLiveness"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "badPhoto", "proofOfAddress_issueDate",
        "proofOfAddress", "badPhoto_imageEditor", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badDocument",
 "proofOfAddress", "proofOfAddress_fullName", "badDocument_invalidId"), List.of("ID_INVALID"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress", "proofOfAddress_fullName", "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "badPhoto", "badDocument",
 "badPhoto_imageEditor", "badDocument_withoutFace", "selfie"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_listOfDocs",
        "proofOfAddress_issueDate", "proofOfAddress", "selfie", "proofOfAddress_fullAddress"), List.of("BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress", "badPhoto_imageEditor",
        "proofOfAddress_fullName", "badPhoto_screenshot", "dataMismatch_fullName", "dataMismatch"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS",
        "SCREENSHOTS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("badPhoto", "proofOfAddress_listOfDocs", "badDocument", "proofOfAddress",
         "badPhoto_screenshot", "badDocument_wrongType", "badPhoto_dataNotVisible"), List.of("BAD_PROOF_OF_ADDRESS", "SCREENSHOTS", "UNSATISFACTORY_PHOTOS",
         "BAD_PROOF_OF_IDENTITY"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybExpirationDate", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
                "kybAdditionalDocuments_kybLegalExistence", "kybUnsuitableDocument_kybUnsignedDocument"), List.of("EXPIRATION_DATE", "UNSUITABLE_DOCUMENT",
                "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybExpirationDate", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
 "kybUnsuitableDocument_kybUnsignedDocument", "kybAdditionalDocuments_kybOfficialPosition"), List.of("EXPIRATION_DATE", "UNFILLED_ID",
        "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybOther_kybIncorrectCompanyName",
        "kybUnsuitableDocument_kybExpirationDate", "kybOther", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
        "kybAdditionalDocuments_kybOfficialPosition"), List.of("EXPIRATION_DATE", "ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_INCORRECT_DATA"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybUnsuitableDocument_kybUnsignedDocument",
        "kybAdditionalDocuments_kybCertificateOfIncorporation", "kybUnsuitableDocument_kybScreenshot"), List.of("EXPIRATION_DATE", "UNSUITABLE_DOCUMENT",
        "SCREENSHOTS", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybControlAndOwnershipStructure_kybMissingKYCForUBOs",
        "kybAdditionalDocuments", "kybUnsuitableDocument_kybExpirationDate", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument"
        , "kybUnsuitableDocument_kybUnsignedDocument", "kybControlAndOwnershipStructure"), List.of("EXPIRATION_DATE", "UNFILLED_ID",
        "ADDITIONAL_DOCUMENT_REQUIRED", "COMPANY_NOT_DEFINED_STRUCTURE"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "badPhoto", "proofOfAddress_listOfDocs",
        "proofOfAddress", "proofOfAddress_fullName", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS", "UNSATISFACTORY_PHOTOS",
        "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_issueDate", "proofOfAddress_listOfDocs", "additionalPages"
        , "badPhoto", "proofOfAddress", "additionalPages_anotherSide", "badPhoto_editedPoa"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("proofOfAddress_listOfDocs", "proofOfAddress_issueDate", "badPhoto",
        "proofOfAddress", "badPhoto_imageEditor", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("GRAPHIC_EDITOR", "UNSATISFACTORY_PHOTOS",
        "SCREENSHOTS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("additionalPages", "badPhoto", "proofOfAddress_listOfDocs", "badDocument"
        , "proofOfAddress", "additionalPages_anotherSide", "badPhoto_screenshot", "badDocument_notFullNameOrDob"), List.of("BAD_PROOF_OF_IDENTITY",
        "DOCUMENT_PAGE_MISSING", "BAD_PROOF_OF_ADDRESS", "SCREENSHOTS", "UNSATISFACTORY_PHOTOS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybPersonalDocument", "kybOther", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
        "kybOther_kybSoleEntrepreneur", "kybAdditionalDocuments_kybLegalExistence"), List.of("NOT_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED",
                "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybUnsuitableDocument_kybNotADocument", "kybAdditionalDocuments",
        "kybAdditionalDocuments_kybProofOfAddress", "kybAdditionalDocuments_kybCompanyRegisterExcerpt",
        "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument", "kybAdditionalDocuments_kybLegalExistence",
        "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of("NOT_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("selfie_badFaceComparison", "proofOfAddress_listOfDocs",
        "proofOfAddress_issueDate", "badPhoto", "proofOfAddress", "selfie", "badPhoto_screenshot", "proofOfAddress_fullAddress"), List.of("SCREENSHOTS",
        "UNSATISFACTORY_PHOTOS", "BAD_PROOF_OF_ADDRESS"))).getReviewMessageForApplicant());
        assertNotNull(sumsubMapper.apply(stubSumsubEventWithRejectionLabels(List.of("kybAdditionalDocuments", "kybAdditionalDocuments_kybProofOfAddress",
        "kybUnsuitableDocument_kybPersonalDocument", "kybOther", "kybAdditionalDocuments_kybRegisterOfShareholdersOrUBOs", "kybUnsuitableDocument",
        "kybOther_kybSoleEntrepreneur", "kybAdditionalDocuments_kybLegalExistence", "kybAdditionalDocuments_kybCertificateOfIncorporation"), List.of(
                "UNSUITABLE_DOCUMENT", "ADDITIONAL_DOCUMENT_REQUIRED", "DOCUMENT_MISSING"))).getReviewMessageForApplicant());
    }

    private SumsubWebhookEventDTO stubSumsubEventWithRejectionLabels(final List<String> buttonIds, final List<String> rejectLabels) {
        final SumsubWebhookEventDTO sumsubWebhookEventDTO = new SumsubWebhookEventDTO();
        sumsubWebhookEventDTO.setApplicantType("individual");
        sumsubWebhookEventDTO.setExternalUserId(UUID.randomUUID().toString());
        sumsubWebhookEventDTO.setReviewStatus("completed");
        sumsubWebhookEventDTO.setApplicantId(faker.lorem().word());
        final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO = new SumsubWebhookEventDTO.ReviewResultDTO();
        reviewResultDTO.setRejectLabels(rejectLabels);
        reviewResultDTO.setButtonIds(buttonIds);
        reviewResultDTO.setReviewRejectType("RETRY");
        reviewResultDTO.setReviewAnswer("RED");
        sumsubWebhookEventDTO.setReviewResult(reviewResultDTO);
        return sumsubWebhookEventDTO;
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

    private static final String SUMSUB_GET_APPLICANT_BY_APPLICANT_ID_JSON = """
            {
                "id": "66b9b196689cf127e92d52e9",
                "createdAt": "2024-08-12 06:54:14",
                "key": "CFLGUPVDEXFEAS",
                "clientId": "onlydust",
                "inspectionId": "66b9b196689cf127e92d52e9",
                "externalUserId": "beneficiary-random-73b1d9ad-cd47-4e0b-9e4c-b64b9b61da5e",
                "sourceKey": "staging",
                "fixedInfo": {
                    "firstName": "Jean",
                    "firstNameEn": "Jean",
                    "middleName": "",
                    "lastName": "Michel",
                    "lastNameEn": "Michel",
                    "phone": "+33 6 11 22 33 44"
                },
                "email": "test@onlydust.xyz",
                "applicantPlatform": "Web",
                "requiredIdDocs": {
                    "docSets": [
                        {
                            "idDocSetType": "IDENTITY",
                            "types": [
                                "DRIVERS",
                                "ID_CARD",
                                "RESIDENCE_PERMIT",
                                "PASSPORT"
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
                            "questionnaireDefId": "odKycUsPersonStaging"
                        }
                    ]
                },
                "review": {
                    "reviewId": "dBwzQ",
                    "attemptId": "jHOQU",
                    "attemptCnt": 0,
                    "levelName": "od-kyc-staging",
                    "levelAutoCheckMode": null,
                    "createDate": "2024-08-12 06:54:14+0000",
                    "reviewStatus": "init",
                    "priority": 0
                },
                "lang": "en",
                "type": "individual",
                "memberOf": [
                    {
                        "applicantId": "66b9b153689cf127e92d4e12"
                    }
                ]
            }
            """;

    private static final String SUMSUB_GET_KYC_SDK_LINK = """
            {
                "url": "https://in.sumsub.com/websdk/p/sbx_MKsda5Lftfbug2Gg"
            }
            """;
}
