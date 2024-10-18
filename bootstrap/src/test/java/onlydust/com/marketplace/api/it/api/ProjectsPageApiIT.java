package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.DatabaseHelper;
import onlydust.com.marketplace.api.postgres.adapter.PostgresProjectAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectProjectCategoryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ProjectTagEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectCategoryRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectTagRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.bi.BiProjectGlobalDataRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Project;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsPageApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_JSON_RESPONSE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 2,
              "hasMore": false,
              "nextPageIndex": 0,
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
                  "contributorCount": 6,
                  "remainingUsdBudget": null,
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
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos",
                      "hidden": null
                    },
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                      "bannerUrl": null,
                      "slug": "ethereum",
                      "hidden": null
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                      "bannerUrl": null,
                      "slug": "zama",
                      "hidden": null
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "slug": "typescript",
                      "name": "Typescript",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": 4040.00,
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "hasMissingGithubAppInstallation": false,
                  "tags": []
                }
              ],
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos",
                  "hidden": null
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail",
                  "hidden": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec",
                  "hidden": null
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum",
                  "hidden": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet",
                  "hidden": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama",
                  "hidden": null
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "description": "AI is cool",
                  "iconSlug": "brain"
                }
              ]
            }
            """;
    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_FOR_MINE_WITH_TAGS_JSON_RESPONSE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 2,
              "hasMore": false,
              "nextPageIndex": 0,
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
                  "contributorCount": 6,
                  "remainingUsdBudget": null,
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
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos",
                      "hidden": null
                    },
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                      "bannerUrl": null,
                      "slug": "ethereum",
                      "hidden": null
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                      "bannerUrl": null,
                      "slug": "zama",
                      "hidden": null
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "slug": "typescript",
                      "name": "Typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": true,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": 4040.00,
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
                      "slug": "javascript",
                      "name": "Javascript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "hasMissingGithubAppInstallation": false,
                  "tags": [
                    "FAST_AND_FURIOUS"
                  ]
                }
              ],
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos",
                  "hidden": null
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail",
                  "hidden": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec",
                  "hidden": null
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum",
                  "hidden": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet",
                  "hidden": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama",
                  "hidden": null
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "description": "AI is cool",
                  "iconSlug": "brain"
                }
              ]
            }
            """;

    private static final String GET_PROJECTS_FOR_AUTHENTICATED_USER_JSON_RESPONSE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 25,
              "hasMore": false,
              "nextPageIndex": 0,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
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
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 6,
                  "remainingUsdBudget": null,
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
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos",
                      "hidden": null
                    },
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                      "bannerUrl": null,
                      "slug": "ethereum",
                      "hidden": null
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                      "bannerUrl": null,
                      "slug": "zama",
                      "hidden": null
                    }
                  ],
                  "languages": [
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
                  "remainingUsdBudget": null,
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
                  "contributorCount": 46,
                  "remainingUsdBudget": null,
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
                      "slug": "starknet",
                      "hidden": null
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
                  "contributorCount": 27,
                  "remainingUsdBudget": null,
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
                      "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                      "slug": "ruby",
                      "name": "Ruby",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
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
                  "repoCount": 5,
                  "contributorCount": 7,
                  "remainingUsdBudget": null,
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
                      "slug": "aztec",
                      "hidden": null
                    },
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet",
                      "hidden": null
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
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "slug": "python",
                      "name": "Python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "avail",
                      "hidden": null
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
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "slug": "python",
                      "name": "Python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": 9191.00,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [],
                  "languages": [
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
                  "contributorCount": 87,
                  "remainingUsdBudget": null,
                  "leaders": [
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
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
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
                  "contributorCount": 21,
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
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
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "contributorCount": 22,
                  "remainingUsdBudget": null,
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
                  "contributorCount": 14,
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet",
                      "hidden": null
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
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "slug": "solidity",
                      "name": "Solidity",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "repoCount": 4,
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 42,
                  "remainingUsdBudget": null,
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
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [],
                  "languages": [],
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
                  "contributorCount": 17,
                  "remainingUsdBudget": 87870.00,
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "hasMissingGithubAppInstallation": false,
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
                  "contributorCount": 3,
                  "remainingUsdBudget": null,
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
                }
              ],
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos",
                  "hidden": null
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail",
                  "hidden": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec",
                  "hidden": null
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum",
                  "hidden": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet",
                  "hidden": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama",
                  "hidden": null
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "description": "AI is cool",
                  "iconSlug": "brain"
                }
              ]
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
              "totalPageNumber": 1,
              "totalItemNumber": 25,
              "hasMore": false,
              "nextPageIndex": 0,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
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
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 6,
                  "remainingUsdBudget": null,
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
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos",
                      "hidden": null
                    },
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                      "bannerUrl": null,
                      "slug": "ethereum",
                      "hidden": null
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                      "bannerUrl": null,
                      "slug": "zama",
                      "hidden": null
                    }
                  ],
                  "languages": [
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
                  "remainingUsdBudget": null,
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
                  "contributorCount": 46,
                  "remainingUsdBudget": null,
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
                      "slug": "starknet",
                      "hidden": null
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
                  "contributorCount": 27,
                  "remainingUsdBudget": null,
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
                      "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                      "slug": "ruby",
                      "name": "Ruby",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
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
                  "repoCount": 5,
                  "contributorCount": 7,
                  "remainingUsdBudget": null,
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
                      "slug": "aztec",
                      "hidden": null
                    },
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet",
                      "hidden": null
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
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "slug": "python",
                      "name": "Python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "avail",
                      "hidden": null
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
                      "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                      "slug": "python",
                      "name": "Python",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                    },
                    {
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [],
                  "languages": [
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
                  "contributorCount": 87,
                  "remainingUsdBudget": null,
                  "leaders": [
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
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 4435377,
                      "login": "Bernardstanislas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                      "id": "6115f024-159a-4b1f-b713-1e2ad5c6063e"
                    },
                    {
                      "githubUserId": 8642470,
                      "login": "gregcha",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/15168934086343666513.webp",
                      "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
                    },
                    {
                      "githubUserId": 21149076,
                      "login": "oscarwroche",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                      "id": "dd0ab03c-5875-424b-96db-a35522eab365"
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
                  "contributorCount": 21,
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 141839618,
                      "login": "Blumebee",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/16582211468658783329.webp",
                      "id": "46fec596-7a91-422e-8532-5f479e790217"
                    },
                    {
                      "githubUserId": 122993337,
                      "login": "GregGamb",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11849964898247380166.webp",
                      "id": "743e096e-c922-4097-9e6f-8ea503055336"
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
                      "githubUserId": 31901905,
                      "login": "kaelsky",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                      "id": "a0da3c1e-6493-4ea1-8bd0-8c46d653f274"
                    },
                    {
                      "githubUserId": 139852598,
                      "login": "mat-yas",
                      "avatarUrl": "https://avatars.githubusercontent.com/u/139852598?v=4",
                      "id": "bdc705b5-cf8e-488f-926a-258e1800ed79"
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "contributorCount": 22,
                  "remainingUsdBudget": null,
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
                  "contributorCount": 14,
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet",
                      "hidden": null
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
                      "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                      "slug": "solidity",
                      "name": "Solidity",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "repoCount": 4,
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 42,
                  "remainingUsdBudget": null,
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
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "remainingUsdBudget": null,
                  "leaders": [],
                  "ecosystems": [],
                  "languages": [],
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
                  "contributorCount": 17,
                  "remainingUsdBudget": null,
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                  "contributorCount": 3,
                  "remainingUsdBudget": null,
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
                }
              ],
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos",
                  "hidden": null
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail",
                  "hidden": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec",
                  "hidden": null
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum",
                  "hidden": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet",
                  "hidden": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama",
                  "hidden": null
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "description": "AI is cool",
                  "iconSlug": "brain"
                }
              ]
            }
            """;

    @Autowired
    ProjectViewRepository projectViewRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;
    @Autowired
    ProjectCategoryRepository projectCategoryRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    BiProjectGlobalDataRepository biProjectGlobalDataRepository;
    @Autowired
    DatabaseHelper databaseHelper;

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
        projectRepository.saveAndFlush(project);
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refresh(ProjectId.of(project.getId())));
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

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "RANK", "pageIndex", "0", "pageSize", "10")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "NAME", "pageIndex", "0", "pageSize", "10")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        client.get().uri(getApiURI(PROJECTS_GET, Map.of("sort", "REPO_COUNT", "pageIndex", "0", "pageSize", "10")))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    @Order(6)
    void should_get_projects_given_authenticated_user() {
        // Given
        final String jwt = userAuthHelper.authenticateAntho().jwt();

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
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refresh(ProjectId.of(bretzel.getId())));

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
                          "totalPageNumber": 5,
                          "totalItemNumber": 25,
                          "hasMore": true,
                          "nextPageIndex": 1,
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
                              "contributorCount": 46,
                              "remainingUsdBudget": null,
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
                                  "slug": "starknet",
                                  "hidden": null
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
                              "contributorCount": 0,
                              "remainingUsdBudget": null,
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
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                              "contributorCount": 3,
                              "remainingUsdBudget": null,
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
                              "contributorCount": 0,
                              "remainingUsdBudget": null,
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
                                  "slug": "javascript",
                                  "name": "Javascript",
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
                              "remainingUsdBudget": null,
                              "leaders": [
                                {
                                  "githubUserId": 26790304,
                                  "login": "gaetanrecly",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                                  "id": "f2215429-83c7-49ce-954b-66ed453c3315"
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
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "bannerUrl": null,
                              "slug": "aptos",
                              "hidden": null
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail",
                              "hidden": null
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec",
                              "hidden": null
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "bannerUrl": null,
                              "slug": "ethereum",
                              "hidden": null
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet",
                              "hidden": null
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                              "bannerUrl": null,
                              "slug": "zama",
                              "hidden": null
                            }
                          ],
                          "categories": [
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "slug": "ai",
                              "name": "AI",
                              "description": "AI is cool",
                              "iconSlug": "brain"
                            }
                          ]
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

        databaseHelper.executeInTransaction(() -> {
            biProjectGlobalDataRepository.refresh(ProjectId.of("7d04163c-4187-4313-8066-61504d34fc56"));
            biProjectGlobalDataRepository.refresh(ProjectId.of("f39b827f-df73-498c-8853-99bc3f562723"));
        });


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
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
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
                              "contributorCount": 6,
                              "remainingUsdBudget": null,
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
                                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                  "name": "Aptos",
                                  "url": "https://aptosfoundation.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                  "bannerUrl": null,
                                  "slug": "aptos",
                                  "hidden": null
                                },
                                {
                                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                  "name": "Ethereum",
                                  "url": "https://ethereum.foundation/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                  "bannerUrl": null,
                                  "slug": "ethereum",
                                  "hidden": null
                                },
                                {
                                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                  "name": "Zama",
                                  "url": "https://www.zama.ai/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                  "bannerUrl": null,
                                  "slug": "zama",
                                  "hidden": null
                                }
                              ],
                              "languages": [
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
                              "contributorCount": 0,
                              "remainingUsdBudget": null,
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
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                                "FAST_AND_FURIOUS"
                              ]
                            }
                          ],
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
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "bannerUrl": null,
                              "slug": "aptos",
                              "hidden": null
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail",
                              "hidden": null
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec",
                              "hidden": null
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "bannerUrl": null,
                              "slug": "ethereum",
                              "hidden": null
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet",
                              "hidden": null
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                              "bannerUrl": null,
                              "slug": "zama",
                              "hidden": null
                            }
                          ],
                          "categories": [
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "slug": "ai",
                              "name": "AI",
                              "description": "AI is cool",
                              "iconSlug": "brain"
                            }
                          ]
                        }
                        """);


        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "100", "tags", "FAST_AND_FURIOUS,NEWBIES_WELCOME"))).exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
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
                              "contributorCount": 6,
                              "remainingUsdBudget": null,
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
                                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                                  "name": "Aptos",
                                  "url": "https://aptosfoundation.org/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                                  "bannerUrl": null,
                                  "slug": "aptos",
                                  "hidden": null
                                },
                                {
                                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                                  "name": "Ethereum",
                                  "url": "https://ethereum.foundation/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                                  "bannerUrl": null,
                                  "slug": "ethereum",
                                  "hidden": null
                                },
                                {
                                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                                  "name": "Zama",
                                  "url": "https://www.zama.ai/",
                                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                                  "bannerUrl": null,
                                  "slug": "zama",
                                  "hidden": null
                                }
                              ],
                              "languages": [
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
                              "contributorCount": 0,
                              "remainingUsdBudget": null,
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
                                  "slug": "javascript",
                                  "name": "Javascript",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                                },
                                {
                                  "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                                  "slug": "rust",
                                  "name": "Rust",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                                "FAST_AND_FURIOUS"
                              ]
                            }
                          ],
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
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "bannerUrl": null,
                              "slug": "aptos",
                              "hidden": null
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail",
                              "hidden": null
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec",
                              "hidden": null
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "bannerUrl": null,
                              "slug": "ethereum",
                              "hidden": null
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet",
                              "hidden": null
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                              "bannerUrl": null,
                              "slug": "zama",
                              "hidden": null
                            }
                          ],
                          "categories": [
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "slug": "ai",
                              "name": "AI",
                              "description": "AI is cool",
                              "iconSlug": "brain"
                            }
                          ]
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

    @Autowired
    PostgresProjectAdapter projectStoragePort;

    @Test
    @Order(14)
    void should_update_projects_tags() throws ParseException {
        // Given
        projectStoragePort.updateProjectsTags(new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2000"));
        final var auth = userAuthHelper.authenticatePierre();
        databaseHelper.executeInTransaction(() -> biProjectGlobalDataRepository.refresh());

        client.get().uri(getApiURI(PROJECTS_GET, Map.of("pageIndex", "0", "pageSize", "10")))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_AFTER_TAGS_UPDATE_JSON_RESPONSE);
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
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "projects": [],
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
                          "ecosystems": [
                            {
                              "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                              "name": "Aptos",
                              "url": "https://aptosfoundation.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                              "bannerUrl": null,
                              "slug": "aptos",
                              "hidden": null
                            },
                            {
                              "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                              "name": "Avail",
                              "url": "https://www.availproject.org/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                              "bannerUrl": null,
                              "slug": "avail",
                              "hidden": null
                            },
                            {
                              "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                              "name": "Aztec",
                              "url": "https://aztec.network/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                              "bannerUrl": null,
                              "slug": "aztec",
                              "hidden": null
                            },
                            {
                              "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                              "name": "Ethereum",
                              "url": "https://ethereum.foundation/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                              "bannerUrl": null,
                              "slug": "ethereum",
                              "hidden": null
                            },
                            {
                              "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                              "name": "Starknet",
                              "url": "https://www.starknet.io/en",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                              "bannerUrl": null,
                              "slug": "starknet",
                              "hidden": null
                            },
                            {
                              "id": "b599313c-a074-440f-af04-a466529ab2e7",
                              "name": "Zama",
                              "url": "https://www.zama.ai/",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                              "bannerUrl": null,
                              "slug": "zama",
                              "hidden": null
                            }
                          ],
                          "categories": [
                            {
                              "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                              "slug": "ai",
                              "name": "AI",
                              "description": "AI is cool",
                              "iconSlug": "brain"
                            }
                          ]
                        }
                        """);
    }

    private static final String GET_PROJECTS_AFTER_TAGS_UPDATE_JSON_RESPONSE = """
            {
              "totalPageNumber": 3,
              "totalItemNumber": 25,
              "hasMore": true,
              "nextPageIndex": 1,
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "remainingUsdBudget": null,
                  "leaders": [
                    {
                      "githubUserId": 26790304,
                      "login": "gaetanrecly",
                      "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2890661490599004977.webp",
                      "id": "f2215429-83c7-49ce-954b-66ed453c3315"
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
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                  "contributorCount": 6,
                  "remainingUsdBudget": null,
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
                      "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                      "name": "Aptos",
                      "url": "https://aptosfoundation.org/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                      "bannerUrl": null,
                      "slug": "aptos",
                      "hidden": null
                    },
                    {
                      "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                      "name": "Ethereum",
                      "url": "https://ethereum.foundation/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                      "bannerUrl": null,
                      "slug": "ethereum",
                      "hidden": null
                    },
                    {
                      "id": "b599313c-a074-440f-af04-a466529ab2e7",
                      "name": "Zama",
                      "url": "https://www.zama.ai/",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                      "bannerUrl": null,
                      "slug": "zama",
                      "hidden": null
                    }
                  ],
                  "languages": [
                    {
                      "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                      "slug": "typescript",
                      "name": "Typescript",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                    }
                  ],
                  "isInvitedAsProjectLead": true,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "BIG_WHALE",
                    "NEWBIES_WELCOME"
                  ]
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
                  "remainingUsdBudget": null,
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
                  "contributorCount": 46,
                  "remainingUsdBudget": null,
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
                      "slug": "starknet",
                      "hidden": null
                    }
                  ],
                  "languages": [
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
                    "FAST_AND_FURIOUS",
                    "HAS_GOOD_FIRST_ISSUES",
                    "HOT_COMMUNITY",
                    "NEWBIES_WELCOME",
                    "WORK_IN_PROGRESS"
                  ]
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
                  "contributorCount": 27,
                  "remainingUsdBudget": null,
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
                      "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                      "slug": "ruby",
                      "name": "Ruby",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                    }
                  ],
                  "isInvitedAsProjectLead": false,
                  "hasMissingGithubAppInstallation": null,
                  "tags": [
                    "FAST_AND_FURIOUS",
                    "HOT_COMMUNITY",
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
                  "repoCount": 5,
                  "contributorCount": 7,
                  "remainingUsdBudget": null,
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
                      "slug": "aztec",
                      "hidden": null
                    },
                    {
                      "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                      "name": "Starknet",
                      "url": "https://www.starknet.io/en",
                      "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                      "bannerUrl": null,
                      "slug": "starknet",
                      "hidden": null
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                    "FAST_AND_FURIOUS",
                    "HOT_COMMUNITY",
                    "LIKELY_TO_REWARD",
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
                  "contributorCount": 0,
                  "remainingUsdBudget": null,
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
                      "slug": "avail",
                      "hidden": null
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
                      "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                      "slug": "rust",
                      "name": "Rust",
                      "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                      "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
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
                    "BIG_WHALE",
                    "FAST_AND_FURIOUS",
                    "HOT_COMMUNITY",
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
                  "remainingUsdBudget": null,
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
                      "slug": "javascript",
                      "name": "Javascript",
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
                }
              ],
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
              "ecosystems": [
                {
                  "id": "9f82bdb4-22c2-455a-91a8-e3c7d96c47d7",
                  "name": "Aptos",
                  "url": "https://aptosfoundation.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8106946702216548210.png",
                  "bannerUrl": null,
                  "slug": "aptos",
                  "hidden": null
                },
                {
                  "id": "397df411-045d-4d9f-8d65-8284c88f9208",
                  "name": "Avail",
                  "url": "https://www.availproject.org/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12011103528231014365.png",
                  "bannerUrl": null,
                  "slug": "avail",
                  "hidden": null
                },
                {
                  "id": "ed314d31-f5f2-40e5-9cfc-a962b35c572e",
                  "name": "Aztec",
                  "url": "https://aztec.network/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/2431172990485257518.jpg",
                  "bannerUrl": null,
                  "slug": "aztec",
                  "hidden": null
                },
                {
                  "id": "6ab7fa6c-c418-4997-9c5f-55fb021a8e5c",
                  "name": "Ethereum",
                  "url": "https://ethereum.foundation/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/8506434858363286425.png",
                  "bannerUrl": null,
                  "slug": "ethereum",
                  "hidden": null
                },
                {
                  "id": "99b6c284-f9bb-4f89-8ce7-03771465ef8e",
                  "name": "Starknet",
                  "url": "https://www.starknet.io/en",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/12429671188779981103.png",
                  "bannerUrl": null,
                  "slug": "starknet",
                  "hidden": null
                },
                {
                  "id": "b599313c-a074-440f-af04-a466529ab2e7",
                  "name": "Zama",
                  "url": "https://www.zama.ai/",
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/599423013682223091.png",
                  "bannerUrl": null,
                  "slug": "zama",
                  "hidden": null
                }
              ],
              "categories": [
                {
                  "id": "b151c7e4-1493-4927-bb0f-8647ec98a9c5",
                  "slug": "ai",
                  "name": "AI",
                  "description": "AI is cool",
                  "iconSlug": "brain"
                }
              ]
            }
            """;
}
