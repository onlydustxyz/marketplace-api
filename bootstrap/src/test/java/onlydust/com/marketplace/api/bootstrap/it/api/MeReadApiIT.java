package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagMe;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@TagMe
public class MeReadApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_get_recommenced_projects() {
        // Given
        final var anthony = userAuthHelper.authenticateAnthony();

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
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "shortDescription": "Scheduling infrastructure for everyone."
                            },
                            {
                              "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                              "slug": "zero-title-11",
                              "name": "Zero title 11",
                              "logoUrl": null,
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
                              "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                              "slug": "taco-tuesday",
                              "name": "Taco Tuesday",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg",
                              "shortDescription": "A projects for the midweek lovers"
                            },
                            {
                              "id": "b0f54343-3732-4118-8054-dba40f1ffb85",
                              "slug": "pacos-project",
                              "name": "Paco's project",
                              "logoUrl": "https://dl.airtable.com/.attachments/01f2dd7497313a1fa13b4c5546429318/764531e3/8bUn9t8ORk6LLyMRcu78",
                              "shortDescription": "A special project for Paco"
                            },
                            {
                              "id": "f992349c-e30c-4156-8b55-0a9dbc20b873",
                              "slug": "gregs-project",
                              "name": "Greg's project",
                              "logoUrl": "https://dl.airtable.com/.attachments/75bca1dce6735d434b19631814ec84b0/2a9cad0b/aeZxLjpJQre2uXBQDoQf",
                              "shortDescription": "A short lead by an older version of Greg. Clearly not a promising topic, don't go there you'll get bored"
                            },
                            {
                              "id": "f25e3389-d681-4811-b45c-3d1106d8e478",
                              "slug": "zero-title-18",
                              "name": "Zero title 18",
                              "logoUrl": null,
                              "shortDescription": "Missing short description"
                            },
                            {
                              "id": "4f7bcc3e-3d3d-4a8f-8280-bb6df33382da",
                              "slug": "zero-title-19",
                              "name": "Zero title 19",
                              "logoUrl": null,
                              "shortDescription": "Missing short description"
                            },
                            {
                              "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                              "slug": "onlydust-marketplace",
                              "name": "OnlyDust Marketplace",
                              "logoUrl": null,
                              "shortDescription": "afsasdas"
                            },
                            {
                              "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                              "slug": "apibara",
                              "name": "Apibara",
                              "logoUrl": null,
                              "shortDescription": "Listen to starknet events using gRPC and build your own node"
                            }
                          ]
                        }
                        """, true);
    }

    @Test
    void should_get_caller_journey() {
        // Given
        final var anthony = userAuthHelper.authenticateAnthony();

        // When
        client.get()
                .uri(getApiURI(ME_JOURNEY))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + anthony.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "completed": false,
                          "completion": 80,
                          "individualBillingProfileSetup": false,
                          "firstContributionMade": true,
                          "firstRewardClaimed": true,
                          "descriptionUpdated": true,
                          "telegramAdded": true
                        }
                        """);
    }
}
