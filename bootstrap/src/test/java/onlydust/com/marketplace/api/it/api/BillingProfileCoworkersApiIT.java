package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CurrencyRepository;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TagAccounting
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BillingProfileCoworkersApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BillingProfileService billingProfileService;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    @Order(1)
    void should_be_authenticated() {
        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS))
                // Then
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(2)
    void should_get_coworkers_of_individual_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "foo.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        final var individualBillingProfile = billingProfileService.createIndividualBillingProfile(userId, faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(individualBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(1)
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "foo.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        
                        """);

    }

    @Test
    @Order(2)
    void should_get_coworkers_of_self_employed_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "bar.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        final var selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(selfEmployedBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(1)
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "bar.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        """);

    }

    @Test
    @Order(2)
    void should_get_coworkers_of_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "boss.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        final var companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(1)
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        """);

    }

    @Test
    @Order(10)
    void should_invite_coworkers_of_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "boss.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        final var companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/123456789999"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        final var invitedGithubUserId = 123456789999L;

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 123456789999,
                          "role": "ADMIN"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(null)
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(invitedGithubUserId)
                .jsonPath("$.coworkers[1].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 595505,
                          "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(3)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(null)
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(123456789999L)
                .jsonPath("$.coworkers[1].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .jsonPath("$.coworkers[2].id").isEqualTo("e461c019-ba23-4671-9b6c-3a5a18748af9")
                .jsonPath("$.coworkers[2].githubUserId").isEqualTo(595505)
                .jsonPath("$.coworkers[2].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[2].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "MEMBER",
                              "joinedAt": null,
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(2)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();

        // When
        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": false
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(null)
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(123456789999L)
                .jsonPath("$.coworkers[1].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(1)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')]").doesNotExist();

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 595505,
                          "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(3)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(null)
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(123456789999L)
                .jsonPath("$.coworkers[1].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .jsonPath("$.coworkers[2].id").isEqualTo("e461c019-ba23-4671-9b6c-3a5a18748af9")
                .jsonPath("$.coworkers[2].githubUserId").isEqualTo(595505)
                .jsonPath("$.coworkers[2].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[2].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "MEMBER",
                              "joinedAt": null,
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(2)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();

        // When
        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.billingProfiles.length()").isEqualTo(2)
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_BY_ID.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.isSwitchableToSelfEmployed").isEqualTo(false)
                .jsonPath("$.me.canDelete").isEqualTo(false)
                .jsonPath("$.me.canLeave").isEqualTo(true)
                .jsonPath("$.me.invitation").isNotEmpty();

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(3)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo("e461c019-ba23-4671-9b6c-3a5a18748af9")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(595505)
                .jsonPath("$.coworkers[1].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .jsonPath("$.coworkers[2].id").isEqualTo(null)
                .jsonPath("$.coworkers[2].githubUserId").isEqualTo(123456789999L)
                .jsonPath("$.coworkers[2].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[2].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "MEMBER",
                              "removable": true
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles.length()").isEqualTo(2)
                .jsonPath("$.billingProfiles[?(@.type == 'SELF_EMPLOYED')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());
    }

    @Test
    @Order(11)
    void should_remove_coworkers_from_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "boss.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());
        final var anthony = userAuthHelper.authenticateAntho();

        final var companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/123456789999"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 43467246,
                          "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + anthony.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(anthony.user().getId().toString())
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(anthony.user().getGithubUserId())
                .jsonPath("$.coworkers[1].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 43467246,
                              "login": "AnthonyBuisset",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                              "isRegistered": true,
                              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
                              "role": "MEMBER",
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + anthony.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_COWORKER.formatted(companyBillingProfile.id().value().toString(), "43467246")))
                .header("Authorization", "Bearer " + anthony.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(1)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + anthony.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')]").doesNotExist();
    }

    @Test
    @Order(12)
    void should_unlink_payout_preferences_and_rewards_when_removing_coworkers_from_company() {
        //Given
        final var pierre = userAuthHelper.authenticatePierre();
        final var companyBillingProfile = BillingProfile.Id.of(UUID.fromString("20282367-56b0-42d3-81d3-5e4b38f67e3e"));

        accountingHelper.patchBillingProfile(companyBillingProfile.value(), BillingProfile.Type.COMPANY,
                VerificationStatus.VERIFIED);

        accountingHelper.patchReward("40fda3c6-2a3f-4cdd-ba12-0499dd232d53", 10, "ETH", 15000, null, "2023-07-12");
        accountingHelper.patchReward("e1498a17-5090-4071-a88a-6f0b0c337c3a", 50, "ETH", 75000, null, "2023-08-12");
        accountingHelper.patchReward("2ac80cc6-7e83-4eef-bc0c-932b58f683c0", 500, "APT", 100000, null, null);
        accountingHelper.patchReward("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0", 30, "OP", null, "2023-08-14", null);
        accountingHelper.patchReward("5b96ca1e-4ad2-41c1-8819-520b885d9223", 9511147, "STRK", null, null, null);

        billingProfileStoragePort.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .ethWallet(Ethereum.wallet("vitalik.eth"))
                .aptosAddress(Aptos.accountAddress("0x" + faker.random().hex(40)))
                .build(), BillingProfile.Id.of("20282367-56b0-42d3-81d3-5e4b38f67e3e"));

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "githubUserId": 16590657,
                              "login": "PierreOucif",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "isRegistered": true,
                              "id": "fc92397c-3431-4a84-8054-845376b630a0",
                              "role": "ADMIN",
                              "joinedAt": "2024-02-28T17:32:57.617763Z",
                              "invitedAt": null,
                              "removable": false
                            }
                          ]
                        }
                        """);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.value().toString())))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 595505,
                          "role": "ADMIN"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.value().toString())))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "githubUserId": 16590657,
                              "login": "PierreOucif",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                              "isRegistered": true,
                              "id": "fc92397c-3431-4a84-8054-845376b630a0",
                              "role": "ADMIN",
                              "joinedAt": "2024-02-28T17:32:57.617763Z",
                              "removable": true
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);


        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "1000"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewards": [
                            {
                              "status": "PENDING_REQUEST",
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "status": "PENDING_REQUEST",
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "status": "PAYOUT_INFO_MISSING",
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            },
                            {
                              "status": "PROCESSING",
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "status": "COMPLETE",
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            },
                            {
                              "status": "COMPLETE",
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                            }
                          ]
                        }
                        """);

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_COWORKER.formatted(companyBillingProfile.value().toString(), pierre.user().getGithubUserId())))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(ME_GET_REWARDS, Map.of(
                        "pageIndex", "0",
                        "pageSize", "1000"
                )))
                .header("Authorization", BEARER_PREFIX + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "rewards": [
                            {
                              "status": "PENDING_BILLING_PROFILE",
                              "id": "2ac80cc6-7e83-4eef-bc0c-932b58f683c0"
                            },
                            {
                              "status": "PENDING_BILLING_PROFILE",
                              "id": "85f8358c-5339-42ac-a577-16d7760d1e28"
                            },
                            {
                              "status": "PENDING_BILLING_PROFILE",
                              "id": "5b96ca1e-4ad2-41c1-8819-520b885d9223"
                            },
                            {
                              "status": "PROCESSING",
                              "id": "8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"
                            },
                            {
                              "status": "COMPLETE",
                              "id": "40fda3c6-2a3f-4cdd-ba12-0499dd232d53"
                            },
                            {
                              "status": "COMPLETE",
                              "id": "e1498a17-5090-4071-a88a-6f0b0c337c3a"
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(20)
    void should_update_coworkers_role() {
        // Given
        final var admin = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "boss.armstrong", "https://www.plop.org", false);
        final var coworker = userAuthHelper.authenticateAntho();
        final var adminId = UserId.of(admin.user().getId());

        final var companyBillingProfile = billingProfileService.createCompanyBillingProfile(adminId, faker.rickAndMorty().character(), null);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/%d".formatted(coworker.user().getGithubUserId())))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + admin.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": %d,
                          "role": "MEMBER"
                        }
                        """.formatted(coworker.user().getGithubUserId()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + admin.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(adminId.toString())
                .jsonPath("$.coworkers[0].role").isEqualTo("ADMIN")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(coworker.user().getGithubUserId())
                .jsonPath("$.coworkers[1].role").isEqualTo("MEMBER")
        ;

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + coworker.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_COWORKER_ROLE.formatted(companyBillingProfile.id().toString(), coworker.user().getGithubUserId())))
                .header("Authorization", "Bearer " + admin.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "role": "ADMIN"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + admin.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(adminId.toString())
                .jsonPath("$.coworkers[0].role").isEqualTo("ADMIN")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(coworker.user().getGithubUserId())
                .jsonPath("$.coworkers[1].role").isEqualTo("ADMIN")
        ;

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + coworker.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_COWORKER_ROLE.formatted(companyBillingProfile.id().value().toString(), coworker.user().getGithubUserId())))
                .header("Authorization", "Bearer " + admin.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + admin.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(adminId.toString())
                .jsonPath("$.coworkers[0].role").isEqualTo("ADMIN")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(coworker.user().getGithubUserId())
                .jsonPath("$.coworkers[1].role").isEqualTo("MEMBER")
        ;

        // When
        client.post()
                .uri(getApiURI(ME_BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + coworker.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "accepted": true
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + coworker.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_COWORKER_ROLE.formatted(companyBillingProfile.id().toString(), coworker.user().getGithubUserId())))
                .header("Authorization", "Bearer " + admin.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "role": "ADMIN"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + admin.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(adminId.toString())
                .jsonPath("$.coworkers[0].role").isEqualTo("ADMIN")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(coworker.user().getGithubUserId())
                .jsonPath("$.coworkers[1].role").isEqualTo("ADMIN")
        ;

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + coworker.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.put()
                .uri(getApiURI(BILLING_PROFILES_COWORKER_ROLE.formatted(companyBillingProfile.id().value().toString(), coworker.user().getGithubUserId())))
                .header("Authorization", "Bearer " + admin.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                            "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + admin.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(adminId.toString())
                .jsonPath("$.coworkers[0].role").isEqualTo("ADMIN")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(coworker.user().getGithubUserId())
                .jsonPath("$.coworkers[1].role").isEqualTo("MEMBER")
        ;

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + coworker.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(false)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());
    }

    @Test
    @Order(21)
    void should_cancel_coworker_invitation_from_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.signUpUser(
                faker.number().randomNumber(10, true), "boss.armstrong", "https://www.plop.org",
                false);
        final String jwt = authenticatedUser.jwt();
        final UserId userId = UserId.of(authenticatedUser.user().getId());

        final var companyBillingProfile = billingProfileService.createCompanyBillingProfile(userId, faker.rickAndMorty().character(), null);

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/123456789999"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        // When
        client.post()
                .uri(getApiURI(BILLING_PROFILES_POST_COWORKER_INVITATIONS.formatted(companyBillingProfile.id().value().toString())))
                .header("Authorization", "Bearer " + jwt)
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "githubUserId": 123456789999,
                          "role": "ADMIN"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(2)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].id").isEqualTo(null)
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(123456789999L)
                .jsonPath("$.coworkers[1].joinedAt").isEqualTo(null)
                .jsonPath("$.coworkers[1].invitedAt").isNotEmpty()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(ME_BILLING_PROFILES))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].pendingInvitationResponse").isEqualTo(true)
                .jsonPath("$.billingProfiles[?(@.type == 'COMPANY')].id").isEqualTo(companyBillingProfile.id().value().toString());

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_COWORKER.formatted(companyBillingProfile.id().value().toString(), "123456789999")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_COWORKERS.formatted(companyBillingProfile.id().value().toString()),
                        Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.coworkers.length()").isEqualTo(1)
                .jsonPath("$.coworkers[0].id").isEqualTo(authenticatedUser.user().getId().toString())
                .jsonPath("$.coworkers[0].githubUserId").isEqualTo(authenticatedUser.user().getGithubUserId())
                .jsonPath("$.coworkers[0].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[0].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_be_able_to_update_payout_info_when_invitation_role_and_billing_profile_role_are_different() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final CompanyBillingProfile companyBillingProfile = billingProfileService.createCompanyBillingProfile(UserId.of(pierre.user().getId()), "Role IT",
                Set.of());
        final UserAuthHelper.AuthenticatedUser antho = userAuthHelper.authenticateAntho();
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(antho.user().getGithubUserId()),
                BillingProfile.User.Role.ADMIN);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(5160414L),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(595505L),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(8642470L),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(4435377),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(21149076),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(141839618),
                BillingProfile.User.Role.MEMBER);
        billingProfileService.inviteCoworker(companyBillingProfile.id(), UserId.of(pierre.user().getId()), GithubUserId.of(595505),
                BillingProfile.User.Role.MEMBER);

        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(5160414L));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(595505L));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(8642470L));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(4435377));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(21149076));
        billingProfileService.acceptCoworkerInvitation(companyBillingProfile.id(), GithubUserId.of(141839618));

        // When
        client.get()
                .uri(getApiURI(BILLING_PROFILES_GET_PAYOUT_INFO.formatted(companyBillingProfile.id().value())))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
