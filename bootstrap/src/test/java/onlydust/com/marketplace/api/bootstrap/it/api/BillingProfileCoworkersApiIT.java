package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.service.BillingProfileService;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

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

}
