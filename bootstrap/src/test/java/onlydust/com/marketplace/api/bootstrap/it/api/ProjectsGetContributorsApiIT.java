package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetContributorsApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_0 = """
            {
              "totalPageNumber": 5,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 1,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 2692632.50,
                    "details": [
                      {
                        "totalAmount": 3250,
                        "totalDollarsEquivalent": 3250,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 19750,
                        "totalDollarsEquivalent": 2689382.50,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                  "isRegistered": true,
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 20855.02,
                    "details": [
                      {
                        "totalAmount": 4188,
                        "totalDollarsEquivalent": 4188,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 16502,
                        "totalDollarsEquivalent": 16667.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "isRegistered": true,
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 11552.38,
                    "details": [
                      {
                        "totalAmount": 11438,
                        "totalDollarsEquivalent": 11552.38,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 24242.02,
                    "details": [
                      {
                        "totalAmount": 24002,
                        "totalDollarsEquivalent": 24242.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                }
              ]
            }
            """;
    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_1 = """
            {
              "totalPageNumber": 5,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 2,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 6060.00,
                    "details": [
                      {
                        "totalAmount": 6000,
                        "totalDollarsEquivalent": 6060.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "isRegistered": false,
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 1010.00,
                    "details": [
                      {
                        "totalAmount": 1000,
                        "totalDollarsEquivalent": 1010.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 31901905,
                  "login": "kaelsky",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "isRegistered": true,
                  "contributionCount": 127,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                }
              ]
            }
            """;
    private static final String GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 18,
              "hasMore": false,
              "nextPageIndex": 0,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 102823832,
                  "login": "SamuelKer",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 1,
                  "pullRequestToReward": 1,
                  "issueToReward": 0,
                  "codeReviewToReward": 0,
                  "hidden": false
                },
                {
                  "githubUserId": 129528947,
                  "login": "VeryDustyBot",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 1,
                  "pullRequestToReward": 0,
                  "issueToReward": 0,
                  "codeReviewToReward": 1,
                  "hidden": false
                },
                {
                  "githubUserId": 698957,
                  "login": "ltoussaint",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                  "isRegistered": false,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 2,
                  "pullRequestToReward": 0,
                  "issueToReward": 0,
                  "codeReviewToReward": 2,
                  "hidden": false
                },
                {
                  "githubUserId": 112474158,
                  "login": "onlydust-contributor",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                  "isRegistered": true,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 2,
                  "pullRequestToReward": 1,
                  "issueToReward": 0,
                  "codeReviewToReward": 1,
                  "hidden": false
                },
                {
                  "githubUserId": 98529704,
                  "login": "tekkac",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                  "isRegistered": false,
                  "contributionCount": 20,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 20,
                  "pullRequestToReward": 7,
                  "issueToReward": 1,
                  "codeReviewToReward": 12,
                  "hidden": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "abdelhamidbakhta",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "isRegistered": false,
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 3977.38,
                    "details": [
                      {
                        "totalAmount": 3938,
                        "totalDollarsEquivalent": 3977.38,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 21,
                  "pullRequestToReward": 8,
                  "issueToReward": 0,
                  "codeReviewToReward": 13,
                  "hidden": false
                },
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 505.00,
                    "details": [
                      {
                        "totalAmount": 500,
                        "totalDollarsEquivalent": 505.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 36,
                  "pullRequestToReward": 25,
                  "issueToReward": 0,
                  "codeReviewToReward": 11,
                  "hidden": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 46,
                  "pullRequestToReward": 34,
                  "issueToReward": 0,
                  "codeReviewToReward": 12,
                  "hidden": false
                },
                {
                  "githubUserId": 143011364,
                  "login": "pixelfact",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                  "isRegistered": false,
                  "contributionCount": 102,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 102,
                  "pullRequestToReward": 55,
                  "issueToReward": 0,
                  "codeReviewToReward": 47,
                  "hidden": false
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 104,
                  "pullRequestToReward": 78,
                  "issueToReward": 0,
                  "codeReviewToReward": 26,
                  "hidden": false
                },
                {
                  "githubUserId": 31901905,
                  "login": "kaelsky",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "isRegistered": true,
                  "contributionCount": 127,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 127,
                  "pullRequestToReward": 88,
                  "issueToReward": 0,
                  "codeReviewToReward": 39,
                  "hidden": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 140,
                  "pullRequestToReward": 57,
                  "issueToReward": 1,
                  "codeReviewToReward": 82,
                  "hidden": false
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "isRegistered": false,
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 1010.00,
                    "details": [
                      {
                        "totalAmount": 1000,
                        "totalDollarsEquivalent": 1010.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 146,
                  "pullRequestToReward": 107,
                  "issueToReward": 0,
                  "codeReviewToReward": 39,
                  "hidden": false
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 6060.00,
                    "details": [
                      {
                        "totalAmount": 6000,
                        "totalDollarsEquivalent": 6060.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 122,
                  "pullRequestToReward": 5,
                  "issueToReward": 0,
                  "codeReviewToReward": 117,
                  "hidden": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 24242.02,
                    "details": [
                      {
                        "totalAmount": 24002,
                        "totalDollarsEquivalent": 24242.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 153,
                  "pullRequestToReward": 57,
                  "issueToReward": 0,
                  "codeReviewToReward": 96,
                  "hidden": false
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "isRegistered": true,
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 11552.38,
                    "details": [
                      {
                        "totalAmount": 11438,
                        "totalDollarsEquivalent": 11552.38,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 374,
                  "pullRequestToReward": 115,
                  "issueToReward": 0,
                  "codeReviewToReward": 259,
                  "hidden": false
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                  "isRegistered": true,
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 20855.02,
                    "details": [
                      {
                        "totalAmount": 4188,
                        "totalDollarsEquivalent": 4188,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 16502,
                        "totalDollarsEquivalent": 16667.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 569,
                  "pullRequestToReward": 247,
                  "issueToReward": 2,
                  "codeReviewToReward": 320,
                  "hidden": false
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 2692632.50,
                    "details": [
                      {
                        "totalAmount": 3250,
                        "totalDollarsEquivalent": 3250,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 19750,
                        "totalDollarsEquivalent": 2689382.50,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "hidden": false
                }
              ]
            }
            """;

    private static final String GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD_LOGIN_LE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 4,
              "hasMore": false,
              "nextPageIndex": 0,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 505.00,
                    "details": [
                      {
                        "totalAmount": 500,
                        "totalDollarsEquivalent": 505.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 36,
                  "pullRequestToReward": 25,
                  "issueToReward": 0,
                  "codeReviewToReward": 11,
                  "hidden": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 46,
                  "pullRequestToReward": 34,
                  "issueToReward": 0,
                  "codeReviewToReward": 12,
                  "hidden": false
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 104,
                  "pullRequestToReward": 78,
                  "issueToReward": 0,
                  "codeReviewToReward": 26,
                  "hidden": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 140,
                  "pullRequestToReward": 57,
                  "issueToReward": 1,
                  "codeReviewToReward": 82,
                  "hidden": false
                }
              ]
            }
            """;
    private static final String GET_PROJECT_CONTRIBUTORS_ANONYMOUS = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 18,
              "hasMore": false,
              "nextPageIndex": 0,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 102823832,
                  "login": "SamuelKer",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 129528947,
                  "login": "VeryDustyBot",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 698957,
                  "login": "ltoussaint",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                  "isRegistered": false,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 112474158,
                  "login": "onlydust-contributor",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                  "isRegistered": true,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 98529704,
                  "login": "tekkac",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                  "isRegistered": false,
                  "contributionCount": 20,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "abdelhamidbakhta",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "isRegistered": false,
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 3977.38,
                    "details": [
                      {
                        "totalAmount": 3938,
                        "totalDollarsEquivalent": 3977.38,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 505.00,
                    "details": [
                      {
                        "totalAmount": 500,
                        "totalDollarsEquivalent": 505.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 143011364,
                  "login": "pixelfact",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                  "isRegistered": false,
                  "contributionCount": 102,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 31901905,
                  "login": "kaelsky",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "isRegistered": true,
                  "contributionCount": 127,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "isRegistered": false,
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 1010.00,
                    "details": [
                      {
                        "totalAmount": 1000,
                        "totalDollarsEquivalent": 1010.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 6060.00,
                    "details": [
                      {
                        "totalAmount": 6000,
                        "totalDollarsEquivalent": 6060.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 24242.02,
                    "details": [
                      {
                        "totalAmount": 24002,
                        "totalDollarsEquivalent": 24242.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "isRegistered": true,
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 11552.38,
                    "details": [
                      {
                        "totalAmount": 11438,
                        "totalDollarsEquivalent": 11552.38,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                  "isRegistered": true,
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 20855.02,
                    "details": [
                      {
                        "totalAmount": 4188,
                        "totalDollarsEquivalent": 4188,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 16502,
                        "totalDollarsEquivalent": 16667.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 2692632.50,
                    "details": [
                      {
                        "totalAmount": 3250,
                        "totalDollarsEquivalent": 3250,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 19750,
                        "totalDollarsEquivalent": 2689382.50,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                }
              ]
            }
            """;

    private static final String GET_PROJECT_CONTRIBUTORS_ANONYMOUS_LOGIN_LE = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 4,
              "hasMore": false,
              "nextPageIndex": 0,
              "contributors": [
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                      "totalAmount": 505.00,
                      "details": [
                        {
                          "totalAmount": 500,
                          "totalDollarsEquivalent": 505.00,
                          "currency": {
                            "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                            "code": "USDC",
                            "name": "USD Coin",
                            "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                            "decimals": 6
                          }
                        }
                      ]
                    },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null
                }
              ]
            }
            """;
    private static final String GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES = """
            {
              "totalPageNumber": 9,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 1,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 2692632.50,
                    "details": [
                      {
                        "totalAmount": 3250,
                        "totalDollarsEquivalent": 3250,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 19750,
                        "totalDollarsEquivalent": 2689382.50,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "hidden": false
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 35210.00,
                    "details": [
                      {
                        "totalAmount": 20.5,
                        "totalDollarsEquivalent": 31426.5,
                        "currency": {
                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                          "code": "ETH",
                          "name": "Ether",
                          "logoUrl": null,
                          "decimals": 18
                        }
                      },
                      {
                        "totalAmount": 450,
                        "totalDollarsEquivalent": 643.5,
                        "currency": {
                          "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                          "code": "OP",
                          "name": "Optimism",
                          "logoUrl": null,
                          "decimals": 18
                        }
                      },
                      {
                        "totalAmount": 2000,
                        "totalDollarsEquivalent": 1120,
                        "currency": {
                          "id": "48388edb-fda2-4a32-b228-28152a147500",
                          "code": "APT",
                          "name": "Aptos Coin",
                          "logoUrl": null,
                          "decimals": 8
                        }
                      },
                      {
                        "totalAmount": 2000,
                        "totalDollarsEquivalent": 2020.00,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      },
                      {
                        "totalAmount": 500000,
                        "totalDollarsEquivalent": 0,
                        "currency": {
                          "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                          "code": "STRK",
                          "name": "StarkNet Token",
                          "logoUrl": null,
                          "decimals": 18
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 120,
                  "pullRequestToReward": 4,
                  "issueToReward": 0,
                  "codeReviewToReward": 116,
                  "hidden": false
                }
              ]
            }
            """;

    private static final String GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES_AND_IGNORED_CONTRIBUTIONS = """
            {
              "totalPageNumber": 9,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 1,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "htmlUrl": null,
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 2692632.50,
                    "details": [
                      {
                        "totalAmount": 3250,
                        "totalDollarsEquivalent": 3250,
                        "currency": {
                          "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                          "code": "USD",
                          "name": "US Dollar",
                          "logoUrl": null,
                          "decimals": 2
                        }
                      },
                      {
                        "totalAmount": 19750,
                        "totalDollarsEquivalent": 2689382.50,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "hidden": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "htmlUrl": null,
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 24242.02,
                    "details": [
                      {
                        "totalAmount": 24002,
                        "totalDollarsEquivalent": 24242.02,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        }
                      }
                    ]
                  },
                  "contributionToRewardCount": 153,
                  "pullRequestToReward": 57,
                  "issueToReward": 0,
                  "codeReviewToReward": 96,
                  "hidden": false
                }
              ]
            }
            """;


    @Autowired
    IgnoredContributionsRepository ignoredContributionsRepository;

    @Test
    @Order(1)
    void should_find_project_contributors_as_anonymous_user() {
        // Given
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_ANONYMOUS);
    }

    @Test
    @Order(1)
    void should_find_project_contributors_as_anonymous_user_with_login_filter() {
        // Given
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("login", "le",
                                "pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_ANONYMOUS_LOGIN_LE);
    }

    @Test
    @Order(2)
    void should_find_project_with_pagination() {
        // Given
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "4", "sort", "CONTRIBUTION_COUNT", "direction", "DESC")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_0);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "1", "pageSize", "4", "sort", "CONTRIBUTION_COUNT", "direction", "DESC")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_1);
    }

    @Test
    @Order(4)
    void should_find_project_contributors_as_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD);
    }


    @Test
    @Order(5)
    void should_hide_and_show_project_contributors_as_project_lead() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var veryDustyBot = 129528947L;

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_HIDE_CONTRIBUTOR, projectId, veryDustyBot)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus().isNoContent();

        // Then
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasHiddenContributors").isEqualTo(true)
                .jsonPath("$.contributors[?(@.githubUserId == 129528947)]").doesNotExist()
        ;

        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT", "showHidden", "true")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasHiddenContributors").isEqualTo(true)
                .jsonPath("$.contributors[?(@.githubUserId == 129528947 && @.hidden == true)]").exists()
                .jsonPath("$.contributors[?(@.githubUserId == 129528947 && @.hidden == false)]").doesNotExist()
                .jsonPath("$.contributors[?(@.githubUserId != 129528947 && @.hidden == true)]").doesNotExist()
                .jsonPath("$.contributors[?(@.githubUserId != 129528947 && @.hidden == false)]").exists()
        ;


        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_HIDE_CONTRIBUTOR, projectId, veryDustyBot)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus().isNoContent();

        // Then
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.hasHiddenContributors").isEqualTo(false)
                .jsonPath("$.contributors[?(@.githubUserId == 129528947)].hidden").isEqualTo(false)
        ;
    }

    @Test
    @Order(4)
    void should_find_project_contributors_as_project_lead_with_login_filter() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("login", "le",
                                "pageIndex", "0", "pageSize", "10000", "sort", "CONTRIBUTION_COUNT")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PROJECT_LEAD_LOGIN_LE);
    }

    @Test
    @Order(5)
    void should_find_project_contributors_with_multi_currencies() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final var reward1 = rewardRepository.findById(UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")).orElseThrow();
        reward1.currency(currencyRepository.findByCode("ETH").orElseThrow());
        reward1.amount(BigDecimal.valueOf(20.5));
        rewardRepository.save(reward1);
        rewardStatusRepository.save(rewardStatusRepository.findById(reward1.id()).orElseThrow().amountUsdEquivalent(BigDecimal.valueOf(31426.5)));

        final var reward2 = rewardRepository.findById(UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a")).orElseThrow();
        reward2.currency(currencyRepository.findByCode("APT").orElseThrow());
        reward2.amount(BigDecimal.valueOf(2000));
        rewardRepository.save(reward2);
        rewardStatusRepository.save(rewardStatusRepository.findById(reward2.id()).orElseThrow().amountUsdEquivalent(BigDecimal.valueOf(1120)));

        final var reward3 = rewardRepository.findById(UUID.fromString("40fda3c6-2a3f-4cdd-ba12-0499dd232d53")).orElseThrow();
        reward3.currency(currencyRepository.findByCode("OP").orElseThrow());
        reward3.amount(BigDecimal.valueOf(450));
        rewardRepository.save(reward3);
        rewardStatusRepository.save(rewardStatusRepository.findById(reward3.id()).orElseThrow().amountUsdEquivalent(BigDecimal.valueOf(643.5)));

        final var reward4 = rewardRepository.findById(UUID.fromString("5b96ca1e-4ad2-41c1-8819-520b885d9223")).orElseThrow();
        reward4.currency(currencyRepository.findByCode("STRK").orElseThrow());
        reward4.amount(BigDecimal.valueOf(500000));
        rewardRepository.save(reward4);
        rewardStatusRepository.save(rewardStatusRepository.findById(reward4.id()).orElseThrow().amountUsdEquivalent(null));

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "2", "sort", "EARNED", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES);
    }


    @Test
    @Order(5)
    void should_find_project_contributors_with_ignored_contributions() {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        ignoredContributionsRepository.save(IgnoredContributionEntity.builder()
                .id(IgnoredContributionEntity.Id.builder()
                        .projectId(projectId)
                        .contributionId("1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13")
                        .build())
                .build());
        ignoredContributionsRepository.save(IgnoredContributionEntity.builder()
                .id(IgnoredContributionEntity.Id.builder()
                        .projectId(projectId)
                        .contributionId("2884dc233c8512d062d7dd0b60d78d58e416349bf0a3e1feddff1183a01895e8")
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "2", "sort", "EARNED", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES_AND_IGNORED_CONTRIBUTIONS);
    }
}
