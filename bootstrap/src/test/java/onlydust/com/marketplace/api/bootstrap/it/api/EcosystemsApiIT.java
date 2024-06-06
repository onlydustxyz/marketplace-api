package onlydust.com.marketplace.api.bootstrap.it.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@TagProject
public class EcosystemsApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_all_ecosystem_given_an_authenticated_user() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(getApiURI(GET_ALL_ECOSYSTEMS, Map.of("pageIndex", "0", "pageSize", "50")))
                .header("Authorization", "Bearer " + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 8,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg"
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                            },
                            {
                              "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                              "name": "Lava",
                              "url": "https://www.lavanet.xyz/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg"
                            },
                            {
                              "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                              "name": "Optimism",
                              "url": "https://www.optimism.io/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png"
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_unauthorized_given_not_authenticated_user() {
        // When
        client.get()
                .uri(getApiURI(GET_ALL_ECOSYSTEMS, Map.of("pageIndex", "0", "pageSize", "50")))
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void should_return_ecosystem_contributors_by_contribution_count() {
        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_CONTRIBUTORS.formatted("starknet"),
                        Map.of("pageIndex", "0", "pageSize", "5", "sort", "CONTRIBUTION_COUNT")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 206,
                          "totalItemNumber": 1027,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributors": [
                            {
                              "githubUserId": 8019099,
                              "login": "PeerRich",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4",
                              "dynamicRank": 1,
                              "globalRank": 20,
                              "globalRankCategory": "A",
                              "contributionCount": 2878,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 3504472,
                              "login": "zomars",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/3504472?v=4",
                              "dynamicRank": 2,
                              "globalRank": 22,
                              "globalRankCategory": "A",
                              "contributionCount": 2280,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 1046695,
                              "login": "emrysal",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4",
                              "dynamicRank": 3,
                              "globalRank": 22,
                              "globalRankCategory": "A",
                              "contributionCount": 1733,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 481465,
                              "login": "frangio",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/481465?v=4",
                              "dynamicRank": 4,
                              "globalRank": 19,
                              "globalRankCategory": "A",
                              "contributionCount": 1456,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 1780212,
                              "login": "hariombalhara",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                              "dynamicRank": 5,
                              "globalRank": 26,
                              "globalRankCategory": "A",
                              "contributionCount": 1352,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_return_ecosystem_contributors_by_total_earned() {
        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_CONTRIBUTORS.formatted("starknet"),
                        Map.of("pageIndex", "0", "pageSize", "5", "sort", "TOTAL_EARNED")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 206,
                          "totalItemNumber": 1027,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "contributors": [
                            {
                              "githubUserId": 45264458,
                              "login": "abdelhamidbakhta",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                              "dynamicRank": 1,
                              "globalRank": 9,
                              "globalRankCategory": "A",
                              "contributionCount": 21,
                              "rewardCount": 1,
                              "totalEarnedUsd": 1515.00
                            },
                            {
                              "githubUserId": 21149076,
                              "login": "oscarwroche",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                              "dynamicRank": 2,
                              "globalRank": 2,
                              "globalRankCategory": "A",
                              "contributionCount": 213,
                              "rewardCount": 1,
                              "totalEarnedUsd": 1010.00
                            },
                            {
                              "githubUserId": 1046695,
                              "login": "emrysal",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4",
                              "dynamicRank": 3,
                              "globalRank": 22,
                              "globalRankCategory": "A",
                              "contributionCount": 1733,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 481465,
                              "login": "frangio",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/481465?v=4",
                              "dynamicRank": 3,
                              "globalRank": 19,
                              "globalRankCategory": "A",
                              "contributionCount": 1456,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            },
                            {
                              "githubUserId": 1780212,
                              "login": "hariombalhara",
                              "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                              "dynamicRank": 3,
                              "globalRank": 26,
                              "globalRankCategory": "A",
                              "contributionCount": 1352,
                              "rewardCount": 0,
                              "totalEarnedUsd": 0
                            }
                          ]
                        }
                        """);
    }

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void should_return_ecosystem_projects() {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                -- // Add good first issue to project B Conseil
                INSERT INTO indexer_exp.github_issues_labels (issue_id, label_id) VALUES (1678706027, 3042230479);
                -- // Add tag to project Bretzel
                INSERT INTO projects_tags (project_id, tag) VALUES ('7d04163c-4187-4313-8066-61504d34fc56', 'FAST_AND_FURIOUS');
                INSERT INTO projects_tags (project_id, tag) VALUES ('7d04163c-4187-4313-8066-61504d34fc56', 'WORK_IN_PROGRESS');
                -- // Add tag to project Moonlight
                INSERT INTO projects_tags (project_id, tag) VALUES ('594ca5ca-48f7-49a8-9c26-84b949d4fdd9', 'WORK_IN_PROGRESS');
                """).executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();

        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("zama"), Map.of("pageIndex", "0", "pageSize", "5")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "shortDescription": "A project for people who love fruits",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "topContributors": [
                                {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                },
                                {
                                  "githubUserId": 52197971,
                                  "login": "jb1011",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                                },
                                {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4"
                                }
                              ],
                              "contributorsCount": 4,
                              "languages": [
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ]
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("starknet"), Map.of("pageIndex", "0", "pageSize", "3", "sortBy", "RANK")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "shortDescription": "Scheduling infrastructure for everyone.",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "topContributors": [
                                {
                                  "githubUserId": 8019099,
                                  "login": "PeerRich",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4"
                                },
                                {
                                  "githubUserId": 3504472,
                                  "login": "zomars",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/3504472?v=4"
                                },
                                {
                                  "githubUserId": 1046695,
                                  "login": "emrysal",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4"
                                }
                              ],
                              "contributorsCount": 559,
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ]
                            },
                            {
                              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "slug": "b-conseil",
                              "name": "B Conseil",
                              "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                              "topContributors": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                },
                                {
                                  "githubUserId": 4435377,
                                  "login": "Bernardstanislas",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                                }
                              ],
                              "contributorsCount": 3,
                              "languages": []
                            },
                            {
                              "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                              "slug": "zero-title-11",
                              "name": "Zero title 11",
                              "shortDescription": "Missing short description",
                              "logoUrl": null,
                              "topContributors": [
                                {
                                  "githubUserId": 481465,
                                  "login": "frangio",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/481465?v=4"
                                },
                                {
                                  "githubUserId": 2432299,
                                  "login": "Amxx",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/2432299?v=4"
                                },
                                {
                                  "githubUserId": 2530770,
                                  "login": "nventuro",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/2530770?v=4"
                                }
                              ],
                              "contributorsCount": 453,
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                  "slug": "solidity",
                                  "name": "Solidity",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                }
                              ]
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 2,
                          "totalItemNumber": 4,
                          "nextPageIndex": 1
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("starknet"), Map.of("pageIndex", "0", "pageSize", "3", "sortBy", "RANK",
                        "hasGoodFirstIssues", "true")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                              "slug": "zero-title-11",
                              "name": "Zero title 11",
                              "shortDescription": "Missing short description",
                              "logoUrl": null,
                              "topContributors": [
                                {
                                  "githubUserId": 481465,
                                  "login": "frangio",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/481465?v=4"
                                },
                                {
                                  "githubUserId": 2432299,
                                  "login": "Amxx",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/2432299?v=4"
                                },
                                {
                                  "githubUserId": 2530770,
                                  "login": "nventuro",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/2530770?v=4"
                                }
                              ],
                              "contributorsCount": 453,
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                  "name": "Solidity",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                }
                              ]
                            },
                            {
                              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                              "slug": "b-conseil",
                              "name": "B Conseil",
                              "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                              "topContributors": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                },
                                {
                                  "githubUserId": 4435377,
                                  "login": "Bernardstanislas",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                                }
                              ],
                              "contributorsCount": 3,
                              "languages": []
                            },
                            {
                              "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                              "slug": "calcom",
                              "name": "Cal.com",
                              "shortDescription": "Scheduling infrastructure for everyone.",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                              "topContributors": [
                                {
                                  "githubUserId": 8019099,
                                  "login": "PeerRich",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/8019099?v=4"
                                },
                                {
                                  "githubUserId": 3504472,
                                  "login": "zomars",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/3504472?v=4"
                                },
                                {
                                  "githubUserId": 1046695,
                                  "login": "emrysal",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/1046695?v=4"
                                }
                              ],
                              "contributorsCount": 559,
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ]
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 3,
                          "nextPageIndex": 0
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("starknet"), Map.of("pageIndex", "0", "pageSize", "3", "sortBy", "RANK",
                        "tag", "WORK_IN_PROGRESS")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                              "slug": "mooooooonlight",
                              "name": "Mooooooonlight",
                              "shortDescription": "hello la team",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                              "topContributors": [
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                },
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                },
                                {
                                  "githubUserId": 4435377,
                                  "login": "Bernardstanislas",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                                }
                              ],
                              "contributorsCount": 20,
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ]
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("zama"), Map.of("pageIndex", "0", "pageSize", "3", "sortBy", "RANK",
                        "tag", "FAST_AND_FURIOUS", "hidden", "true")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [
                            {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "shortDescription": "A project for people who love fruits",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "topContributors": [
                                {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                                },
                                {
                                  "githubUserId": 52197971,
                                  "login": "jb1011",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                                },
                                {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4"
                                }
                              ],
                              "contributorsCount": 4,
                              "languages": [
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ]
                            }
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 1,
                          "nextPageIndex": 0
                        }
                        """);

    }

    @Test
    void should_return_ecosystem_featured_projects() {
        // When
        client.get()
                .uri(getApiURI(GET_ECOSYSTEM_PROJECTS.formatted("starknet"), Map.of("featuredOnly", "true", "sortBy", "RANK")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                                 "projects": [
                                   {
                                     "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                     "slug": "b-conseil",
                                     "name": "B Conseil",
                                     "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                                     "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                                     "topContributors": [
                                       {
                                         "githubUserId": 43467246,
                                         "login": "AnthonyBuisset",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                       },
                                       {
                                         "githubUserId": 595505,
                                         "login": "ofux",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                       },
                                       {
                                         "githubUserId": 4435377,
                                         "login": "Bernardstanislas",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                                       }
                                     ],
                                     "contributorsCount": 3,
                                     "languages": []
                                   },
                                   {
                                     "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                     "slug": "mooooooonlight",
                                     "name": "Mooooooonlight",
                                     "shortDescription": "hello la team",
                                     "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                                     "topContributors": [
                                       {
                                         "githubUserId": 43467246,
                                         "login": "AnthonyBuisset",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                                       },
                                       {
                                         "githubUserId": 595505,
                                         "login": "ofux",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                                       },
                                       {
                                         "githubUserId": 4435377,
                                         "login": "Bernardstanislas",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                                       }
                                     ],
                                     "contributorsCount": 20,
                                     "languages": [
                                       {
                                         "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                         "name": "Javascript",
                                         "slug": "javascript",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                       },
                                       {
                                         "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                         "name": "Python",
                                         "slug": "python",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                       },
                                       {
                                         "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                         "name": "Rust",
                                         "slug": "rust",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                       },
                                       {
                                         "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                         "name": "Typescript",
                                         "slug": "typescript",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                       }
                                     ]
                                   },
                                   {
                                     "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                                     "slug": "zero-title-11",
                                     "name": "Zero title 11",
                                     "shortDescription": "Missing short description",
                                     "logoUrl": null,
                                     "topContributors": [
                                       {
                                         "githubUserId": 481465,
                                         "login": "frangio",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/481465?v=4"
                                       },
                                       {
                                         "githubUserId": 2432299,
                                         "login": "Amxx",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/2432299?v=4"
                                       },
                                       {
                                         "githubUserId": 2530770,
                                         "login": "nventuro",
                                         "avatarUrl": "https://avatars.githubusercontent.com/u/2530770?v=4"
                                       }
                                     ],
                                     "contributorsCount": 453,
                                     "languages": [
                                       {
                                         "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                         "name": "Javascript",
                                         "slug": "javascript",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                       },
                                       {
                                         "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                         "name": "Solidity",
                                         "slug": "solidity",
                                         "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                         "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                       }
                                     ]
                                   }
                                 ],
                                 "hasMore": false,
                                 "totalPageNumber": 1,
                                 "totalItemNumber": 4,
                                 "nextPageIndex": 0
                               }
                                                
                        """, true);
    }
}
