package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagUser;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.service.ProjectCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagUser
public class UserProfileUpdateApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    ProjectCategoryService projectCategoryService;

    @Test
    void should_update_user_profile() {
        // Given
        final ProjectCategory category1 = projectCategoryService.createCategory("category1", "category1", "icon", null);
        final ProjectCategory category2 = projectCategoryService.createCategory("category2", "category2", "icon", null);
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // Proves that the initial user profile is different from the updated one
        client.get()
                .uri(getApiURI(ME_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.location").isEqualTo("Vence, France")
                .jsonPath("$.bio").isEqualTo("FullStack engineerr")
                .jsonPath("$.avatarUrl").isEqualTo(
                        "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp")
                .jsonPath("$.website").isEqualTo("https://linktr.ee/abuisset")
                .jsonPath("$.firstName").isEqualTo("Anthony")
                .jsonPath("$.lastName").isEqualTo("BUISSET")
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("NONE")
                .jsonPath("$.isLookingForAJob").isEqualTo(false)
                .jsonPath("$.contacts.length()").isEqualTo(3)
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.joiningGoal").isEmpty()
                .jsonPath("$.joiningReason").isEqualTo("MAINTAINER")
                .jsonPath("$.preferredLanguages").isEmpty()
                .jsonPath("$.preferredCategories").isEmpty();

        // When
        client.patch()
                .uri(getApiURI(ME_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "avatarUrl": "https://foobar.org/plop.jpg",
                            "location": "Paris, France",
                            "bio": "FullStack engineer",
                            "website": "https://croute.org",
                            "firstName": "AnthonyTest",
                            "lastName": "BuissetTest",
                            "contacts": [
                                {
                                    "contact": "https://t.me/yolocroute",
                                    "channel": "TELEGRAM",
                                    "visibility": "private"
                                }
                            ],
                            "allocatedTimeToContribute": "ONE_TO_THREE_DAYS",
                            "isLookingForAJob": true,
                            "joiningGoal": "EARN",
                            "joiningReason": "CONTRIBUTOR",
                            "preferredLanguages": ["ca600cac-0f45-44e9-a6e8-25e21b0c6887", "6b3f8a21-8ae9-4f73-81df-06aeaddbaf42"],
                            "preferredCategories": ["%s", "%s"]
                        }
                        """.formatted(category1.id().value(), category2.id().value()))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful();


        // When
        client.get()
                .uri(getApiURI(ME_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.location").isEqualTo("Paris, France")
                .jsonPath("$.bio").isEqualTo("FullStack engineer")
                .jsonPath("$.avatarUrl").isEqualTo("https://foobar.org/plop.jpg")
                .jsonPath("$.website").isEqualTo("https://croute.org")
                .jsonPath("$.firstName").isEqualTo("AnthonyTest")
                .jsonPath("$.lastName").isEqualTo("BuissetTest")
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("ONE_TO_THREE_DAYS")
                .jsonPath("$.isLookingForAJob").isEqualTo(true)
                .jsonPath("$.contacts.length()").isEqualTo(1)
                .jsonPath("$.contacts[?(@.contact=='antho')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='https://t.me/yolocroute')].visibility").isEqualTo("private")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/yolocroute')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.joiningReason").isEqualTo("CONTRIBUTOR")
                .jsonPath("$.joiningGoal").isEqualTo("EARN");
    }

    @Test
    void should_create_user_profile_with_minimal_info() {
        // Given
        final String jwt = userAuthHelper.authenticateHayden().jwt();

        // When
        client.patch()
                .uri(getApiURI(ME_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "firstName": null,
                          "lastName": null,
                          "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                          "location": null,
                          "bio": null,
                          "website": null,
                          "contacts": [],
                          "allocatedTimeToContribute": null,
                          "isLookingForAJob": null,
                          "joiningGoal": null,
                          "joiningReason": "CONTRIBUTOR",
                          "preferredLanguages": null,
                          "preferredCategories": null
                        }
                        """)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
        ;
    }

    @Test
    void should_return_an_unauthorized_error() {
        // Given

        // When
        client.put()
                .uri(getApiURI(ME_PROFILE))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "location": "Paris, France",
                            "bio": "FullStack engineer",
                            "website": "https://croute.org",
                            "cover": "YELLOW",
                            "contacts": [
                                {
                                    "contact": "https://t.me/yolocroute",
                                    "channel": "TELEGRAM",
                                    "visibility": "private"
                                }
                            ],
                            "allocatedTimeToContribute": "ONE_TO_THREE_DAYS",
                            "isLookingForAJob": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus().isUnauthorized();
    }
}
