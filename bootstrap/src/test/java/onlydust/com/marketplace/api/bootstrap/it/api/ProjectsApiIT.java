package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectSponsorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectTagRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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
              "moreInfos": [],
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 4,
              "topContributors": [
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                },
                {
                  "githubUserId": 52197971,
                  "login": "jb1011",
                  "htmlUrl": "https://github.com/jb1011",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                },
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "htmlUrl": "https://github.com/antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
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
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
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
                  "htmlUrl": "https://github.com/KasarLabs",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/119948009?v=4",
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
              "hasRemainingBudget": true,
              "rewardSettings": {
                "ignorePullRequests": false,
                "ignoreIssues": false,
                "ignoreCodeReviews": false,
                "ignoreContributionsBefore": null
              },
              "indexingComplete": true,
              "indexedAt": "2023-12-04T14:35:10.986567Z"
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
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp"
                },
                {
                  "githubUserId": 52197971,
                  "login": "jb1011",
                  "htmlUrl": "https://github.com/jb1011",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/52197971?v=4"
                },
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "htmlUrl": "https://github.com/antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
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
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
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
                  "htmlUrl": "https://github.com/KasarLabs",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/119948009?v=4",
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
              "invitedLeaders": [
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "htmlUrl": "https://github.com/PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "id": null
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
                "isInvitedAsProjectLead": false,
                "hasApplied": false
              },
              "tags": [
                "NEWBIES_WELCOME",
                "FAST_AND_FURIOUS"
              ]
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
                  "htmlUrl": "https://github.com/AnthonyBuisset",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
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
                  "installationId": 44637372
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
              "hasRemainingBudget": true,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "Shell": 732,
                    "CSS": 423688,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": true,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": false,
                  "tags": []
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                }
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 2,
              "nextPageIndex": 0
            }
            """;
    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_WITH_TAGS_JSON_RESPONSE = """
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "Shell": 732,
                    "CSS": 423688,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": true,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": false,
                  "tags": [
                    "FAST_AND_FURIOUS"
                  ]
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 114,
                    "Makefile": 49,
                    "JavaScript": 15570,
                    "HTML": 235
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    },
                    {
                      "id": "dd6f737e-2a9d-40b9-be62-8f64ec157989",
                      "name": "Optimism",
                      "url": "https://www.optimism.io/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12058007825795511084.png"
                    },
                    {
                      "id": "f7821bfb-df73-464c-9d87-a94dfb4f5aef",
                      "name": "Lava",
                      "url": "https://www.lavanet.xyz/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15939879525439639427.jpg"
                    }
                  ],
                  "technologies": {
                    "CSS": 323507,
                    "Rust": 527,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "Shell": 732,
                    "CSS": 423688,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    }
                  ],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Ruby": 9708
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3592913,
                    "MDX": 2520,
                    "Shell": 12431,
                    "CSS": 6065,
                    "Cairo": 72428,
                    "PLpgSQL": 1372,
                    "JavaScript": 26108,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": false,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    },
                    {
                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                      "name": "Aztec",
                      "url": "https://aztec.network/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                      "name": "Avail",
                      "url": "https://www.availproject.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "Dockerfile": 325,
                    "Shell": 12431,
                    "CSS": 6065,
                    "PLpgSQL": 1372,
                    "JavaScript": 24365,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "COBOL": 10808,
                    "JavaScript": 6987
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": true,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "C++": 2226,
                    "CSS": 323507,
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
                    "Python": 6719,
                    "Dart": 121265
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 609777,
                    "Solidity": 420744,
                    "Makefile": 367,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 5416
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Jinja": 2398,
                    "C++": 23419,
                    "CSS": 1396,
                    "Rust": 527,
                    "C": 1425,
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    }
                  ],
                  "ecosystems": [],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Rust": 23314
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 3429,
                    "Cairo": 42100,
                    "Python": 45301
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Dockerfile": 325,
                    "Scheme": 43698,
                    "JavaScript": 342,
                    "Haskell": 16365
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 4429,
                    "Rust": 2017307,
                    "HTML": 871
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 26,
              "nextPageIndex": 0
            }
            """;
    private static final String GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE = """
            {
              "projects": [],
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
              ],
              "hasMore": false,
              "totalPageNumber": 0,
              "totalItemNumber": 0,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 114,
                    "Makefile": 49,
                    "JavaScript": 15570,
                    "HTML": 235
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 190809,
                    "Dockerfile": 1982,
                    "Shell": 732,
                    "CSS": 423688,
                    "Rust": 408641,
                    "SCSS": 98360,
                    "JavaScript": 62716,
                    "HTML": 121874
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    }
                  ],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Ruby": 9708
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3592913,
                    "MDX": 2520,
                    "Shell": 12431,
                    "CSS": 6065,
                    "Cairo": 72428,
                    "PLpgSQL": 1372,
                    "JavaScript": 26108,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    },
                    {
                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                      "name": "Aztec",
                      "url": "https://aztec.network/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                      "name": "Avail",
                      "url": "https://www.availproject.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "Dockerfile": 325,
                    "Shell": 12431,
                    "CSS": 6065,
                    "PLpgSQL": 1372,
                    "JavaScript": 24365,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "COBOL": 10808,
                    "JavaScript": 6987
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "C++": 2226,
                    "CSS": 323507,
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
                    "Python": 6719,
                    "Dart": 121265
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 609777,
                    "Solidity": 420744,
                    "Makefile": 367,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 5416
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Jinja": 2398,
                    "C++": 23419,
                    "CSS": 1396,
                    "Rust": 527,
                    "C": 1425,
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 3175211,
                    "MDX": 2520,
                    "CSS": 6065,
                    "Shell": 12431,
                    "PLpgSQL": 1372,
                    "JavaScript": 24023,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    }
                  ],
                  "ecosystems": [],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Rust": 23314
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 3429,
                    "Cairo": 42100,
                    "Python": 45301
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Dockerfile": 325,
                    "Scheme": 43698,
                    "JavaScript": 342,
                    "Haskell": 16365
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 4429,
                    "Rust": 2017307,
                    "HTML": 871
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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

    @Test
    @Order(1)
    public void should_get_a_project_by_slug() {
        // Given
        final String slug = "bretzel";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(BRETZEL_OVERVIEW_JSON);

        // When user is authenticated
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().jsonPath("$.me.isMember").isEqualTo(false).jsonPath("$.me.isContributor").isEqualTo(false).jsonPath("$.me.isProjectLead").isEqualTo(false).jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false).jsonPath("$.me.hasApplied").isEqualTo(false).json(BRETZEL_OVERVIEW_JSON);
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
                .jsonPath("$.me.hasApplied").isEqualTo(false).json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    @Order(4)
    void should_get_projects_given_anonymous_user() {
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100"))).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(GET_PROJECTS_FOR_ANONYMOUS_USER_JSON_RESPONSE);
    }

    @Test
    @Order(5)
    void should_get_projects_given_anonymous_user_with_sorts_and_filters() {
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "CONTRIBUTOR_COUNT", "technologies", "Rust", "ecosystemId", "c848d288-e6d9-4c93-ad8b" +
                                                                                                                            "-1db94483aaa6", "search", "t",
                        "pageIndex", "0", "pageSize",
                        "100"))).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE);
    }

    @Test
    @Order(6)
    void should_get_projects_given_authenticated_user() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100"))).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE);
    }

    @Test
    @Order(7)
    void should_get_projects_given_authenticated_user_for_mine() {
        // Given
        final var auth = userAuthHelper.authenticatePierre();

        final ProjectViewEntity bretzel = projectViewRepository.findByKey("bretzel").orElseThrow();
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(), bretzel.getId(), auth.user().getGithubUserId()));

        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "mine", "true")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_JSON_RESPONSE);
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
                .jsonPath("$.me.hasApplied").isEqualTo(false).json(B_CONSEIL_OVERVIEW_JSON);

        // When a lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateUser(134486697L).jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().jsonPath("$.me.isMember").isEqualTo(true).jsonPath("$.me.isContributor").isEqualTo(false).jsonPath("$.me.isProjectLead").isEqualTo(true).jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(false).jsonPath("$.me.hasApplied").isEqualTo(false).json(B_CONSEIL_OVERVIEW_JSON);

        // When an invited lead gets the project
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().jsonPath("$.me.isMember").isEqualTo(true).jsonPath("$.me.isContributor").isEqualTo(false).jsonPath("$.me.isProjectLead").isEqualTo(false).jsonPath("$.me.isInvitedAsProjectLead").isEqualTo(true).jsonPath("$.me.hasApplied").isEqualTo(false).json(B_CONSEIL_OVERVIEW_JSON);
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
                .jsonPath("$.me.hasApplied").isEqualTo(false)
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
                .jsonPath("$.me.hasApplied").isEqualTo(false)
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
                .jsonPath("$.me.hasApplied").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);
    }

    @Test
    @Order(10)
    public void should_update_project_ranking() {
        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "5", "sort", "RANK")))
                // Then
                .exchange().expectStatus().is2xxSuccessful().expectBody().json("""
                        {
                          "projects": [
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
                              "ecosystems": [
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                                }
                              ],
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
                              "isMissingGithubAppInstallation": null,
                              "tags": []
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                }
                              ],
                              "ecosystems": [],
                              "technologies": {
                                "TypeScript": 3175211,
                                "MDX": 2520,
                                "CSS": 6065,
                                "Shell": 12431,
                                "PLpgSQL": 1372,
                                "JavaScript": 24023,
                                "HTML": 1520
                              },
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": []
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                  "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                                }
                              ],
                              "ecosystems": [],
                              "technologies": {
                                "Rust": 23314
                              },
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": []
                            },
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                                  "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                                }
                              ],
                              "ecosystems": [],
                              "technologies": {},
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": []
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                },
                                {
                                  "githubUserId": 26790304,
                                  "login": "gaetanrecly",
                                  "htmlUrl": null,
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                                  "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                                }
                              ],
                              "ecosystems": [],
                              "technologies": {
                                "CSS": 114,
                                "Makefile": 49,
                                "JavaScript": 15570,
                                "HTML": 235
                              },
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": []
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
                          ],
                          "hasMore": true,
                          "totalPageNumber": 5,
                          "totalItemNumber": 25,
                          "nextPageIndex": 1
                        }
                        """);
    }

    @Autowired
    public ProjectTagRepository projectTagRepository;

    @Test
    @Order(11)
    void should_get_projects_given_anonymous_user_and_project_tags() {
        // Given
        projectTagRepository.saveAll(List.of(
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
                                                .tag(ProjectTagEntity.ProjectTagEnumEntity.NEWBIES_WELCOME)
                                                .build()
                                ).build(),
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"))
                                                .tag(ProjectTagEntity.ProjectTagEnumEntity.FAST_AND_FURIOUS)
                                                .build()
                                ).build(),
                        ProjectTagEntity.builder()
                                .id(
                                        ProjectTagEntity.Id.builder()
                                                .projectId(UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"))
                                                .tag(ProjectTagEntity.ProjectTagEnumEntity.FAST_AND_FURIOUS)
                                                .build()
                                ).build()
                )
        );

        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "BIG_WHALE"))).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json("""
                        {
                          "projects": [],
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
                          ],
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                         """);

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "FAST_AND_FURIOUS"))).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json("""
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                              "ecosystems": [
                                {
                                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                  "name": "Ethereum",
                                  "url": "https://ethereum.foundation/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                                },
                                {
                                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                  "name": "Zama",
                                  "url": "https://www.zama.ai/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                                },
                                {
                                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                  "name": "Aptos",
                                  "url": "https://aptosfoundation.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                                }
                              ],
                              "technologies": {
                                "TypeScript": 190809,
                                "Dockerfile": 1982,
                                "Shell": 732,
                                "CSS": 423688,
                                "Rust": 408641,
                                "SCSS": 98360,
                                "JavaScript": 62716,
                                "HTML": 121874
                              },
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": [
                                "NEWBIES_WELCOME",
                                "FAST_AND_FURIOUS"
                              ]
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                }
                              ],
                              "ecosystems": [],
                              "technologies": {
                                "TypeScript": 3175211,
                                "MDX": 2520,
                                "CSS": 6065,
                                "Shell": 12431,
                                "PLpgSQL": 1372,
                                "JavaScript": 24023,
                                "HTML": 1520
                              },
                              "isInvitedAsProjectLead": false,
                              "isMissingGithubAppInstallation": null,
                              "tags": [
                                "FAST_AND_FURIOUS"
                              ]
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
                          ],
                          "hasMore": false,
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "nextPageIndex": 0
                        }
                        """);


        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "FAST_AND_FURIOUS,NEWBIES_WELCOME"))).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json("""
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
                                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                                  "ecosystems": [
                                    {
                                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                      "name": "Ethereum",
                                      "url": "https://ethereum.foundation/",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                                    },
                                    {
                                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                      "name": "Zama",
                                      "url": "https://www.zama.ai/",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                                    },
                                    {
                                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                      "name": "Aptos",
                                      "url": "https://aptosfoundation.org/",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
                                    }
                                  ],
                                  "technologies": {
                                    "TypeScript": 190809,
                                    "Dockerfile": 1982,
                                    "Shell": 732,
                                    "CSS": 423688,
                                    "Rust": 408641,
                                    "SCSS": 98360,
                                    "JavaScript": 62716,
                                    "HTML": 121874
                                  },
                                  "isInvitedAsProjectLead": false,
                                  "isMissingGithubAppInstallation": null,
                                  "tags": [
                                    "NEWBIES_WELCOME",
                                    "FAST_AND_FURIOUS"
                                  ]
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
                                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                    }
                                  ],
                                  "ecosystems": [],
                                  "technologies": {
                                    "TypeScript": 3175211,
                                    "MDX": 2520,
                                    "CSS": 6065,
                                    "Shell": 12431,
                                    "PLpgSQL": 1372,
                                    "JavaScript": 24023,
                                    "HTML": 1520
                                  },
                                  "isInvitedAsProjectLead": false,
                                  "isMissingGithubAppInstallation": null,
                                  "tags": [
                                    "FAST_AND_FURIOUS"
                                  ]
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
                              ],
                              "hasMore": false,
                              "totalPageNumber": 1,
                              "totalItemNumber": 2,
                              "nextPageIndex": 0
                            }
                        """);
    }

    @Test
    @Order(12)
    void should_get_projects_given_authenticated_user_for_mine_with_tags() {
        // Given
        final var auth = userAuthHelper.authenticatePierre();
        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "mine", "true",
                        "tags", "NEWBIES_WELCOME,FAST_AND_FURIOUS"))).header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody().json(GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_WITH_TAGS_JSON_RESPONSE);
    }

    @Test
    @Order(13)
    public void should_get_a_project_by_slug_with_tags() {
        // Given
        final String slug = "bretzel";

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + userAuthHelper.authenticateHayden().jwt()).exchange()
                // Then
                .expectStatus().is2xxSuccessful().expectBody()
                .json(BRETZEL_OVERVIEW_WITH_TAGS_JSON);
    }

    @Autowired
    PostgresProjectAdapter projectStoragePort;

    @Test
    @Order(14)
    void should_update_projects_tags() throws ParseException {
        // Given
        projectStoragePort.updateProjectsTags(new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2000"));
        final var auth = userAuthHelper.authenticatePierre();

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_AFTER_TAGS_UPDATE_JSON_RESPONSE);
    }


    @Autowired
    ProjectSponsorRepository projectSponsorRepository;

    @Test
    @Order(15)
    public void should_get_a_project_by_slug_with_active_sponsors_only() {
        // Given
        final String slug = "bretzel";
        projectSponsorRepository.save(new ProjectSponsorEntity(
                UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56"),
                UUID.fromString("0980c5ab-befc-4314-acab-777fbf970cbb"),
                Date.from(ZonedDateTime.now().minusMonths(6).minusDays(1).toInstant())));

        // When
        client.get().uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + slug)).exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.sponsors").isArray()
                .jsonPath("$.sponsors.length()").isEqualTo(1)
                .jsonPath("$.sponsors[0].id").isEqualTo("c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f");
    }

    private static final String GET_PROJECTS_AFTER_TAGS_UPDATE_JSON_RESPONSE = """
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png"
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png"
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "BIG_WHALE",
                    "NEWBIES_WELCOME"
                  ]
                },
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 114,
                    "Makefile": 49,
                    "JavaScript": 15570,
                    "HTML": 235
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "BIG_WHALE"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    }
                  ],
                  "technologies": {
                    "TypeScript": 7052833,
                    "MDX": 109316,
                    "Dockerfile": 2591,
                    "CSS": 41229,
                    "Shell": 6831,
                    "Procfile": 37,
                    "JavaScript": 56416,
                    "PHP": 1205,
                    "HTML": 119986
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Ruby": 9708
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3592913,
                    "CSS": 6065,
                    "Shell": 12431,
                    "Cairo": 72428,
                    "PLpgSQL": 1372,
                    "JavaScript": 26108,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "LIKELY_TO_REWARD",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
                    },
                    {
                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                      "name": "Aztec",
                      "url": "https://aztec.network/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg"
                    }
                  ],
                  "technologies": {
                    "MDX": 2520,
                    "CSS": 6065,
                    "C++": 2226,
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
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                  "ecosystems": [
                    {
                      "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                      "name": "Avail",
                      "url": "https://www.availproject.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png"
                    }
                  ],
                  "technologies": {
                    "MDX": 2520,
                    "TypeScript": 3175211,
                    "Dockerfile": 325,
                    "Shell": 12431,
                    "CSS": 6065,
                    "PLpgSQL": 1372,
                    "JavaScript": 24365,
                    "HTML": 1520
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "COBOL": 10808,
                    "JavaScript": 6987
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME"
                  ]
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
                  "ecosystems": [],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                  "ecosystems": [],
                  "technologies": {
                    "TypeScript": 609777,
                    "Solidity": 420744,
                    "Makefile": 367,
                    "HTML": 771,
                    "Nix": 85,
                    "Python": 5416
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 1396,
                    "Jinja": 2398,
                    "C++": 23419,
                    "Rust": 527,
                    "C": 1425,
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
                    "Python": 1676320,
                    "Dart": 204844
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "LIKELY_TO_REWARD",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
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
                  "isMissingGithubAppInstallation": false,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
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
                  "ecosystems": [],
                  "technologies": {
                    "CSS": 323507,
                    "SCSS": 102453,
                    "JavaScript": 58624,
                    "HTML": 169898
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "BIG_WHALE",
                    "LIKELY_TO_REWARD"
                  ]
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
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "htmlUrl": "https://github.com/mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
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
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "htmlUrl": "https://github.com/gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "htmlUrl": null,
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    }
                  ],
                  "ecosystems": [],
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
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "LIKELY_TO_REWARD",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Rust": 23314
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 3429,
                    "Cairo": 42100,
                    "Python": 45301
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png"
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
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {},
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": []
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
                  "ecosystems": [],
                  "technologies": {
                    "Dockerfile": 325,
                    "Scheme": 43698,
                    "JavaScript": 342,
                    "Haskell": 16365
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "NEWBIES_WELCOME"
                  ]
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "technologies": {
                    "Shell": 4429,
                    "Rust": 2017307,
                    "HTML": 871
                  },
                  "isInvitedAsProjectLead": false,
                  "isMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "BIG_WHALE",
                    "LIKELY_TO_REWARD",
                    "WORK_IN_PROGRESS",
                    "FAST_AND_FURIOUS",
                    "NEWBIES_WELCOME"
                  ]
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
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 25,
              "nextPageIndex": 0
            }
            """;
}
