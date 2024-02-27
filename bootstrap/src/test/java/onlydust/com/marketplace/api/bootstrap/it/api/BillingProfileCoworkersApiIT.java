package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BillingProfileCoworkersApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    BillingProfileService billingProfileService;

    @Test
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
    void should_get_coworkers_of_individual_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                              "htmlUrl": null,
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
    void should_get_coworkers_of_self_employed_billing_profile() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                              "htmlUrl": null,
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
    void should_get_coworkers_of_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                              "htmlUrl": null,
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
    void should_invite_coworkers_of_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "htmlUrl": "https://github.com/ofux",
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "htmlUrl": "https://github.com/ofux",
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
                .jsonPath("$.coworkers[1].invitedAt").isEqualTo(null)
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "htmlUrl": "https://github.com/ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "MEMBER",
                              "removable": true
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_remove_coworkers_from_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                          "githubUserId": 595505,
                          "role": "MEMBER"
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

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
                .jsonPath("$.coworkers[1].id").isEqualTo("e461c019-ba23-4671-9b6c-3a5a18748af9")
                .jsonPath("$.coworkers[1].githubUserId").isEqualTo(595505)
                .jsonPath("$.coworkers[1].joinedAt").isNotEmpty()
                .jsonPath("$.coworkers[1].invitedAt").isEqualTo(null)
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "coworkers": [
                            {
                              "login": "boss.armstrong",
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 595505,
                              "login": "ofux",
                              "htmlUrl": "https://github.com/ofux",
                              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                              "isRegistered": true,
                              "id": "e461c019-ba23-4671-9b6c-3a5a18748af9",
                              "role": "MEMBER",
                              "removable": true
                            }
                          ]
                        }
                        """);

        // When
        client.delete()
                .uri(getApiURI(BILLING_PROFILES_DELETE_COWORKER.formatted(companyBillingProfile.id().value().toString(), "595505")))
                .header("Authorization", "Bearer " + userAuthHelper.authenticateOlivier().jwt())
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
                              "htmlUrl": null,
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
    void should_cancel_coworker_invitation_from_company() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.newFakeUser(UUID.randomUUID(),
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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            },
                            {
                              "githubUserId": 123456789999,
                              "login": null,
                              "htmlUrl": null,
                              "avatarUrl": null,
                              "isRegistered": false,
                              "role": "ADMIN",
                              "removable": true
                            }
                          ]
                        }
                        """);

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
                              "htmlUrl": null,
                              "avatarUrl": "https://www.plop.org",
                              "isRegistered": true,
                              "role": "ADMIN",
                              "removable": false
                            }
                          ]
                        }
                        """);
    }

}
