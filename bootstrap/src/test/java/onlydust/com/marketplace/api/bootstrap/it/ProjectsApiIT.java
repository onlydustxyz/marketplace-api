package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsApiIT extends AbstractMarketplaceApiIT {

    private static final String BRETZEL_OVERVIEW_JSON = """
            {
              "id": "7d04163c-4187-4313-8066-61504d34fc56",
              "slug": "bretzel",
              "name": "Bretzel",
              "createdAt": "2023-02-21T09:15:09.603Z",
              "shortDescription": "A project for people who love fruits",
              "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
              "moreInfos": [
                {
                  "url": "https://bretzel.club/",
                  "value": null
                }
              ],
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 4,
              "topContributors": [
                {
                  "githubUserId": 117665867,
                  "login": "gilbertVDB17",
                  "htmlUrl": "https://github.com/gilbertVDB17",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4"
                },
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                },
                {
                  "githubUserId": 52197971,
                  "login": "jb1011",
                  "htmlUrl": "https://github.com/jb1011",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                }
              ],
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
                  "isIncludedInProject": null,
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
                  "isIncludedInProject": null,
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
                  "isIncludedInProject": null,
                  "isAuthorizedInGithubApp": true
                },
                {
                  "id": 659718526,
                  "owner": "KasarLabs",
                  "name": "deoxys-telemetry",
                  "description": "Deoxys Telemetry service",
                  "htmlUrl": "https://github.com/KasarLabs/deoxys-telemetry",
                  "stars": 0,
                  "forkCount": 1,
                  "hasIssues": false,
                  "isIncludedInProject": null,
                  "isAuthorizedInGithubApp": false
                }
              ],
              "organizations": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "htmlUrl": "https://github.com/gregcha",
                  "name": "Grégoire CHARLES",
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
                  "installed": true,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372
                },
                {
                  "githubUserId": 119948009,
                  "login": "KasarLabs",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/119948009?v=4",
                  "htmlUrl": "https://github.com/KasarLabs",
                  "name": "KasarLabs",
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
                  "installed": false,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": null
                }
              ],
              "leaders": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                },
                {
                  "githubUserId": 98735421,
                  "login": "pacovilletard",
                  "htmlUrl": "https://github.com/pacovilletard",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                  "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                }
              ],
              "invitedLeaders": [],
              "sponsors": [
                {
                  "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                  "name": "OGC Nissa Ineos",
                  "url": "https://www.ogcnice.com/fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                },
                {
                  "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                  "name": "Coca Cola",
                  "url": null,
                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                }
              ],
              "technologies": {
                "TypeScript": 190809,
                "Dockerfile": 1982,
                "CSS": 423688,
                "Shell": 732,
                "Rust": 408641,
                "SCSS": 98360,
                "JavaScript": 62716,
                "HTML": 121874
              },
              "remainingUsdBudget": 99250.00,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              },
              "indexingComplete": true,
              "indexedAt": "2023-12-04T14:34:49.384525Z"
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
              "moreInfos": [
                {
                  "url": "https://www.bconseil.fr/",
                  "value": null
                }
              ],
              "hiring": true,
              "visibility": "PRIVATE",
              "contributorCount": 3,
              "topContributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": "https://github.com/AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4"
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "htmlUrl": "https://github.com/Bernardstanislas",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4"
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "htmlUrl": "https://github.com/ofux",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4"
                }
              ],
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
                  "isIncludedInProject": null,
                  "isAuthorizedInGithubApp": true
                },
                {
                  "id": 602953043,
                  "owner": "od-mocks",
                  "name": "cool-repo-A",
                  "description": "This is repo A for our e2e tests",
                  "htmlUrl": "https://github.com/od-mocks/cool-repo-A",
                  "stars": 1,
                  "forkCount": 2,
                  "hasIssues": true,
                  "isIncludedInProject": null,
                  "isAuthorizedInGithubApp": false
                }
              ],
              "organizations": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                  "name": "Grégoire CHARLES",
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
                  "installed": true,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44378743
                },
                {
                  "githubUserId": 121887739,
                  "login": "od-mocks",
                  "htmlUrl": "https://github.com/od-mocks",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/121887739?v=4",
                  "name": "OD Mocks",
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
                  "installed": false,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": null
                }
              ],
              "leaders": [
                {
                  "githubUserId": 134486697,
                  "login": "axelbconseil",
                  "htmlUrl": "https://github.com/axelbconseil",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/134486697?v=4",
                  "id": "83612081-949a-47c4-a467-6f28f6adad6d"
                }
              ],
              "invitedLeaders": [
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": "https://github.com/haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "id": null
                }
              ],
              "sponsors": [],
              "technologies": {
                "CSS": 323507,
                "Rust": 527,
                "SCSS": 102453,
                "JavaScript": 58624,
                "HTML": 169898
              },
              "remainingUsdBudget": 50000,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              }
            }
                        
            """;
    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_JSON_RESPONSE = """
            {
              "projects": [
                {
                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                  "slug": "bretzel",
                  "name": "Bretzel",
                  "shortDescription": "A project for people who love fruits",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 4,
                  "contributorCount": 4,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 98735421,
                      "login": "pacovilletard",
                      "htmlUrl": "https://github.com/pacovilletard",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                      "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    },
                    {
                      "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                      "name": "Coca Cola",
                      "url": null,
                      "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "CSS": 423688,
                    "Shell": 732,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": true,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                  "slug": "qa-new-contributions",
                  "name": "QA new contributions",
                  "shortDescription": "QA new contributions",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 18,
                  "leaders": [
                    {
                      "githubUserId": 16590657,
                      "login": "PierreOucif",
                      "htmlUrl": "https://github.com/PierreOucif",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": false
                }
              ],
              "technologies": [
                "CSS",
                "Dockerfile",
                "HTML",
                "JavaScript",
                "MDX",
                "PLpgSQL",
                "Rust",
                "SCSS",
                "Shell",
                "TypeScript"
              ],
              "sponsors": [
                {
                  "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                  "name": "Coca Cola",
                  "url": null,
                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                },
                {
                  "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                  "name": "OGC Nissa Ineos",
                  "url": "https://www.ogcnice.com/fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                }
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 2,
              "nextPageIndex": 0
            }
                        
            """;
    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE = """
            {
              "projects": [
                {
                  "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                  "slug": "aiolia-du-lion",
                  "name": "Aiolia du Lion",
                  "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/26790304?v=4",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                      "name": "AS Nancy Lorraine",
                      "url": null,
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                    },
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    },
                    {
                      "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                      "name": "Coca Cola",
                      "url": null,
                      "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                    },
                    {
                      "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                      "name": "Coca Colax",
                      "url": "https://www.coca-cola-france.fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                    }
                  ],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                  "slug": "aldbaran-du-taureau",
                  "name": "Aldébaran du Taureau",
                  "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/26790304?v=4",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                      "name": "AS Nancy Lorraine",
                      "url": null,
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                    },
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    }
                  ],
                  "technologies": {
                    "CSS": 114,
                    "Makefile": 49,
                    "JavaScript": 15570,
                    "HTML": 235
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                  "slug": "apibara",
                  "name": "Apibara",
                  "shortDescription": "Listen to starknet events using gRPC and build your own node",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                  "slug": "b-conseil",
                  "name": "B Conseil",
                  "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                  "hiring": true,
                  "visibility": "PRIVATE",
                  "repoCount": 2,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 134486697,
                      "login": "axelbconseil",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134486697?v=4",
                      "id": "83612081-949a-47c4-a467-6f28f6adad6d"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "Rust": 527,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                  "slug": "bretzel",
                  "name": "Bretzel",
                  "shortDescription": "A project for people who love fruits",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 4,
                  "contributorCount": 4,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 98735421,
                      "login": "pacovilletard",
                      "htmlUrl": "https://github.com/pacovilletard",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                      "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    },
                    {
                      "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                      "name": "Coca Cola",
                      "url": null,
                      "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "CSS": 423688,
                    "Shell": 732,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "247ac542-762d-44cb-b8d4-4d6199c916be",
                  "slug": "bretzel-196",
                  "name": "Bretzel 196",
                  "shortDescription": "bretzel gives you wings",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                  "slug": "calcom",
                  "name": "Cal.com",
                  "shortDescription": "Scheduling infrastructure for everyone.",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 559,
                  "leaders": [
                    {
                      "githubUserId": 117665867,
                      "login": "gilbertVDB17",
                      "htmlUrl": "https://github.com/gilbertVDB17",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                      "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 109316,
                    "TypeScript": 7052833,
                    "Dockerfile": 2591,
                    "CSS": 41229,
                    "Shell": 6831,
                    "Procfile": 37,
                    "JavaScript": 56416,
                    "PHP": 1205,
                    "HTML": 119986
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "97ab7c1f-f86d-4cb7-83bf-6062e6847564",
                  "slug": "coucou",
                  "name": "coucou",
                  "shortDescription": "\\uD83D\\uDEA8 Short description missing",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f992349c-e30c-4156-8b55-0a9dbc20b873",
                  "slug": "gregs-project",
                  "name": "Greg's project",
                  "shortDescription": "A short lead by an older version of Greg. Clearly not a promising topic, don't go there you'll get bored",
                  "logoUrl": "https://dl.airtable.com/.attachments/75bca1dce6735d434b19631814ec84b0/2a9cad0b/aeZxLjpJQre2uXBQDoQf",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 36,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "Ruby": 9708
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                  "slug": "kaaper",
                  "name": "kaaper",
                  "shortDescription": "Documentation generator for Cairo projects.",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 21,
                  "leaders": [
                    {
                      "githubUserId": 43467246,
                      "login": "AnthonyBuisset",
                      "htmlUrl": "https://github.com/AnthonyBuisset",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "TypeScript": 3592913,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "Cairo": 72428,
                    "PLpgSQL": 1372,
                    "JavaScript": 26108,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": false
                },
                {
                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                  "slug": "mooooooonlight",
                  "name": "Mooooooonlight",
                  "shortDescription": "hello la team",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 4,
                  "contributorCount": 20,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                      "name": "Theodo",
                      "url": null,
                      "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                    },
                    {
                      "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                      "name": "Starknet Foundation",
                      "url": "https://starknet.io",
                      "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                    }
                  ],
                  "technologies": {
                    "MDX": 2520,
                    "C++": 2226,
                    "CSS": 6065,
                    "Rust": 453557,
                    "CMake": 460,
                    "PLpgSQL": 1372,
                    "HTML": 1520,
                    "Kotlin": 1381,
                    "TypeScript": 3175211,
                    "Dockerfile": 325,
                    "Shell": 12431,
                    "JavaScript": 24365,
                    "Objective-C": 38,
                    "Swift": 404,
                    "Dart": 121265
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                  "slug": "no-sponsors",
                  "name": "No sponsors",
                  "shortDescription": "afsasdas",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 18,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "Dockerfile": 325,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24365,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                  "slug": "onlydust-marketplace",
                  "name": "OnlyDust Marketplace",
                  "shortDescription": "afsasdas",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 10,
                  "leaders": [
                    {
                      "githubUserId": 43467246,
                      "login": "AnthonyBuisset",
                      "htmlUrl": "https://github.com/AnthonyBuisset",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "COBOL": 10808,
                    "JavaScript": 6987
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": true
                },
                {
                  "id": "b0f54343-3732-4118-8054-dba40f1ffb85",
                  "slug": "pacos-project",
                  "name": "Paco's project",
                  "shortDescription": "A special project for Paco",
                  "logoUrl": "https://dl.airtable.com/.attachments/01f2dd7497313a1fa13b4c5546429318/764531e3/8bUn9t8ORk6LLyMRcu78",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 455,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "C++": 2226,
                    "CMake": 460,
                    "Makefile": 1714,
                    "HTML": 169898,
                    "Kotlin": 1381,
                    "Shell": 8324,
                    "Solidity": 837904,
                    "SCSS": 102453,
                    "JavaScript": 1085801,
                    "Objective-C": 38,
                    "Swift": 404,
                    "Ruby": 255376,
                    "Dart": 121265,
                    "Python": 6719
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "b58b40b8-1521-41cf-972c-9c08d58eaff8",
                  "slug": "pineapple",
                  "name": "Pineapple",
                  "shortDescription": "A project for people who love fruits",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/3930283280174221329.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "TypeScript": 609777,
                    "Solidity": 420744,
                    "Makefile": 367,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 5416
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                  "slug": "pizzeria-yoshi-",
                  "name": "Pizzeria Yoshi !",
                  "shortDescription": "Miaaaam une pizza !",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 5,
                  "contributorCount": 886,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "htmlUrl": "https://github.com/kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "C++": 23419,
                    "CSS": 1396,
                    "Jinja": 2398,
                    "C": 1425,
                    "Rust": 527,
                    "CMake": 18862,
                    "Makefile": 2213,
                    "HTML": 7303,
                    "Jupyter Notebook": 577371,
                    "Kotlin": 140,
                    "TypeScript": 631356,
                    "Dockerfile": 6263,
                    "Shell": 20110,
                    "Batchfile": 478,
                    "Solidity": 476140,
                    "Cairo": 654593,
                    "JavaScript": 4071194,
                    "Objective-C": 38,
                    "Swift": 2384,
                    "Nix": 85,
                    "Ruby": 2803,
                    "Dart": 204844,
                    "Python": 1676320
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                  "slug": "qa-new-contributions",
                  "name": "QA new contributions",
                  "shortDescription": "QA new contributions",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 18,
                  "leaders": [
                    {
                      "githubUserId": 16590657,
                      "login": "PierreOucif",
                      "htmlUrl": "https://github.com/PierreOucif",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                  "slug": "starklings",
                  "name": "Starklings",
                  "shortDescription": "Stop tuto",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "htmlUrl": "https://github.com/Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                  "slug": "taco-tuesday",
                  "name": "Taco Tuesday",
                  "shortDescription": "A projects for the midweek lovers",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 45,
                  "leaders": [
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/122993337?v=4",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "htmlUrl": "https://github.com/kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/141839618?v=4",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                      "name": "Red Bull",
                      "url": "https://www.redbull.com/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 617035,
                    "Dockerfile": 5694,
                    "Shell": 4352,
                    "Rust": 255905,
                    "Solidity": 421314,
                    "Cairo": 797556,
                    "Makefile": 2085,
                    "JavaScript": 5356,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 11538
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                  "slug": "toto",
                  "name": "toto",
                  "shortDescription": "to",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 1,
                  "leaders": [
                    {
                      "githubUserId": 595505,
                      "login": "ofux",
                      "htmlUrl": "https://github.com/ofux",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                      "name": "PSG",
                      "url": "https://www.psg.fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                    }
                  ],
                  "technologies": {
                    "Rust": 23314
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                  "slug": "zama",
                  "name": "Zama",
                  "shortDescription": "A super description for Zama",
                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 18,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "Shell": 3429,
                    "Cairo": 42100,
                    "Python": 45301
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                  "slug": "zero-title-11",
                  "name": "Zero title 11",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 453,
                  "leaders": [],
                  "sponsors": [
                    {
                      "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                      "name": "Theodo",
                      "url": null,
                      "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                    },
                    {
                      "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                      "name": "Starknet Foundation",
                      "url": "https://starknet.io",
                      "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                    }
                  ],
                  "technologies": {
                    "Shell": 8324,
                    "Solidity": 837904,
                    "Makefile": 1714,
                    "JavaScript": 1027177,
                    "Ruby": 255376,
                    "Python": 6719
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f25e3389-d681-4811-b45c-3d1106d8e478",
                  "slug": "zero-title-18",
                  "name": "Zero title 18",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "4f7bcc3e-3d3d-4a8f-8280-bb6df33382da",
                  "slug": "zero-title-19",
                  "name": "Zero title 19",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                  "slug": "zero-title-4",
                  "name": "Zero title 4",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 4,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "Dockerfile": 325,
                    "Scheme": 43698,
                    "JavaScript": 342,
                    "Haskell": 16365
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "e41f44a2-464c-4c96-817f-81acb06b2523",
                  "slug": "zero-title-5",
                  "name": "Zero title 5",
                  "shortDescription": "Missing short description",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 297,
                  "leaders": [
                    {
                      "githubUserId": 595505,
                      "login": "ofux",
                      "htmlUrl": "https://github.com/ofux",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "Shell": 4429,
                    "Rust": 2017307,
                    "HTML": 871
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                }
              ],
              "technologies": [
                "Batchfile",
                "C",
                "C++",
                "CMake",
                "COBOL",
                "CSS",
                "Cairo",
                "Dart",
                "Dockerfile",
                "HTML",
                "Haskell",
                "JavaScript",
                "Jinja",
                "Jupyter Notebook",
                "Kotlin",
                "MDX",
                "Makefile",
                "Nix",
                "Objective-C",
                "PHP",
                "PLpgSQL",
                "Procfile",
                "Python",
                "Ruby",
                "Rust",
                "SCSS",
                "Scheme",
                "Shell",
                "Solidity",
                "Swift",
                "TypeScript"
              ],
              "sponsors": [
                {
                  "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                  "name": "AS Nancy Lorraine",
                  "url": null,
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                },
                {
                  "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                  "name": "Coca Cola",
                  "url": null,
                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                },
                {
                  "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                  "name": "Coca Colax",
                  "url": "https://www.coca-cola-france.fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                },
                {
                  "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                  "name": "OGC Nissa Ineos",
                  "url": "https://www.ogcnice.com/fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                },
                {
                  "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                  "name": "PSG",
                  "url": "https://www.psg.fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                },
                {
                  "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                  "name": "Red Bull",
                  "url": "https://www.redbull.com/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                },
                {
                  "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                  "name": "Starknet Foundation",
                  "url": "https://starknet.io",
                  "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                },
                {
                  "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                  "name": "Theodo",
                  "url": null,
                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                }
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 26,
              "nextPageIndex": 0
            }
            """;
    private static final String GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE = """
            {
               "projects": [
                 {
                   "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                   "slug": "mooooooonlight",
                   "name": "Mooooooonlight",
                   "shortDescription": "hello la team",
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                   "hiring": false,
                   "visibility": "PUBLIC",
                   "repoCount": 4,
                   "contributorCount": 20,
                   "leaders": [
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "htmlUrl": "https://github.com/gregcha",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "sponsors": [
                     {
                       "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                       "name": "Theodo",
                       "url": null,
                       "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                     },
                     {
                       "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                       "name": "Starknet Foundation",
                       "url": "https://starknet.io",
                       "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                     }
                   ],
                   "technologies": {
                     "MDX": 2520,
                     "C++": 2226,
                     "CSS": 6065,
                     "Rust": 453557,
                     "CMake": 460,
                     "PLpgSQL": 1372,
                     "HTML": 1520,
                     "Kotlin": 1381,
                     "TypeScript": 3175211,
                     "Dockerfile": 325,
                     "Shell": 12431,
                     "JavaScript": 24365,
                     "Objective-C": 38,
                     "Swift": 404,
                     "Dart": 121265
                   },
                   "isInvitedAsProjectLead": false,
                   "isMissingGithubAppInstallation": null
                 }
               ],
               "technologies": [
                 "C++",
                 "CMake",
                 "CSS",
                 "Dart",
                 "Dockerfile",
                 "HTML",
                 "JavaScript",
                 "Kotlin",
                 "MDX",
                 "Objective-C",
                 "PLpgSQL",
                 "Rust",
                 "Shell",
                 "Swift",
                 "TypeScript"
               ],
               "sponsors": [
                 {
                   "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                   "name": "Starknet Foundation",
                   "url": "https://starknet.io",
                   "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                 },
                 {
                   "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                   "name": "Theodo",
                   "url": null,
                   "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                 }
               ],
               "hasMore": false,
               "totalPageNumber": 1,
               "totalItemNumber": 1,
               "nextPageIndex": 0
             }
            """;
    private static final String GET_PROJECTS_FOR_ANONYMOUS_USER_JSON_RESPONSE = """
            {
              "projects": [
                {
                  "id": "98873240-31df-431a-81dc-7d6fe01143a0",
                  "slug": "aiolia-du-lion",
                  "name": "Aiolia du Lion",
                  "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aiolia_lion.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=11e0e551affa5a88cc8c6de7f352449c",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/26790304?v=4",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                      "name": "AS Nancy Lorraine",
                      "url": null,
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                    },
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    },
                    {
                      "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                      "name": "Coca Cola",
                      "url": null,
                      "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                    },
                    {
                      "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                      "name": "Coca Colax",
                      "url": "https://www.coca-cola-france.fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                    }
                  ],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                  "slug": "aldbaran-du-taureau",
                  "name": "Aldébaran du Taureau",
                  "shortDescription": "An interactive tutorial to get you up and running with Starknet",
                  "logoUrl": "https://www.puregamemedia.fr/media/images/uploads/2019/11/ban_saint_seiya_awakening_kotz_aldebaran_taureau.jpg/?w=790&h=inherit&fm=webp&fit=contain&s=ab78704b124d2de9525a8af91ef7c4ed",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/26790304?v=4",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                      "name": "AS Nancy Lorraine",
                      "url": null,
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                    },
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    }
                  ],
                  "technologies": {
                    "CSS": 114,
                    "Makefile": 49,
                    "JavaScript": 15570,
                    "HTML": 235
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                  "slug": "apibara",
                  "name": "Apibara",
                  "shortDescription": "Listen to starknet events using gRPC and build your own node",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "7d04163c-4187-4313-8066-61504d34fc56",
                  "slug": "bretzel",
                  "name": "Bretzel",
                  "shortDescription": "A project for people who love fruits",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 4,
                  "contributorCount": 4,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 98735421,
                      "login": "pacovilletard",
                      "htmlUrl": "https://github.com/pacovilletard",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                      "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                      "name": "OGC Nissa Ineos",
                      "url": "https://www.ogcnice.com/fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                    },
                    {
                      "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                      "name": "Coca Cola",
                      "url": null,
                      "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "CSS": 423688,
                    "Shell": 732,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "247ac542-762d-44cb-b8d4-4d6199c916be",
                  "slug": "bretzel-196",
                  "name": "Bretzel 196",
                  "shortDescription": "bretzel gives you wings",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                  "slug": "calcom",
                  "name": "Cal.com",
                  "shortDescription": "Scheduling infrastructure for everyone.",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 559,
                  "leaders": [
                    {
                      "githubUserId": 117665867,
                      "login": "gilbertVDB17",
                      "htmlUrl": "https://github.com/gilbertVDB17",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                      "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 109316,
                    "TypeScript": 7052833,
                    "Dockerfile": 2591,
                    "CSS": 41229,
                    "Shell": 6831,
                    "Procfile": 37,
                    "JavaScript": 56416,
                    "PHP": 1205,
                    "HTML": 119986
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "97ab7c1f-f86d-4cb7-83bf-6062e6847564",
                  "slug": "coucou",
                  "name": "coucou",
                  "shortDescription": "\\uD83D\\uDEA8 Short description missing",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f992349c-e30c-4156-8b55-0a9dbc20b873",
                  "slug": "gregs-project",
                  "name": "Greg's project",
                  "shortDescription": "A short lead by an older version of Greg. Clearly not a promising topic, don't go there you'll get bored",
                  "logoUrl": "https://dl.airtable.com/.attachments/75bca1dce6735d434b19631814ec84b0/2a9cad0b/aeZxLjpJQre2uXBQDoQf",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 36,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "Ruby": 9708
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                  "slug": "kaaper",
                  "name": "kaaper",
                  "shortDescription": "Documentation generator for Cairo projects.",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 21,
                  "leaders": [
                    {
                      "githubUserId": 43467246,
                      "login": "AnthonyBuisset",
                      "htmlUrl": "https://github.com/AnthonyBuisset",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "TypeScript": 3592913,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "Cairo": 72428,
                    "PLpgSQL": 1372,
                    "JavaScript": 26108,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                  "slug": "mooooooonlight",
                  "name": "Mooooooonlight",
                  "shortDescription": "hello la team",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 4,
                  "contributorCount": 20,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                      "name": "Theodo",
                      "url": null,
                      "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                    },
                    {
                      "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                      "name": "Starknet Foundation",
                      "url": "https://starknet.io",
                      "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                    }
                  ],
                  "technologies": {
                    "MDX": 2520,
                    "C++": 2226,
                    "CSS": 6065,
                    "Rust": 453557,
                    "CMake": 460,
                    "PLpgSQL": 1372,
                    "HTML": 1520,
                    "Kotlin": 1381,
                    "TypeScript": 3175211,
                    "Dockerfile": 325,
                    "Shell": 12431,
                    "JavaScript": 24365,
                    "Objective-C": 38,
                    "Swift": 404,
                    "Dart": 121265
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                  "slug": "no-sponsors",
                  "name": "No sponsors",
                  "shortDescription": "afsasdas",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 2,
                  "contributorCount": 18,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "Dockerfile": 325,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24365,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                  "slug": "onlydust-marketplace",
                  "name": "OnlyDust Marketplace",
                  "shortDescription": "afsasdas",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 10,
                  "leaders": [
                    {
                      "githubUserId": 43467246,
                      "login": "AnthonyBuisset",
                      "htmlUrl": "https://github.com/AnthonyBuisset",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "COBOL": 10808,
                    "JavaScript": 6987
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "b0f54343-3732-4118-8054-dba40f1ffb85",
                  "slug": "pacos-project",
                  "name": "Paco's project",
                  "shortDescription": "A special project for Paco",
                  "logoUrl": "https://dl.airtable.com/.attachments/01f2dd7497313a1fa13b4c5546429318/764531e3/8bUn9t8ORk6LLyMRcu78",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 455,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "C++": 2226,
                    "CMake": 460,
                    "Makefile": 1714,
                    "HTML": 169898,
                    "Kotlin": 1381,
                    "Shell": 8324,
                    "Solidity": 837904,
                    "SCSS": 102453,
                    "JavaScript": 1085801,
                    "Objective-C": 38,
                    "Swift": 404,
                    "Ruby": 255376,
                    "Dart": 121265,
                    "Python": 6719
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "b58b40b8-1521-41cf-972c-9c08d58eaff8",
                  "slug": "pineapple",
                  "name": "Pineapple",
                  "shortDescription": "A project for people who love fruits",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/3930283280174221329.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "TypeScript": 609777,
                    "Solidity": 420744,
                    "Makefile": 367,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 5416
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                  "slug": "pizzeria-yoshi-",
                  "name": "Pizzeria Yoshi !",
                  "shortDescription": "Miaaaam une pizza !",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 5,
                  "contributorCount": 886,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "htmlUrl": "https://github.com/kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "C++": 23419,
                    "CSS": 1396,
                    "Jinja": 2398,
                    "C": 1425,
                    "Rust": 527,
                    "CMake": 18862,
                    "Makefile": 2213,
                    "HTML": 7303,
                    "Jupyter Notebook": 577371,
                    "Kotlin": 140,
                    "TypeScript": 631356,
                    "Dockerfile": 6263,
                    "Shell": 20110,
                    "Batchfile": 478,
                    "Solidity": 476140,
                    "Cairo": 654593,
                    "JavaScript": 4071194,
                    "Objective-C": 38,
                    "Swift": 2384,
                    "Nix": 85,
                    "Ruby": 2803,
                    "Dart": 204844,
                    "Python": 1676320
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                  "slug": "qa-new-contributions",
                  "name": "QA new contributions",
                  "shortDescription": "QA new contributions",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 18,
                  "leaders": [
                    {
                      "githubUserId": 16590657,
                      "login": "PierreOucif",
                      "htmlUrl": "https://github.com/PierreOucif",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                  "slug": "starklings",
                  "name": "Starklings",
                  "shortDescription": "Stop tuto",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg",
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 0,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "htmlUrl": "https://github.com/Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                  "slug": "taco-tuesday",
                  "name": "Taco Tuesday",
                  "shortDescription": "A projects for the midweek lovers",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/6987338668519888809.jpg",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 45,
                  "leaders": [
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/122993337?v=4",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "htmlUrl": "https://github.com/kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "htmlUrl": null,
                      "avatarUrl": "https://avatars.githubusercontent.com/u/141839618?v=4",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                      "name": "Red Bull",
                      "url": "https://www.redbull.com/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 617035,
                    "Dockerfile": 5694,
                    "Shell": 4352,
                    "Rust": 255905,
                    "Solidity": 421314,
                    "Cairo": 797556,
                    "Makefile": 2085,
                    "JavaScript": 5356,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 11538
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "61076487-6ec5-4751-ab0d-3b876c832239",
                  "slug": "toto",
                  "name": "toto",
                  "shortDescription": "to",
                  "logoUrl": null,
                  "hiring": true,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 1,
                  "leaders": [
                    {
                      "githubUserId": 595505,
                      "login": "ofux",
                      "htmlUrl": "https://github.com/ofux",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "sponsors": [
                    {
                      "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                      "name": "PSG",
                      "url": "https://www.psg.fr/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                    }
                  ],
                  "technologies": {
                    "Rust": 23314
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                  "slug": "zama",
                  "name": "Zama",
                  "shortDescription": "A super description for Zama",
                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 18,
                  "leaders": [],
                  "sponsors": [],
                  "technologies": {
                    "Shell": 3429,
                    "Cairo": 42100,
                    "Python": 45301
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "467cb27c-9726-4f94-818e-6aa49bbf5e75",
                  "slug": "zero-title-11",
                  "name": "Zero title 11",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 453,
                  "leaders": [],
                  "sponsors": [
                    {
                      "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                      "name": "Theodo",
                      "url": null,
                      "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                    },
                    {
                      "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                      "name": "Starknet Foundation",
                      "url": "https://starknet.io",
                      "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                    }
                  ],
                  "technologies": {
                    "Shell": 8324,
                    "Solidity": 837904,
                    "Makefile": 1714,
                    "JavaScript": 1027177,
                    "Ruby": 255376,
                    "Python": 6719
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "f25e3389-d681-4811-b45c-3d1106d8e478",
                  "slug": "zero-title-18",
                  "name": "Zero title 18",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "4f7bcc3e-3d3d-4a8f-8280-bb6df33382da",
                  "slug": "zero-title-19",
                  "name": "Zero title 19",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 3,
                  "leaders": [
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                  "slug": "zero-title-4",
                  "name": "Zero title 4",
                  "shortDescription": "Missing short description",
                  "logoUrl": null,
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 3,
                  "contributorCount": 4,
                  "leaders": [
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "htmlUrl": "https://github.com/oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "Dockerfile": 325,
                    "Scheme": 43698,
                    "JavaScript": 342,
                    "Haskell": 16365
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                },
                {
                  "id": "e41f44a2-464c-4c96-817f-81acb06b2523",
                  "slug": "zero-title-5",
                  "name": "Zero title 5",
                  "shortDescription": "Missing short description",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1458710211645943860.png",
                  "hiring": false,
                  "visibility": "PUBLIC",
                  "repoCount": 1,
                  "contributorCount": 297,
                  "leaders": [
                    {
                      "githubUserId": 595505,
                      "login": "ofux",
                      "htmlUrl": "https://github.com/ofux",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "sponsors": [],
                  "technologies": {
                    "Shell": 4429,
                    "Rust": 2017307,
                    "HTML": 871
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null
                }
              ],
              "technologies": [
                "Batchfile",
                "C",
                "C++",
                "CMake",
                "COBOL",
                "CSS",
                "Cairo",
                "Dart",
                "Dockerfile",
                "HTML",
                "Haskell",
                "JavaScript",
                "Jinja",
                "Jupyter Notebook",
                "Kotlin",
                "MDX",
                "Makefile",
                "Nix",
                "Objective-C",
                "PHP",
                "PLpgSQL",
                "Procfile",
                "Python",
                "Ruby",
                "Rust",
                "SCSS",
                "Scheme",
                "Shell",
                "Solidity",
                "Swift",
                "TypeScript"
              ],
              "sponsors": [
                {
                  "id": "85435c9b-da7f-4670-bf65-02b84c5da7f0",
                  "name": "AS Nancy Lorraine",
                  "url": null,
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/951523516066154017.png"
                },
                {
                  "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                  "name": "Coca Cola",
                  "url": null,
                  "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                },
                {
                  "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                  "name": "Coca Colax",
                  "url": "https://www.coca-cola-france.fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                },
                {
                  "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                  "name": "OGC Nissa Ineos",
                  "url": "https://www.ogcnice.com/fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                },
                {
                  "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                  "name": "PSG",
                  "url": "https://www.psg.fr/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                },
                {
                  "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                  "name": "Red Bull",
                  "url": "https://www.redbull.com/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                },
                {
                  "id": "eb04a5de-4802-4071-be7b-9007b563d48d",
                  "name": "Starknet Foundation",
                  "url": "https://starknet.io",
                  "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                },
                {
                  "id": "2639563e-4437-4bde-a4f4-654977c0cb39",
                  "name": "Theodo",
                  "url": null,
                  "logoUrl": "https://upload.wikimedia.org/wikipedia/fr/thumb/d/dd/Logo-theodo.png/1200px-Logo-theodo.png"
                }
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 25,
              "nextPageIndex": 0
            }
            """;
    @Autowired
    ProjectViewRepository projectViewRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    HasuraUserHelper userHelper;

    @Test
    @Order(1)
    public void should_get_a_project_by_slug() {
        // Given
        final String slug = "bretzel";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    @Order(2)
    public void should_get_a_project_by_id() {
        // Given
        final String id = "7d04163c-4187-4313-8066-61504d34fc56";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    @Order(4)
    void should_get_projects_given_anonymous_user() {
        client.get()
                .uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_JSON_RESPONSE);
    }

    @Test
    @Order(5)
    void should_get_projects_given_anonymous_user_with_sorts_and_filters() {
        client.get()
                .uri(getApiURI(PROJECTS_GET, Map.of("sort", "CONTRIBUTOR_COUNT", "technologies", "Rust", "sponsorId",
                        "2639563e-4437-4bde-a4f4-654977c0cb39", "search", "t", "pageIndex", "0", "pageSize", "100")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE);
    }

    @Test
    @Order(6)
    void should_get_projects_given_authenticated_user() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE);
    }

    @Test
    @Order(7)
    void should_get_projects_given_authenticated_user_for_mine() {
        // Given
        final var auth = userHelper.authenticatePierre();

        final ProjectViewEntity bretzel = projectViewRepository.findByKey("bretzel").orElseThrow();
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(), bretzel.getId(),
                auth.user().getGithubUserId()));

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "mine", "true")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_JSON_RESPONSE);
    }

    @Test
    @Order(8)
    public void should_get_a_private_project_by_slug() {
        // Given
        final String slug = "b-conseil";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // When a contributor gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateOlivier().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When a lead gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateUser(134486697L).jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When an invited lead gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateHayden().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);
    }

    @Test
    @Order(9)
    public void should_get_a_private_project_by_id() {
        // Given
        final String id = "27ca7e18-9e71-468f-8825-c64fe6b79d66";

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        // When a contributor gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateOlivier().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When a lead gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateUser(134486697L).jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);

        // When an invited lead gets the project
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + id))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userHelper.authenticateHayden().jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(B_CONSEIL_OVERVIEW_JSON);
    }
}
