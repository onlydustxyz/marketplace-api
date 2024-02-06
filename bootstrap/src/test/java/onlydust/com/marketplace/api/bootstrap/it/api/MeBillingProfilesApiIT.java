package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.onlydust.api.sumsub.api.client.adapter.SumsubApiClientAdapter;
import com.onlydust.api.sumsub.api.client.adapter.SumsubClientProperties;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubSignatureVerifier;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_OD_API;
import static onlydust.com.marketplace.api.sumsub.webhook.adapter.SumsubWebhookApiAdapter.X_SUMSUB_PAYLOAD_DIGEST;

public class MeBillingProfilesApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    SumsubWebhookProperties sumsubWebhookProperties;
    @Autowired
    SumsubClientProperties sumsubClientProperties;

    @Test
    void should_get_individual_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME_GET_INDIVIDUAL_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");
    }

    @Test
    void should_get_company_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(ME_GET_COMPANY_BILLING_PROFILE)
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");
    }

    @Autowired
    IndividualBillingProfileRepository individualBillingProfileRepository;

    // To delete when we'll have a test with Sumsub mocked to create a billing profile with the KYC flow
    @Test
    void should_get_individual_billing_profile_given_one() throws InterruptedException {
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
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.status").isEqualTo("NOT_STARTED");

        final UUID billingProfileId = individualBillingProfileRepository.findByUserId(userId).orElseThrow().getId();

        final String sumsubApiPath = String.format("/resources/applicants/-;externalUserId=%s/one",
                billingProfileId.toString());
        sumsubWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(sumsubApiPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader(SumsubApiClientAdapter.X_APP_TOKEN, equalTo(sumsubClientProperties.getAppToken()))
                .willReturn(responseDefinition().withStatus(200).withBody(SUMSUB_INDIVIDUAL_RESPONSE)));

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
                }""", billingProfileId).getBytes(StandardCharsets.UTF_8);
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
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW")
                .jsonPath("$.address").isEqualTo("25 AVENUE SAINT LOUIS, ETAGE 2 APT, ST MAUR DES FOSSES, France, 94210")
                .jsonPath("$.firstName").isEqualTo("ALEXIS")
                .jsonPath("$.lastName").isEqualTo("BENOLIEL")
                .jsonPath("$.country").isEqualTo("France")
                .jsonPath("$.birthdate").isEqualTo("1995-09-19T00:00:00Z")
                .jsonPath("$.idDocumentNumber").isEqualTo("15AC05169")
                .jsonPath("$.idDocumentType").isEqualTo("PASSPORT")
                .jsonPath("$.idDocumentCountryCode").isEqualTo("FRA")
                .jsonPath("$.validUntil").isEqualTo("2025-04-19T00:00:00Z")
                .jsonPath("$.usCitizen").isEqualTo(false);
    }

    @Autowired
    CompanyBillingProfileRepository companyBillingProfileRepository;

    // To delete when we'll have a test with Sumsub mocked to create a billing profile with the KYB flow
    @Test
    void should_get_company_billing_profile_given_one() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .address(faker.address().fullAddress())
                .country(faker.address().country())
                .name(faker.rickAndMorty().character())
                .registrationNumber(faker.harryPotter().character())
                .euVATNumber(faker.hacker().abbreviation())
                .usEntity(true)
                .registrationDate(faker.date().past(10, TimeUnit.DAYS))
                .verificationStatus(VerificationStatusEntity.UNDER_REVIEW)
                .build());

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
                .jsonPath("$.status").isEqualTo("UNDER_REVIEW");
    }


    @Test
    void should_update_user_billing_profile_type() {
        // Given
        final var githubUserId = faker.number().randomNumber() + faker.number().randomNumber();
        final var login = faker.name().username();
        final var avatarUrl = faker.internet().avatar();
        final var userId = UUID.randomUUID();
        final String jwt = userAuthHelper.newFakeUser(userId, githubUserId, login, avatarUrl, false).jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfileType").isEqualTo("INDIVIDUAL");

        // When
        client.patch()
                .uri(getApiURI(ME_PATCH_BILLING_PROFILE_TYPE))
                .header("Authorization", "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "type": "COMPANY"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(ME_GET))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfileType").isEqualTo("COMPANY");
    }

    private static final String SUMSUB_INDIVIDUAL_RESPONSE = """
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
                        "id": "kycCustom",
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
}
