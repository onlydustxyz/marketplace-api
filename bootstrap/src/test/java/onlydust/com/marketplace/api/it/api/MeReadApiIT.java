package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagMe;
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
    @Test
    void should_get_recommenced_projects() {
        // Given
        final var anthony = userAuthHelper.authenticateAntho();

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
                          "totalItemNumber": 75,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "projects": [
                            {
                              "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                              "slug": "taco-tuesday",
                              "name": "Taco Tuesday",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg",
                              "shortDescription": "A projects for the midweek lovers"
                            },
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "shortDescription": "Scheduling infrastructure for everyone."
                            },
                            {
                              "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                              "slug": "toto",
                              "name": "toto",
                              "logoUrl": null,
                              "shortDescription": "to"
                            },
                            {
                              "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                              "slug": "zero-title-11",
                              "name": "Zero title 11",
                              "logoUrl": null,
                              "shortDescription": "Missing short description"
                            },
                            {
                              "id": "b0f54343-3732-4118-8054-dba40f1ffb85",
                              "slug": "pacos-project",
                              "name": "Paco's project",
                              "logoUrl": "https://dl.airtable.com/.attachments/01f2dd7497313a1fa13b4c5546429318/764531e3/8bUn9t8ORk6LLyMRcu78",
                              "shortDescription": "A special project for Paco"
                            },
                            {
                              "id": "b58b40b8-1521-41cf-972c-9c08d58eaff8",
                              "slug": "pineapple",
                              "name": "Pineapple",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/3930283280174221329.jpg",
                              "shortDescription": "A project for people who love fruits"
                            },
                            {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "shortDescription": "A project for people who love fruits"
                            },
                            {
                              "id": "e41f44a2-464c-4c96-817f-81acb06b2523",
                              "slug": "zero-title-5",
                              "name": "Zero title 5",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png",
                              "shortDescription": "Missing short description"
                            },
                            {
                              "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                              "slug": "aiolia-du-lion",
                              "name": "Aiolia du Lion",
                              "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                              "shortDescription": "An interactive tutorial to get you up and running with Starknet"
                            },
                            {
                              "id": "f992349c-e30c-4156-8b55-0a9dbc20b873",
                              "slug": "gregs-project",
                              "name": "Greg's project",
                              "logoUrl": "https://dl.airtable.com/.attachments/75bca1dce6735d434b19631814ec84b0/2a9cad0b/aeZxLjpJQre2uXBQDoQf",
                              "shortDescription": "A short lead by an older version of Greg. Clearly not a promising topic, don't go there you'll get bored"
                            }
                          ]
                        }
                        """, true);
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
