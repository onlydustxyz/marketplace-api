package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Project;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.STRK;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsApiIT extends AbstractMarketplaceApiIT {
    private final static String CAL_DOT_COM = "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e";

    private static final String BRETZEL_OVERVIEW_JSON = """
            {
              "id": "7d04163c-4187-4313-8066-61504d34fc56",
              "slug": "bretzel",
              "name": "Bretzel",
              "createdAt": "2023-02-21T09:15:09.603Z",
              "shortDescription": "A project for people who love fruits",
              "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
              "moreInfos": [],
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 4,
              "topContributors": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                },
                {
                  "githubUserId": 52197971,
                  "login": "jb1011",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                },
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
                }
              ],
              "organizations": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "name": "Grégoire CHARLES",
                  "htmlUrl": "https://github.com/gregcha",
                  "repos": [
                    {
                      "id": 380954304,
                      "owner": "gregcha",
                      "name": "bretzel-app",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-app",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    },
                    {
                      "id": 452047076,
                      "owner": "gregcha",
                      "name": "bretzel-site",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-site",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    },
                    {
                      "id": 466482535,
                      "owner": "gregcha",
                      "name": "bretzel-ressources",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-ressources",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372,
                  "installationStatus": "COMPLETE"
                },
                {
                  "githubUserId": 119948009,
                  "login": "KasarLabs",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/119948009?v=4",
                  "name": "KasarLabs",
                  "htmlUrl": "https://github.com/KasarLabs",
                  "repos": [
                    {
                      "id": 659718526,
                      "owner": "KasarLabs",
                      "name": "deoxys-telemetry",
                      "description": "Deoxys Telemetry service",
                      "htmlUrl": "https://github.com/KasarLabs/deoxys-telemetry",
                      "stars": 0,
                      "forkCount": 1,
                      "hasIssues": false,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": false
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": null,
                  "installationStatus": "NOT_INSTALLED"
                }
              ],
              "leaders": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                },
                {
                  "githubUserId": 98735421,
                  "login": "pacovilletard",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                  "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                }
              ],
              "invitedLeaders": [],
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos"
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum"
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama"
                }
              ],
              "categories": [],
              "programs": [],
              "languages": [
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "slug": "typescript",
                  "name": "Typescript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                }
              ],
              "hasRemainingBudget": true,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              },
              "indexingComplete": true,
              "indexedAt": "2023-12-04T14:35:10.986567Z",
              "tags": [],
              "goodFirstIssueCount": 0
            }
            """;
    private static final String BRETZEL_OVERVIEW_WITH_TAGS_JSON = """
            {
              "id": "7d04163c-4187-4313-8066-61504d34fc56",
              "slug": "bretzel",
              "name": "Bretzel",
              "createdAt": "2023-02-21T09:15:09.603Z",
              "shortDescription": "A project for people who love fruits",
              "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
              "moreInfos": [],
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 4,
              "topContributors": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                },
                {
                  "githubUserId": 52197971,
                  "login": "jb1011",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                },
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
                }
              ],
              "organizations": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "name": "Grégoire CHARLES",
                  "htmlUrl": "https://github.com/gregcha",
                  "repos": [
                    {
                      "id": 380954304,
                      "owner": "gregcha",
                      "name": "bretzel-app",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-app",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    },
                    {
                      "id": 452047076,
                      "owner": "gregcha",
                      "name": "bretzel-site",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-site",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    },
                    {
                      "id": 466482535,
                      "owner": "gregcha",
                      "name": "bretzel-ressources",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/bretzel-ressources",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372,
                  "installationStatus": "COMPLETE"
                },
                {
                  "githubUserId": 119948009,
                  "login": "KasarLabs",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/119948009?v=4",
                  "name": "KasarLabs",
                  "htmlUrl": "https://github.com/KasarLabs",
                  "repos": [
                    {
                      "id": 659718526,
                      "owner": "KasarLabs",
                      "name": "deoxys-telemetry",
                      "description": "Deoxys Telemetry service",
                      "htmlUrl": "https://github.com/KasarLabs/deoxys-telemetry",
                      "stars": 0,
                      "forkCount": 1,
                      "hasIssues": false,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": false
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": null,
                  "installationStatus": "NOT_INSTALLED"
                }
              ],
              "leaders": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                },
                {
                  "githubUserId": 98735421,
                  "login": "pacovilletard",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                  "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                }
              ],
              "invitedLeaders": [
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": null
                }
              ],
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos"
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum"
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama"
                }
              ],
              "categories": [],
              "programs": [],
              "hasRemainingBudget": true,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              },
              "indexingComplete": true,
              "indexedAt": "2023-12-04T14:35:10.986567Z",
              "me": {
                "isMember": false,
                "isContributor": false,
                "isProjectLead": false,
                "isInvitedAsProjectLead": false
              },
              "tags": [
                "FAST_AND_FURIOUS",
                "NEWBIES_WELCOME"
              ],
              "goodFirstIssueCount": 0
            }
            """;
    private static final String B_CONSEIL_OVERVIEW_JSON = """
            {
              "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
              "slug": "b-conseil",
              "name": "B Conseil",
              "createdAt": "2023-05-24T09:55:04.729Z",
              "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
              "longDescription": "Nous sommes **pure player** du financement et du management de l’innovation. Avec une présence physique à Paris nous adressons des entreprises **sur tout le territoire** jusque dans les départements d'outre mer.  \\nNotre équipe d’**ingénieurs pluridisciplinaire** (École des Mines, Arts et métiers, Centrale Nantes, École centrale d’Electronique, Polytech, Epitech, etc.) nous permet d’adresser **tous les secteurs de l’Innovation et de la recherche**. Nous avons également une parfaite maîtrise de la valorisation des sciences humaines et sociales.",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
              "moreInfos": [],
              "hiring": true,
              "visibility": "PRIVATE",
              "contributorCount": 3,
              "topContributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                }
              ],
              "organizations": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "name": "Grégoire CHARLES",
                  "htmlUrl": "https://github.com/gregcha",
                  "repos": [
                    {
                      "id": 302082426,
                      "owner": "gregcha",
                      "name": "crew-app",
                      "description": null,
                      "htmlUrl": "https://github.com/gregcha/crew-app",
                      "stars": 0,
                      "forkCount": 0,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": true
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372,
                  "installationStatus": "COMPLETE"
                },
                {
                  "githubUserId": 121887739,
                  "login": "od-mocks",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/121887739?v=4",
                  "name": "OD Mocks",
                  "htmlUrl": "https://github.com/od-mocks",
                  "repos": [
                    {
                      "id": 602953043,
                      "owner": "od-mocks",
                      "name": "cool-repo-A",
                      "description": "This is repo A for our e2e tests",
                      "htmlUrl": "https://github.com/od-mocks/cool-repo-A",
                      "stars": 1,
                      "forkCount": 2,
                      "hasIssues": true,
                      "isIncludedInProject": true,
                      "isAuthorizedInGithubApp": false
                    }
                  ],
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": null,
                  "installationStatus": "NOT_INSTALLED"
                }
              ],
              "leaders": [
                {
                  "githubUserId": 134486697,
                  "login": "axelbconseil",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/134486697?v=4",
                  "id": "83612081-949a-47c4-a467-6f28f6adad6d"
                }
              ],
              "invitedLeaders": [
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "id": null
                }
              ],
              "ecosystems": [
                {
                  "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                  "name": "Lava",
                  "url": "https://www.lavanet.xyz/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg",
                  "bannerUrl": null,
                  "slug": "lava"
                },
                {
                  "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                  "name": "Optimism",
                  "url": "https://www.optimism.io/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png",
                  "bannerUrl": null,
                  "slug": "optimism"
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet"
                }
              ],
              "categories": [],
              "categorySuggestions": [],
              "programs": [],
              "languages": [],
              "hasRemainingBudget": true,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              },
              "indexingComplete": true,
              "indexedAt": "2023-12-04T14:29:52.896598Z",
              "tags": [],
              "goodFirstIssueCount": 0
            }
            """;

    @Autowired
    ProjectViewRepository projectViewRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ProjectCategorySuggestionRepository projectCategorySuggestionRepository;
    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    public ProjectTagRepository projectTagRepository;

    @BeforeEach
    void setUp() {
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "ai", "AI", "AI is cool", "brain", Set.of());
        projectCategoryRepository.saveAll(List.of(
                new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "security", "Security", "Security is important", "lock",
                        Set.of()),
                categoryAI
        ));
        final var project = projectRepository.findById(UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c")).get();
        project.setCategories(Set.of(new ProjectProjectCategoryEntity(project.getId(), categoryAI.getId())));
        projectRepository.save(project);
    }

    @Test
    @Order(1)
    public void should_get_a_project_by_slug() {
        // Given
        final String slug = "bretzel";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody()
                .json(BRETZEL_OVERVIEW_JSON);

        // When user is authenticated
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember")
                .isEqualTo(false)
                .jsonPath("$.me.isContributor")
                .isEqualTo(false)
                .jsonPath("$.me.isProjectLead")
                .isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead")
                .isEqualTo(false)
                .json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    @Order(2)
    public void should_get_a_project_by_id() {
        // Given
        final String id = "7d04163c-4187-4313-8066-61504d34fc56";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id)).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(BRETZEL_OVERVIEW_JSON);

        // When user is authenticated
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id)).header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(false)
                .jsonPath("$.me.isContributor").isEqualTo(false)
                .jsonPath("$.me.isProjectLead").isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false)
                .json(BRETZEL_OVERVIEW_JSON);
    }


    @Test
    @Order(3)
    public void should_return_good_first_issue_count() {
        // Given
        final String id = "7d04163c-4187-4313-8066-61504d34fc56";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + CAL_DOT_COM)).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody()
                .jsonPath("$.goodFirstIssueCount").isEqualTo(11);
    }

    @Test
    @Order(8)
    public void should_get_a_private_project_by_slug() {
        // Given
        final String slug = "b-conseil";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).exchange()
                // Then
                .expectStatus().isForbidden();

        // When a contributor gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateOlivier().jwt()).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(true)
                .jsonPath("$.me.isProjectLead").isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When a lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateUser(134486697L).jwt()).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(false)
                .jsonPath("$.me.isProjectLead").isEqualTo(true)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When an invited lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(false)
                .jsonPath("$.me.isProjectLead").isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(true)
                .json(B_CONSEIL_OVERVIEW_JSON);
    }

    @Test
    @Order(9)
    public void should_get_a_private_project_by_id() {
        // Given
        final String id = "27ca7e18-9e71-468f-8825-c64fe6b79d66";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id)).exchange()
                // Then
                .expectStatus().isForbidden();

        // When a contributor gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(true)
                .jsonPath("$.me.isProjectLead").isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When a lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateUser(134486697L).jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful().expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(false)
                .jsonPath("$.me.isProjectLead").isEqualTo(true)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When an invited lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id)).header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateHayden().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.me.isMember").isEqualTo(true)
                .jsonPath("$.me.isContributor").isEqualTo(false)
                .jsonPath("$.me.isProjectLead").isEqualTo(false)
                .jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(true)
                .json(B_CONSEIL_OVERVIEW_JSON);
    }

    @Test
    @Order(13)
    public void should_get_a_project_by_slug_with_tags() {
        // Given
        final String slug = "bretzel";
        final ProjectViewEntity bretzel = projectViewRepository.findBySlug(slug).orElseThrow();
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(), bretzel.getId(),
                userAuthHelper.authenticatePierre().user().getGithubUserId()));

        projectTagRepository.saveAll(List.of(
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
                                                .tag(Project.Tag.NEWBIES_WELCOME)
                                                .build()
                                ).build(),
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
                                                .tag(Project.Tag.FAST_AND_FURIOUS)
                                                .build()
                                ).build(),
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"))
                                                .tag(Project.Tag.FAST_AND_FURIOUS)
                                                .build()
                                ).build()
                )
        );

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody()
                .json(BRETZEL_OVERVIEW_WITH_TAGS_JSON);
    }

    @Test
    @Order(15)
    public void should_get_a_project_by_slug_with_active_program() {
        // Given
        final var slug = "bretzel";
        final var sponsor = sponsorHelper.create();
        final var program = programHelper.create();
        final var projectId = ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56");

        accountingHelper.createSponsorAccount(sponsor.id(), 100, STRK);
        accountingHelper.allocate(sponsor.id(), program.id(), 100, STRK);
        accountingHelper.grant(program.id(), projectId, 100, STRK);

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.programs").isArray()
                .jsonPath("$.programs.length()").isEqualTo(1)
                .jsonPath("$.programs[0].id").isEqualTo(program.id().toString());
    }
}
