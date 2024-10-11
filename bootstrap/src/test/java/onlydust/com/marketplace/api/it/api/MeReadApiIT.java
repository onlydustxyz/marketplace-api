package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.service.ProjectCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagMe
public class MeReadApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    private PostgresBiProjectorAdapter postgresBiProjectorAdapter;

    @Test
    void should_get_recommended_projects() {
        // Given
        final var anthony = userAuthHelper.authenticateAntho();
        // refresh the recommendations as if Anthony had just signed up
        postgresBiProjectorAdapter.onUserSignedUp(AuthenticatedUser.builder()
                .githubUserId(anthony.githubUserId().value())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_RECOMMENDED_PROJECTS))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anthony.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 8,
                          "totalItemNumber": 74,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "projects": [
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "shortDescription": "Scheduling infrastructure for everyone."
                            },
                            {
                              "id": "f39b827f-df73-498c-8853-99bc3f562723",
                              "slug": "qa-new-contributions",
                              "name": "QA new contributions",
                              "logoUrl": null,
                              "shortDescription": "QA new contributions"
                            },
                            {
                              "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                              "slug": "toto",
                              "name": "toto",
                              "logoUrl": null,
                              "shortDescription": "to"
                            },
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "slug": "aiolia-du-lion",
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet"
                            },
                            {
                              "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                              "slug": "aldbaran-du-taureau",
                              "name": "Aldébaran du Taureau",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet"
                            },
                            {
                              "id": "247ac542-762d-44cb-b8d4-4d6199c916be",
                              "slug": "bretzel-196",
                              "name": "Bretzel 196",
                              "logoUrl": null,
                              "shortDescription": "bretzel gives you wings"
                            },
                            {
                              "id": "e41f44a2-464c-4c96-817f-81acb06b2523",
                              "slug": "zero-title-5",
                              "name": "Zero title 5",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png",
                              "shortDescription": "Missing short description"
                            },
                            {
                              "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                              "slug": "taco-tuesday",
                              "name": "Taco Tuesday",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg",
                              "shortDescription": "A projects for the midweek lovers"
                            },
                            {
                              "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                              "slug": "pizzeria-yoshi-",
                              "name": "Pizzeria Yoshi !",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                              "shortDescription": "Miaaaam une pizza !"
                            },
                            {
                              "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                              "slug": "zero-title-11",
                              "name": "Zero title 11",
                              "logoUrl": null,
                              "shortDescription": "Missing short description"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_caller_onboarding() {
        // Given
        final var anthony = userAuthHelper.authenticateAntho();

        // When
        client.get()
                .uri(getApiURI(ME_ONBOARDING))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anthony.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "completed": true,
                          "completion": 80,
                          "verificationInformationProvided": true,
                          "termsAndConditionsAccepted": true,
                          "projectPreferencesProvided": false,
                          "profileCompleted": true,
                          "payoutInformationProvided": true
                        }
                        """);
    }

    @Autowired
    ProjectCategoryService projectCategoryService;

    @Test
    void should_get_caller_onboarding_for_non_indexed_users() {
        // Given
        final var newUser = userAuthHelper.signUpUser(777, "DeViL", "https://devil.com/avatar.jpg", false);

        // When
        client.get()
                .uri(getApiURI(ME_ONBOARDING))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newUser.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "completed": false,
                          "completion": 0,
                          "verificationInformationProvided": false,
                          "termsAndConditionsAccepted": false,
                          "projectPreferencesProvided": false,
                          "profileCompleted": false,
                          "payoutInformationProvided": false
                        }
                        """);

        // Given
        final ProjectCategory category1 = projectCategoryService.createCategory("category1", "category1", "icon", null);
        final ProjectCategory category2 = projectCategoryService.createCategory("category2", "category2", "icon", null);

        // When
        client.patch()
                .uri(getApiURI(ME_PROFILE))
                .header("Authorization", BEARER_PREFIX + newUser.jwt())
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
                            "preferredCategories": ["%s", "%s"],
                            "contactEmail": "fake@test.scam"
                        }
                        """.formatted(category1.id().value(), category2.id().value()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // When
        client.get()
                .uri(getApiURI(ME_ONBOARDING))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newUser.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "completed": false,
                          "completion": 60,
                          "verificationInformationProvided": true,
                          "termsAndConditionsAccepted": false,
                          "projectPreferencesProvided": true,
                          "profileCompleted": true,
                          "payoutInformationProvided": false
                        }
                        """);
    }
}
