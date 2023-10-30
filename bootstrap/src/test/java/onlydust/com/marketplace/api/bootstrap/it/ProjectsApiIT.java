package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
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
              "shortDescription": "A project for people who love fruits",
              "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
              "moreInfoUrl": "https://bretzel.club/",
              "hiring": true,
              "visibility": "PUBLIC",
              "contributorCount": 2,
              "topContributors": [
                {
                  "githubUserId": 74653697,
                  "login": "antiyro",
                  "htmlUrl": "https://github.com/antiyro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/74653697?v=4"
                },
                {
                  "githubUserId": 8642470,
                  "login": "gregcha",
                  "htmlUrl": "https://github.com/gregcha",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4"
                }
              ],
              "repos": [
                {
                  "id": 380954304,
                  "owner": "gregcha",
                  "name": "bretzel-app",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-app",
                  "hasIssues": true
                },
                {
                  "id": 466482535,
                  "owner": "gregcha",
                  "name": "bretzel-ressources",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-ressources",
                  "hasIssues": true
                },
                {
                  "id": 659718526,
                  "owner": "KasarLabs",
                  "name": "deoxys-telemetry",
                  "description": "Deoxys Telemetry service",
                  "stars": 0,
                  "forkCount": 1,
                  "htmlUrl": "https://github.com/KasarLabs/deoxys-telemetry",
                  "hasIssues": false
                },
                {
                  "id": 452047076,
                  "owner": "gregcha",
                  "name": "bretzel-site",
                  "description": "",
                  "stars": 0,
                  "forkCount": 0,
                  "htmlUrl": "https://github.com/gregcha/bretzel-site",
                  "hasIssues": true
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
                "TypeScript": 189275,
                "Dockerfile": 1982,
                "CSS": 422216,
                "Shell": 732,
                "Rust": 407023,
                "SCSS": 98360,
                "JavaScript": 62717,
                "HTML": 121906
              },
              "remainingUsdBudget": 99250.00
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
                        "contributorCount": 2,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "pacovilletard",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                                "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                                "name": "OGC Nissa Ineos",
                                "url": null,
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
                            "TypeScript": 757100,
                            "Dockerfile": 7928,
                            "CSS": 1688864,
                            "Shell": 2928,
                            "Rust": 1628092,
                            "SCSS": 393440,
                            "JavaScript": 250868,
                            "HTML": 487624
                        },
                        "isInvitedAsProjectLead": true
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
                        "contributorCount": 17,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "PierreOucif",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 5602900,
                            "CSS": 11070,
                            "Shell": 22948,
                            "PLpgSQL": 2744,
                            "JavaScript": 47730,
                            "HTML": 3040
                        },
                        "isInvitedAsProjectLead": false
                    }
                ],
                "technologies": [
                    "TypeScript",
                    "CSS",
                    "Shell",
                    "PLpgSQL",
                    "JavaScript",
                    "HTML",
                    "Dockerfile",
                    "Rust",
                    "SCSS"
                ],
                "sponsors": ["OGC Nissa Ineos", "Coca Cola"]
            }""";
    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE = """
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
                        "contributorCount": 506,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gilbertvandenbruck",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                                "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "MDX": 108632,
                            "TypeScript": 6300439,
                            "CSS": 39918,
                            "Shell": 6831,
                            "Procfile": 37,
                            "JavaScript": 54821,
                            "PHP": 1205,
                            "HTML": 119986
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
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
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                        "slug": "b-conseil",
                        "name": "B Conseil",
                        "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                        "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                        "hiring": true,
                        "visibility": "PUBLIC",
                        "repoCount": 2,
                        "contributorCount": 3,
                        "leaders": [
                            {
                                "githubUserId": null,
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
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 20,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "AnthonyBuisset",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 3219152,
                            "CSS": 5535,
                            "Shell": 11474,
                            "Cairo": 72428,
                            "PLpgSQL": 1372,
                            "JavaScript": 25950,
                            "HTML": 1520
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 38,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "kaelsky",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "croziflette74",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                            },
                            {
                                "githubUserId": null,
                                "login": "Blumebee",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/141839618?v=4",
                                "id": "46fec596-7a91-422e-8532-5f479e790217"
                            },
                            {
                                "githubUserId": null,
                                "login": "GregGamb",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/122993337?v=4",
                                "id": "743e096e-c922-4097-9e6f-8ea503055336"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                                "name": "Red Bull",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            }
                        ],
                        "technologies": {
                            "TypeScript": 3658662,
                            "Dockerfile": 34164,
                            "Shell": 21396,
                            "Rust": 2265690,
                            "Solidity": 2527884,
                            "Cairo": 2688144,
                            "Makefile": 13152,
                            "JavaScript": 32136,
                            "HTML": 4626,
                            "Nix": 510,
                            "Python": 69228
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 621,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "kaelsky",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "croziflette74",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                            },
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "C++": 117095,
                            "CSS": 6980,
                            "Jinja": 11990,
                            "Rust": 2635,
                            "C": 7125,
                            "CMake": 94310,
                            "Makefile": 11070,
                            "HTML": 36515,
                            "Jupyter Notebook": 2886855,
                            "Kotlin": 700,
                            "TypeScript": 3156780,
                            "Dockerfile": 31400,
                            "Shell": 94930,
                            "Solidity": 2358600,
                            "Batchfile": 2795,
                            "Cairo": 3917950,
                            "JavaScript": 19313520,
                            "Objective-C": 190,
                            "Swift": 11920,
                            "Nix": 425,
                            "Ruby": 14015,
                            "Python": 8467960,
                            "Dart": 978090
                        },
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                        "slug": "mooooooonlight",
                        "name": "Mooooooonlight",
                        "shortDescription": "hello la team",
                        "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                        "hiring": false,
                        "visibility": "PUBLIC",
                        "repoCount": 5,
                        "contributorCount": 2100,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
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
                                "url": null,
                                "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                            }
                        ],
                        "technologies": {
                            "C++": 93032,
                            "CSS": 139094,
                            "Rust": 907114,
                            "C": 10454,
                            "CMake": 920,
                            "PLpgSQL": 2744,
                            "Makefile": 378,
                            "HTML": 244612,
                            "Kotlin": 2762,
                            "TypeScript": 5645842,
                            "Dockerfile": 650,
                            "Shell": 27358,
                            "CoffeeScript": 34960,
                            "JavaScript": 8278736,
                            "Objective-C": 76,
                            "Swift": 808,
                            "Dart": 242530,
                            "Python": 518
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "ofux",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                                "name": "PSG",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                            }
                        ],
                        "technologies": {
                            "Rust": 23314
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "Bernardstanislas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                                "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "CSS": 1294028,
                            "SCSS": 409812,
                            "JavaScript": 234496,
                            "HTML": 679592
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 2,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "pacovilletard",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                                "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                                "name": "OGC Nissa Ineos",
                                "url": null,
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
                            "TypeScript": 757100,
                            "Dockerfile": 7928,
                            "CSS": 1688864,
                            "Shell": 2928,
                            "Rust": 1628092,
                            "SCSS": 393440,
                            "JavaScript": 250868,
                            "HTML": 487624
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 17,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 2801450,
                            "Dockerfile": 325,
                            "CSS": 5535,
                            "Shell": 11474,
                            "PLpgSQL": 1372,
                            "JavaScript": 24207,
                            "HTML": 1520
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
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
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                            },
                            {
                                "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                                "name": "Coca Colax",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                            },
                            {
                                "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                "name": "Coca Cola",
                                "url": null,
                                "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                            }
                        ],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
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
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                            }
                        ],
                        "technologies": {
                            "CSS": 456,
                            "Makefile": 196,
                            "JavaScript": 62280,
                            "HTML": 940
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 8,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "AnthonyBuisset",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "COBOL": 10808,
                            "JavaScript": 6987
                        },
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                        "slug": "zero-title-4",
                        "name": "Zero title 4",
                        "shortDescription": "Missing short description",
                        "logoUrl": null,
                        "hiring": false,
                        "visibility": "PUBLIC",
                        "repoCount": 4,
                        "contributorCount": 2085,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "C++": 44290,
                            "CSS": 64012,
                            "Scheme": 43698,
                            "C": 5227,
                            "Makefile": 189,
                            "HTML": 120786,
                            "TypeScript": 21471,
                            "Dockerfile": 325,
                            "Shell": 2205,
                            "CoffeeScript": 17480,
                            "JavaScript": 4115503,
                            "Haskell": 16365,
                            "Python": 259
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 280,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "ofux",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "Shell": 4429,
                            "Rust": 1985261,
                            "HTML": 871
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 17,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "PierreOucif",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 5602900,
                            "CSS": 11070,
                            "Shell": 22948,
                            "PLpgSQL": 2744,
                            "JavaScript": 47730,
                            "HTML": 3040
                        },
                        "isInvitedAsProjectLead": false
                    }
                ],
                "technologies": [
                    "Procfile",
                    "C",
                    "Scheme",
                    "CMake",
                    "Makefile",
                    "HTML",
                    "Jupyter Notebook",
                    "TypeScript",
                    "Shell",
                    "Solidity",
                    "SCSS",
                    "JavaScript",
                    "PHP",
                    "Objective-C",
                    "Haskell",
                    "Ruby",
                    "Python",
                    "MDX",
                    "CSS",
                    "C++",
                    "Jinja",
                    "Rust",
                    "PLpgSQL",
                    "COBOL",
                    "Kotlin",
                    "Dockerfile",
                    "CoffeeScript",
                    "Batchfile",
                    "Cairo",
                    "Swift",
                    "Nix",
                    "Dart"
                ],
                "sponsors": [
                    "Starknet Foundation",
                    "PSG",
                    "Coca Colax",
                    "Theodo",
                    "AS Nancy Lorraine",
                    "Coca Cola",
                    "Red Bull",
                    "OGC Nissa Ineos"
                ]
            }""";
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
                        "repoCount": 5,
                        "contributorCount": 2100,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
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
                            }
                        ],
                        "technologies": {
                            "Rust": 453557
                        },
                        "isInvitedAsProjectLead": false
                    }
                ],
                "technologies": [
                    "Rust"
                ],
                "sponsors": [
                    "Theodo"
                ]
            }""";
    private static final String GET_PROJECTS_FOR_ANONYMOUS_USER_JSON_RESPONSE = """
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
                        "contributorCount": 506,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gilbertvandenbruck",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/117665867?v=4",
                                "id": "9a779f53-5762-4110-94b8-5596bbbd74ec"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "MDX": 108632,
                            "TypeScript": 6300439,
                            "CSS": 39918,
                            "Shell": 6831,
                            "Procfile": 37,
                            "JavaScript": 54821,
                            "PHP": 1205,
                            "HTML": 119986
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
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
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                        "slug": "b-conseil",
                        "name": "B Conseil",
                        "shortDescription": "Nous sommes B.Conseil, la bonne gestion du Crédit d’Impôt Recherche.",
                        "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                        "hiring": true,
                        "visibility": "PUBLIC",
                        "repoCount": 2,
                        "contributorCount": 3,
                        "leaders": [
                            {
                                "githubUserId": null,
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
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 20,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "AnthonyBuisset",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 3219152,
                            "CSS": 5535,
                            "Shell": 11474,
                            "Cairo": 72428,
                            "PLpgSQL": 1372,
                            "JavaScript": 25950,
                            "HTML": 1520
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 38,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "kaelsky",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "croziflette74",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                            },
                            {
                                "githubUserId": null,
                                "login": "Blumebee",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/141839618?v=4",
                                "id": "46fec596-7a91-422e-8532-5f479e790217"
                            },
                            {
                                "githubUserId": null,
                                "login": "GregGamb",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/122993337?v=4",
                                "id": "743e096e-c922-4097-9e6f-8ea503055336"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "0d66ba03-cecb-45a4-ab7d-98f0cc18a3aa",
                                "name": "Red Bull",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13218160580172982881.jpg"
                            }
                        ],
                        "technologies": {
                            "TypeScript": 3658662,
                            "Dockerfile": 34164,
                            "Shell": 21396,
                            "Rust": 2265690,
                            "Solidity": 2527884,
                            "Cairo": 2688144,
                            "Makefile": 13152,
                            "JavaScript": 32136,
                            "HTML": 4626,
                            "Nix": 510,
                            "Python": 69228
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 621,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "kaelsky",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                                "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                            },
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "croziflette74",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/134493681?v=4",
                                "id": "44e078b7-d095-49f2-a7b3-647149337dc5"
                            },
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "C++": 117095,
                            "CSS": 6980,
                            "Jinja": 11990,
                            "Rust": 2635,
                            "C": 7125,
                            "CMake": 94310,
                            "Makefile": 11070,
                            "HTML": 36515,
                            "Jupyter Notebook": 2886855,
                            "Kotlin": 700,
                            "TypeScript": 3156780,
                            "Dockerfile": 31400,
                            "Shell": 94930,
                            "Batchfile": 2795,
                            "Solidity": 2358600,
                            "Cairo": 3917950,
                            "JavaScript": 19313520,
                            "Objective-C": 190,
                            "Swift": 11920,
                            "Nix": 425,
                            "Ruby": 14015,
                            "Dart": 978090,
                            "Python": 8467960
                        },
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                        "slug": "mooooooonlight",
                        "name": "Mooooooonlight",
                        "shortDescription": "hello la team",
                        "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                        "hiring": false,
                        "visibility": "PUBLIC",
                        "repoCount": 5,
                        "contributorCount": 2100,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
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
                                "url": null,
                                "logoUrl": "https://logos-marques.com/wp-content/uploads/2020/09/Logo-Instagram-1.png"
                            }
                        ],
                        "technologies": {
                            "C++": 93032,
                            "CSS": 139094,
                            "Rust": 907114,
                            "C": 10454,
                            "CMake": 920,
                            "Makefile": 378,
                            "PLpgSQL": 2744,
                            "HTML": 244612,
                            "Kotlin": 2762,
                            "TypeScript": 5645842,
                            "Dockerfile": 650,
                            "Shell": 27358,
                            "CoffeeScript": 34960,
                            "JavaScript": 8278736,
                            "Objective-C": 76,
                            "Swift": 808,
                            "Python": 518,
                            "Dart": 242530
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "ofux",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "1774fd34-a8b6-43b0-b376-f2c2b256d478",
                                "name": "PSG",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168095065030147290.png"
                            }
                        ],
                        "technologies": {
                            "Rust": 23314
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "Bernardstanislas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                                "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                            },
                            {
                                "githubUserId": null,
                                "login": "mat-yas",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                                "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                            },
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "CSS": 1294028,
                            "SCSS": 409812,
                            "JavaScript": 234496,
                            "HTML": 679592
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 2,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "pacovilletard",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                                "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                            }
                        ],
                        "sponsors": [
                            {
                                "id": "c8dfb479-ee9d-4c16-b4b3-0ba39c2fdd6f",
                                "name": "OGC Nissa Ineos",
                                "url": null,
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
                            "TypeScript": 757100,
                            "Dockerfile": 7928,
                            "CSS": 1688864,
                            "Shell": 2928,
                            "Rust": 1628092,
                            "SCSS": 393440,
                            "JavaScript": 250868,
                            "HTML": 487624
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 17,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 2801450,
                            "Dockerfile": 325,
                            "CSS": 5535,
                            "Shell": 11474,
                            "PLpgSQL": 1372,
                            "JavaScript": 24207,
                            "HTML": 1520
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
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
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                            },
                            {
                                "id": "44c6807c-48d1-4987-a0a6-ac63f958bdae",
                                "name": "Coca Colax",
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg"
                            },
                            {
                                "id": "0980c5ab-befc-4314-acab-777fbf970cbb",
                                "name": "Coca Cola",
                                "url": null,
                                "logoUrl": "https://yt3.googleusercontent.com/NgMkZDr_RjcizNLNSQkAy1kmKC-qRkX-wsWTt97e1XFRstMapTAGBPO1XQJpW3J2KRv2eBkYucY=s900-c-k-c0x00ffffff-no-rj"
                            }
                        ],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
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
                                "url": null,
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2946389705306833508.png"
                            }
                        ],
                        "technologies": {
                            "CSS": 456,
                            "Makefile": 196,
                            "JavaScript": 62280,
                            "HTML": 940
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 8,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "AnthonyBuisset",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "COBOL": 10808,
                            "JavaScript": 6987
                        },
                        "isInvitedAsProjectLead": false
                    },
                    {
                        "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                        "slug": "zero-title-4",
                        "name": "Zero title 4",
                        "shortDescription": "Missing short description",
                        "logoUrl": null,
                        "hiring": false,
                        "visibility": "PUBLIC",
                        "repoCount": 4,
                        "contributorCount": 2085,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "oscarwroche",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                                "id": "dd0ab03c-5875-424b-96db-a35522eab365"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "C++": 44290,
                            "CSS": 64012,
                            "C": 5227,
                            "Scheme": 43698,
                            "Makefile": 189,
                            "HTML": 120786,
                            "TypeScript": 21471,
                            "Dockerfile": 325,
                            "Shell": 2205,
                            "CoffeeScript": 17480,
                            "JavaScript": 4115503,
                            "Haskell": 16365,
                            "Python": 259
                        },
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 280,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "ofux",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                                "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "Shell": 4429,
                            "Rust": 1985261,
                            "HTML": 871
                        },
                        "isInvitedAsProjectLead": false
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
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {},
                        "isInvitedAsProjectLead": false
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
                        "contributorCount": 17,
                        "leaders": [
                            {
                                "githubUserId": null,
                                "login": "gregcha",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                            },
                            {
                                "githubUserId": null,
                                "login": "PierreOucif",
                                "htmlUrl": null,
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                            }
                        ],
                        "sponsors": [],
                        "technologies": {
                            "TypeScript": 5602900,
                            "CSS": 11070,
                            "Shell": 22948,
                            "PLpgSQL": 2744,
                            "JavaScript": 47730,
                            "HTML": 3040
                        },
                        "isInvitedAsProjectLead": false
                    }
                ],
                "technologies": [
                    "Procfile",
                    "C",
                    "Scheme",
                    "CMake",
                    "Makefile",
                    "HTML",
                    "Jupyter Notebook",
                    "TypeScript",
                    "Shell",
                    "Solidity",
                    "SCSS",
                    "JavaScript",
                    "PHP",
                    "Objective-C",
                    "Haskell",
                    "Ruby",
                    "Python",
                    "MDX",
                    "CSS",
                    "C++",
                    "Jinja",
                    "Rust",
                    "PLpgSQL",
                    "COBOL",
                    "Kotlin",
                    "Dockerfile",
                    "CoffeeScript",
                    "Batchfile",
                    "Cairo",
                    "Swift",
                    "Nix",
                    "Dart"
                ],
                "sponsors": [
                    "Starknet Foundation",
                    "PSG",
                    "Coca Colax",
                    "Theodo",
                    "AS Nancy Lorraine",
                    "Coca Cola",
                    "Red Bull",
                    "OGC Nissa Ineos"
                ]
            }""";
    @Autowired
    ProjectRepository projectRepository;
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
                .uri(getApiURI(PROJECTS_GET))
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
                .uri(getApiURI(PROJECTS_GET, Map.of("sort", "CONTRIBUTOR_COUNT", "technologies", "Rust", "sponsor",
                        "Theodo", "search", "t")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_ANONYMOUS_USER_WITH_SORTS_AND_FILTERS_JSON_RESPONSE);
    }

    @Test
    @Order(6)
    void should_get_projects_given_authenticated_user() throws JsonProcessingException {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE);
    }

    @Test
    @Order(7)
    void should_get_projects_given_authenticated_user_for_mine() throws JsonProcessingException {
        // Given
        final var auth = userHelper.authenticatePierre();

        final ProjectEntity bretzel = projectRepository.findByKey("bretzel").orElseThrow();
        projectLeaderInvitationRepository.save(ProjectLeaderInvitationEntity.builder()
                .projectId(bretzel.getId())
                .githubUserId(auth.user().getGithubUserId())
                .id(UUID.randomUUID())
                .build());

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET, "mine", "true"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_JSON_RESPONSE);
    }
}
