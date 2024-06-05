package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectSponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.project.domain.model.Project;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;


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
                 "installed": true,
                 "isCurrentUserAdmin": null,
                 "isPersonal": null,
                 "installationId": 44637372
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
             "languages": [
               {
                 "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                 "name": "Typescript",
                 "slug": "typescript",
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
             "tags": []
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
                  "installed": true,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372
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
              "languages": [
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "name": "Typescript",
                  "slug": "typescript",
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
              "me": {
                "isMember": false,
                "isContributor": false,
                "isProjectLead": false,
                "isInvitedAsProjectLead": false,
                "hasApplied": false
              },
              "tags": [
                "FAST_AND_FURIOUS",
                "NEWBIES_WELCOME"
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
                  "installed": true,
                  "isCurrentUserAdmin": null,
                  "isPersonal": null,
                  "installationId": 44637372
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
              "sponsors": [
                {
                  "id": "01bc5c57-9b7c-4521-b7be-8a12861ae5f4",
                  "name": "No Sponsor",
                  "url": null,
                  "logoUrl": "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp"
                }
              ],
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
              "tags": []
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
                   "ecosystems": [
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
                     },
                     {
                       "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                       "name": "Aptos",
                       "url": "https://aptosfoundation.org/",
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                       "bannerUrl": null,
                       "slug": "aptos"
                     }
                   ],
                   "languages": [
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "slug": "typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": true,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                       "id": "fc92397c-3431-4a84-8054-845376b630a0"
                     },
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": false,
                   "tags": []
                 }
               ],
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
                  "ecosystems": [
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
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos"
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": true,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": false,
                  "tags": [
                    "FAST_AND_FURIOUS"
                  ]
                }
              ],
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134486697?v=4",
                      "id": "83612081-949a-47c4-a467-6f28f6adad6d"
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
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "ecosystems": [
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
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos"
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                      "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet"
                    }
                  ],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                      "name": "Ruby",
                      "slug": "ruby",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
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
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": false,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                      "name": "Aztec",
                      "url": "https://aztec.network/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                      "bannerUrl": null,
                      "slug": "aztec"
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                      "name": "Avail",
                      "url": "https://www.availproject.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                      "bannerUrl": null,
                      "slug": "avail"
                    }
                  ],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": true,
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
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
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "name": "Solidity",
                      "slug": "solidity",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "name": "Rust",
                      "slug": "rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "name": "Solidity",
                      "slug": "solidity",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet"
                    }
                  ],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "name": "Rust",
                      "slug": "rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": []
                }
              ],
              "languages": [
                {
                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                  "name": "Cairo",
                  "slug": "cairo",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                },
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
                  "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                  "name": "Ruby",
                  "slug": "ruby",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                },
                {
                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                  "name": "Rust",
                  "slug": "rust",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                },
                {
                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                  "name": "Solidity",
                  "slug": "solidity",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                },
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "name": "Typescript",
                  "slug": "typescript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail"
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec"
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
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "iconSlug": "brain"
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
              "languages": [
                {
                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                  "name": "Cairo",
                  "slug": "cairo",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                },
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
                  "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                  "name": "Ruby",
                  "slug": "ruby",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                },
                {
                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                  "name": "Rust",
                  "slug": "rust",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                },
                {
                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                  "name": "Solidity",
                  "slug": "solidity",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                },
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "name": "Typescript",
                  "slug": "typescript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail"
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec"
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
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet"
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
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "iconSlug": "brain"
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                       "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     },
                     {
                       "githubUserId": 26790304,
                       "login": "gaetanrecly",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                       "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   "ecosystems": [
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
                     },
                     {
                       "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                       "name": "Aptos",
                       "url": "https://aptosfoundation.org/",
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                       "bannerUrl": null,
                       "slug": "aptos"
                     }
                   ],
                   "languages": [
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                       "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                     }
                   ],
                   "ecosystems": [
                     {
                       "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                       "name": "Starknet",
                       "url": "https://www.starknet.io/en",
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                       "bannerUrl": null,
                       "slug": "starknet"
                     }
                   ],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     },
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   "languages": [],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     },
                     {
                       "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                       "name": "Ruby",
                       "slug": "ruby",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                       "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                     }
                   ],
                   "ecosystems": [],
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
                       "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                       "name": "Cairo",
                       "slug": "cairo",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                     },
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "slug": "typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [
                     {
                       "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                       "name": "Aztec",
                       "url": "https://aztec.network/",
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                       "bannerUrl": null,
                       "slug": "aztec"
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                       "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                     }
                   ],
                   "ecosystems": [
                     {
                       "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                       "name": "Avail",
                       "url": "https://www.availproject.org/",
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                       "bannerUrl": null,
                       "slug": "avail"
                     }
                   ],
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                       "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   "languages": [
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "slug": "typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                       "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                     },
                     {
                       "githubUserId": 139852598,
                       "login": "mat-yas",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                       "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                     },
                     {
                       "githubUserId": 31901905,
                       "login": "kaelsky",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                       "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                     },
                     {
                       "githubUserId": 134493681,
                       "login": "croziflette74",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                       "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                     },
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
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
                       "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                       "name": "Cairo",
                       "slug": "cairo",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                     },
                     {
                       "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                       "name": "Solidity",
                       "slug": "solidity",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                     },
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "slug": "typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                       "id": "fc92397c-3431-4a84-8054-845376b630a0"
                     },
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                       "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                     },
                     {
                       "githubUserId": 139852598,
                       "login": "mat-yas",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                       "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                     },
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     },
                     {
                       "githubUserId": 4435377,
                       "login": "Bernardstanislas",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                       "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                       "id": "46fec596-7a91-422e-8532-5f479e790217"
                     },
                     {
                       "githubUserId": 139852598,
                       "login": "mat-yas",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                       "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                     },
                     {
                       "githubUserId": 31901905,
                       "login": "kaelsky",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                       "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                     },
                     {
                       "githubUserId": 134493681,
                       "login": "croziflette74",
                       "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                       "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                     },
                     {
                       "githubUserId": 8642470,
                       "login": "gregcha",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     },
                     {
                       "githubUserId": 122993337,
                       "login": "GregGamb",
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                       "id": "743e096e-c922-4097-9e6f-8ea503055336"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                       "name": "Rust",
                       "slug": "rust",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                     },
                     {
                       "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                       "name": "Cairo",
                       "slug": "cairo",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                     },
                     {
                       "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                       "name": "Solidity",
                       "slug": "solidity",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                     },
                     {
                       "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                       "name": "Typescript",
                       "slug": "typescript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                       "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                   "languages": [
                     {
                       "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                       "name": "Python",
                       "slug": "python",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                     },
                     {
                       "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                       "name": "Cairo",
                       "slug": "cairo",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                       "bannerUrl": null,
                       "slug": "starknet"
                     }
                   ],
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
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                       "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                       "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                       "name": "Javascript",
                       "slug": "javascript",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
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
                       "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                       "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                     }
                   ],
                   "ecosystems": [],
                   "languages": [
                     {
                       "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                       "name": "Rust",
                       "slug": "rust",
                       "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                       "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                     }
                   ],
                   "isInvitedAsProjectLead": false,
                   "hasMissingGithubAppInstallation": null,
                   "tags": []
                 }
               ],
               "languages": [
                 {
                   "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                   "name": "Cairo",
                   "slug": "cairo",
                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                 },
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
                   "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                   "name": "Ruby",
                   "slug": "ruby",
                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                 },
                 {
                   "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                   "name": "Rust",
                   "slug": "rust",
                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                 },
                 {
                   "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                   "name": "Solidity",
                   "slug": "solidity",
                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                 },
                 {
                   "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                   "name": "Typescript",
                   "slug": "typescript",
                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                   "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                   "name": "Avail",
                   "url": "https://www.availproject.org/",
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                   "bannerUrl": null,
                   "slug": "avail"
                 },
                 {
                   "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                   "name": "Aztec",
                   "url": "https://aztec.network/",
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                   "bannerUrl": null,
                   "slug": "aztec"
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
                   "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                   "name": "Starknet",
                   "url": "https://www.starknet.io/en",
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                   "bannerUrl": null,
                   "slug": "starknet"
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
               "hasMore": false,
               "totalPageNumber": 1,
               "totalItemNumber": 25,
               "nextPageIndex": 0
             }
            """;

    private static final String GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_CATEGORY_FILTER_JSON_RESPONSE = """
            {
              "projects": [
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
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    },
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": []
                }
              ],
              "languages": [
                {
                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                  "name": "Python",
                  "slug": "python",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                },
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "name": "Typescript",
                  "slug": "typescript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                },
                {
                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                  "name": "Solidity",
                  "slug": "solidity",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                },
                {
                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                  "name": "Javascript",
                  "slug": "javascript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                },
                {
                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                  "name": "Cairo",
                  "slug": "cairo",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                },
                {
                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                  "name": "Rust",
                  "slug": "rust",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                },
                {
                  "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                  "name": "Ruby",
                  "slug": "ruby",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                }
              ],
              "ecosystems": [
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec"
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama"
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail"
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
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet"
                },
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos"
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "iconSlug": "brain"
                }
              ],
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 1,
              "nextPageIndex": 0
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

    @BeforeEach
    void setUp() {
        final var categoryAI = new ProjectCategoryEntity(UUID.fromString("b151c7e4-1493-4927-bb0f-8647ec98a9c5"), "ai", "AI", "brain");
        projectCategoryRepository.saveAll(List.of(
                new ProjectCategoryEntity(UUID.fromString("7a1c0dcb-2079-487c-adaa-88d425bf13ea"), "security", "Security", "lock"),
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
                .jsonPath("$.me.hasApplied")
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
                .jsonPath("$.me.hasApplied").isEqualTo(false)
                .json(BRETZEL_OVERVIEW_JSON);
    }

    @Test
    @Order(4)
    void should_get_projects_given_anonymous_user() {
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100"))).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_JSON_RESPONSE);
    }

    @Test
    @Order(5)
    void should_get_projects_given_anonymous_user_with_sorts_and_filters() {
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "CONTRIBUTOR_COUNT", "languageSlugs", "rust", "search", "t",
                        "ecosystemSlugs", "fake",
                        "pageIndex", "0", "pageSize", "100")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE);

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "CONTRIBUTOR_COUNT", "categories", "ai",
                        "pageIndex", "0", "pageSize", "100")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_CATEGORY_FILTER_JSON_RESPONSE);
    }

    @Test
    @Order(6)
    void should_get_projects_given_authenticated_user() {
        // Given
        final String jwt = userAuthHelper.authenticateAnthony().jwt();

        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100"))).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE);
    }

    @Test
    @Order(7)
    void should_get_projects_given_authenticated_user_for_mine() {
        // Given
        final var auth = userAuthHelper.authenticatePierre();

        final ProjectViewEntity bretzel = projectViewRepository.findBySlug("bretzel").orElseThrow();
        projectLeaderInvitationRepository.save(new ProjectLeaderInvitationEntity(UUID.randomUUID(), bretzel.getId(), auth.user().getGithubUserId()));

        // When
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "mine", "true")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt()).exchange()
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
                .jsonPath("$.me.hasApplied").isEqualTo(false)
                .json(B_CONSEIL_OVERVIEW_JSON);

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
                              "hiring": true,
                              "visibility": "PUBLIC",
                              "repoCount": 1,
                              "contributorCount": 559,
                              "leaders": [
                                {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                                  "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                                }
                              ],
                              "ecosystems": [
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet"
                                }
                              ],
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "name": "Javascript",
                                  "slug": "javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "slug": "typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
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
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0"
                                },
                                {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                }
                              ],
                              "ecosystems": [],
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
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                  "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                                }
                              ],
                              "ecosystems": [],
                              "languages": [],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                                  "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                                }
                              ],
                              "ecosystems": [],
                              "languages": [
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "name": "Javascript",
                                  "slug": "javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                }
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
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
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                },
                                {
                                  "githubUserId": 26790304,
                                  "login": "gaetanrecly",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                                  "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                                }
                              ],
                              "ecosystems": [],
                              "languages": [],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": []
                            }
                          ],
                          "languages": [
                            {
                              "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                              "name": "Cairo",
                              "slug": "cairo",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                            },
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
                              "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                              "name": "Ruby",
                              "slug": "ruby",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                            },
                            {
                              "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                              "name": "Rust",
                              "slug": "rust",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                            },
                            {
                              "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                              "name": "Solidity",
                              "slug": "solidity",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                            },
                            {
                              "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                              "name": "Typescript",
                              "slug": "typescript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec"
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
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet"
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
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "BIG_WHALE"))).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "projects": [],
                          "languages": [
                            {
                              "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                              "name": "Cairo",
                              "slug": "cairo",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                            },
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
                              "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                              "name": "Ruby",
                              "slug": "ruby",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                            },
                            {
                              "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                              "name": "Rust",
                              "slug": "rust",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                            },
                            {
                              "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                              "name": "Solidity",
                              "slug": "solidity",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                            },
                            {
                              "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                              "name": "Typescript",
                              "slug": "typescript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail"
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec"
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
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet"
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
                          "hasMore": false,
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "nextPageIndex": 0
                        }
                        
                        """);

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "FAST_AND_FURIOUS"))).exchange()
                // Then
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
                               "hiring": true,
                               "visibility": "PUBLIC",
                               "repoCount": 4,
                               "contributorCount": 4,
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
                               "ecosystems": [
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
                                 },
                                 {
                                   "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                   "name": "Aptos",
                                   "url": "https://aptosfoundation.org/",
                                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                   "bannerUrl": null,
                                   "slug": "aptos"
                                 }
                               ],
                               "languages": [
                                 {
                                   "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                   "name": "Typescript",
                                   "slug": "typescript",
                                   "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                   "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                 }
                               ],
                               "isInvitedAsProjectLead": false,
                               "hasMissingGithubAppInstallation": null,
                               "tags": [
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
                                   "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                   "id": "fc92397c-3431-4a84-8054-845376b630a0"
                                 },
                                 {
                                   "githubUserId": 8642470,
                                   "login": "gregcha",
                                   "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                   "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                 }
                               ],
                               "ecosystems": [],
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
                               ],
                               "isInvitedAsProjectLead": false,
                               "hasMissingGithubAppInstallation": null,
                               "tags": [
                                 "FAST_AND_FURIOUS"
                               ]
                             }
                           ],
                           "languages": [
                             {
                               "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                               "name": "Cairo",
                               "slug": "cairo",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                             },
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
                               "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                               "name": "Ruby",
                               "slug": "ruby",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                             },
                             {
                               "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                               "name": "Rust",
                               "slug": "rust",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                             },
                             {
                               "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                               "name": "Solidity",
                               "slug": "solidity",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                             },
                             {
                               "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                               "name": "Typescript",
                               "slug": "typescript",
                               "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                               "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                               "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                               "name": "Avail",
                               "url": "https://www.availproject.org/",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                               "bannerUrl": null,
                               "slug": "avail"
                             },
                             {
                               "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                               "name": "Aztec",
                               "url": "https://aztec.network/",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                               "bannerUrl": null,
                               "slug": "aztec"
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
                               "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                               "name": "Starknet",
                               "url": "https://www.starknet.io/en",
                               "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                               "bannerUrl": null,
                               "slug": "starknet"
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
                           "hasMore": false,
                           "totalPageNumber": 1,
                           "totalItemNumber": 2,
                           "nextPageIndex": 0
                         }
                        """);


        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "FAST_AND_FURIOUS,NEWBIES_WELCOME"))).exchange()
                // Then
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
                                  "hiring": true,
                                  "visibility": "PUBLIC",
                                  "repoCount": 4,
                                  "contributorCount": 4,
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
                                  "ecosystems": [
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
                                    },
                                    {
                                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                      "name": "Aptos",
                                      "url": "https://aptosfoundation.org/",
                                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                      "bannerUrl": null,
                                      "slug": "aptos"
                                    }
                                  ],
                                  "languages": [
                                    {
                                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                      "name": "Typescript",
                                      "slug": "typescript",
                                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                    }
                                  ],
                                  "isInvitedAsProjectLead": false,
                                  "hasMissingGithubAppInstallation": null,
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
                                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                                    },
                                    {
                                      "githubUserId": 8642470,
                                      "login": "gregcha",
                                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                    }
                                  ],
                                  "ecosystems": [],
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
                                  ],
                                  "isInvitedAsProjectLead": false,
                                  "hasMissingGithubAppInstallation": null,
                                  "tags": [
                                    "FAST_AND_FURIOUS"
                                  ]
                                }
                              ],
                              "languages": [
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "name": "Cairo",
                                  "slug": "cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
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
                                  "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                                  "name": "Ruby",
                                  "slug": "ruby",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "name": "Rust",
                                  "slug": "rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                  "name": "Solidity",
                                  "slug": "solidity",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "name": "Typescript",
                                  "slug": "typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                                  "name": "Avail",
                                  "url": "https://www.availproject.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                                  "bannerUrl": null,
                                  "slug": "avail"
                                },
                                {
                                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                                  "name": "Aztec",
                                  "url": "https://aztec.network/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                                  "bannerUrl": null,
                                  "slug": "aztec"
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
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet"
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
                        "tags", "NEWBIES_WELCOME,FAST_AND_FURIOUS")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_WITH_TAGS_JSON_RESPONSE);
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

    @Test
    @Order(16)
    void should_get_projects_with_good_first_issues() {
        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "5", "hasGoodFirstIssues", "true")))
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
                              "hiring": true,
                              "visibility": "PUBLIC",
                              "repoCount": 1,
                              "contributorCount": 559,
                              "leaders": [
                                {
                                  "githubUserId": 117665867,
                                  "login": "gilbertVDB17",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                                  "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                                }
                              ],
                              "ecosystems": [
                                {
                                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                                  "name": "Starknet",
                                  "url": "https://www.starknet.io/en",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                                  "bannerUrl": null,
                                  "slug": "starknet"
                                }
                              ],
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
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": [
                                "HOT_COMMUNITY",
                                "NEWBIES_WELCOME",
                                "WORK_IN_PROGRESS",
                                "FAST_AND_FURIOUS"
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
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": [
                                "HOT_COMMUNITY",
                                "NEWBIES_WELCOME",
                                "WORK_IN_PROGRESS",
                                "FAST_AND_FURIOUS"
                              ]
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
                                  "githubUserId": 134493681,
                                  "login": "croziflette74",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                  "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                                },
                                {
                                  "githubUserId": 21149076,
                                  "login": "oscarwroche",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                  "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                                },
                                {
                                  "githubUserId": 139852598,
                                  "login": "mat-yas",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                  "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                                },
                                {
                                  "githubUserId": 31901905,
                                  "login": "kaelsky",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                  "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                                },
                                {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                }
                              ],
                              "ecosystems": [],
                              "languages": [
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "slug": "cairo",
                                  "name": "Cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
                                {
                                  "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                  "slug": "solidity",
                                  "name": "Solidity",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": [
                                "HOT_COMMUNITY",
                                "NEWBIES_WELCOME",
                                "LIKELY_TO_REWARD",
                                "WORK_IN_PROGRESS",
                                "FAST_AND_FURIOUS",
                                "BIG_WHALE"
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
                                  "githubUserId": 134493681,
                                  "login": "croziflette74",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                  "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                                },
                                {
                                  "githubUserId": 122993337,
                                  "login": "GregGamb",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                                  "id": "743e096e-c922-4097-9e6f-8ea503055336"
                                },
                                {
                                  "githubUserId": 139852598,
                                  "login": "mat-yas",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                  "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                                },
                                {
                                  "githubUserId": 31901905,
                                  "login": "kaelsky",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                  "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                                },
                                {
                                  "githubUserId": 141839618,
                                  "login": "Blumebee",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                                  "id": "46fec596-7a91-422e-8532-5f479e790217"
                                },
                                {
                                  "githubUserId": 8642470,
                                  "login": "gregcha",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                                  "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                                }
                              ],
                              "ecosystems": [],
                              "languages": [
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                                },
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "slug": "cairo",
                                  "name": "Cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
                                {
                                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                                  "slug": "solidity",
                                  "name": "Solidity",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                                },
                                {
                                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                                  "slug": "typescript",
                                  "name": "Typescript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                                }
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": [
                                "HOT_COMMUNITY",
                                "NEWBIES_WELCOME",
                                "LIKELY_TO_REWARD",
                                "WORK_IN_PROGRESS",
                                "FAST_AND_FURIOUS",
                                "BIG_WHALE"
                              ]
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
                              "languages": [
                                {
                                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                                  "slug": "cairo",
                                  "name": "Cairo",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                                },
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                }
                              ],
                              "isInvitedAsProjectLead": false,
                              "hasMissingGithubAppInstallation": null,
                              "tags": [
                                "HOT_COMMUNITY",
                                "NEWBIES_WELCOME",
                                "WORK_IN_PROGRESS",
                                "FAST_AND_FURIOUS"
                              ]
                            }
                          ],
                          "languages": [
                            {
                              "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                              "slug": "typescript",
                              "name": "Typescript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                            },
                            {
                              "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                              "slug": "cairo",
                              "name": "Cairo",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                            },
                            {
                              "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                              "slug": "solidity",
                              "name": "Solidity",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                            },
                            {
                              "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                              "slug": "python",
                              "name": "Python",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                            },
                            {
                              "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                              "slug": "javascript",
                              "name": "Javascript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                            },
                            {
                              "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                              "slug": "ruby",
                              "name": "Ruby",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                            },
                            {
                              "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                              "slug": "rust",
                              "name": "Rust",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                            }
                          ],
                          "ecosystems": [
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec"
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                              "bannerUrl": null,
                              "slug": "zama"
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail"
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
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet"
                            },
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "bannerUrl": null,
                              "slug": "aptos"
                            }
                          ],
                          "categories": [
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "name": "AI",
                              "iconSlug": "brain"
                            }
                          ],
                          "hasMore": true,
                          "totalPageNumber": 2,
                          "totalItemNumber": 7,
                          "nextPageIndex": 1
                        }
                        """);
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
                  "ecosystems": [
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
                    },
                    {
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos"
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": true,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "NEWBIES_WELCOME",
                    "BIG_WHALE"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                      "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet"
                    }
                  ],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "WORK_IN_PROGRESS"
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
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                      "name": "Ruby",
                      "slug": "ruby",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "name": "Rust",
                      "slug": "rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE",
                    "WORK_IN_PROGRESS"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                      "name": "Aztec",
                      "url": "https://aztec.network/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                      "bannerUrl": null,
                      "slug": "aztec"
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
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS"
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "ecosystems": [
                    {
                      "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                      "name": "Avail",
                      "url": "https://www.availproject.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                      "bannerUrl": null,
                      "slug": "avail"
                    }
                  ],
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                      "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "WORK_IN_PROGRESS"
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
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "name": "Solidity",
                      "slug": "solidity",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE",
                    "WORK_IN_PROGRESS"
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                      "id": "fc92397c-3431-4a84-8054-845376b630a0"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": false,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE"
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "LIKELY_TO_REWARD",
                    "BIG_WHALE"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 134493681,
                      "login": "croziflette74",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                      "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "name": "Rust",
                      "slug": "rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    },
                    {
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "name": "Solidity",
                      "slug": "solidity",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                    },
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "name": "Typescript",
                      "slug": "typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE",
                    "WORK_IN_PROGRESS"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                  "languages": [
                    {
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "name": "Python",
                      "slug": "python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                      "name": "Cairo",
                      "slug": "cairo",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "WORK_IN_PROGRESS"
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
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet"
                    }
                  ],
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
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "FAST_AND_FURIOUS",
                    "WORK_IN_PROGRESS"
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                      "name": "Javascript",
                      "slug": "javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
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
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                      "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                    }
                  ],
                  "ecosystems": [],
                  "languages": [
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "name": "Rust",
                      "slug": "rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "LIKELY_TO_REWARD",
                    "FAST_AND_FURIOUS",
                    "BIG_WHALE",
                    "WORK_IN_PROGRESS"
                  ]
                }
              ],
              "languages": [
                {
                  "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                  "name": "Cairo",
                  "slug": "cairo",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                },
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
                  "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                  "name": "Ruby",
                  "slug": "ruby",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                },
                {
                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                  "name": "Rust",
                  "slug": "rust",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                },
                {
                  "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                  "name": "Solidity",
                  "slug": "solidity",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                },
                {
                  "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                  "name": "Typescript",
                  "slug": "typescript",
                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
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
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail"
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec"
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
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet"
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
              "hasMore": false,
              "totalPageNumber": 1,
              "totalItemNumber": 25,
              "nextPageIndex": 0
            }
            """;
}
