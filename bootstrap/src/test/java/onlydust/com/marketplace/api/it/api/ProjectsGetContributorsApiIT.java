package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.api.contract.model.ContributorsPageResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.RewardId;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetContributorsApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_0 = """
            {
              "totalPageNumber": 5,
              "totalItemNumber": 20,
              "hasMore": true,
              "nextPageIndex": 1,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 888,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                  "isRegistered": true,
                  "contributionCount": 574,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "isRegistered": true,
                  "contributionCount": 380,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 1,
                  "earned": {
                    "totalAmount": 1010.00,
                    "details": [
                      {
                        "amount": 1000,
                        "prettyAmount": 1000,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        },
                        "usdEquivalent": 1010.00
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
              "totalItemNumber": 20,
              "hasMore": true,
              "nextPageIndex": 2,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 148,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "isRegistered": false,
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "isRegistered": true,
                  "contributionCount": 127,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
               "totalItemNumber": 20,
               "hasMore": false,
               "nextPageIndex": 0,
               "hasHiddenContributors": false,
               "contributors": [
                 {
                   "githubUserId": 45336124,
                   "login": "MaximeBeasse",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/45336124?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 1,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 1,
                   "pullRequestToReward": 0,
                   "issueToReward": 1,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 102823832,
                   "login": "SamuelKer",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 1,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 129528947,
                   "login": "VeryDustyBot",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 1,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 8495664,
                   "login": "doomed-theory",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/8495664?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 1,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 1,
                   "pullRequestToReward": 1,
                   "issueToReward": 0,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 698957,
                   "login": "ltoussaint",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 2,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 112474158,
                   "login": "onlydust-contributor",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 2,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 98529704,
                   "login": "tekkac",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 20,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 1,
                   "pullRequestToReward": 0,
                   "issueToReward": 1,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 45264458,
                   "login": "AbdelStark",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 21,
                   "rewardCount": 1,
                   "earned": {
                     "totalAmount": 1515.00,
                     "details": [
                       {
                         "amount": 1500,
                         "prettyAmount": 1500,
                         "currency": {
                           "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                           "code": "USDC",
                           "name": "USD Coin",
                           "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                           "decimals": 6
                         },
                         "usdEquivalent": 1515.00,
                         "usdConversionRate": null
                       }
                     ]
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 10167015,
                   "login": "lechinoix",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 36,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 0,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null,
                   "hidden": false
                 },
                 {
                   "githubUserId": 10922658,
                   "login": "alexbensimon",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 46,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 3,
                   "pullRequestToReward": 3,
                   "issueToReward": 0,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 143011364,
                   "login": "pixelfact",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 102,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 13,
                   "pullRequestToReward": 10,
                   "issueToReward": 0,
                   "codeReviewToReward": 3,
                   "hidden": false
                 },
                 {
                   "githubUserId": 17259618,
                   "login": "alexbeno",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 104,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 26,
                   "pullRequestToReward": 17,
                   "issueToReward": 0,
                   "codeReviewToReward": 9,
                   "hidden": false
                 },
                 {
                   "githubUserId": 31901905,
                   "login": "kaelsky",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 127,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 7,
                   "pullRequestToReward": 3,
                   "issueToReward": 0,
                   "codeReviewToReward": 4,
                   "hidden": false
                 },
                 {
                   "githubUserId": 5160414,
                   "login": "haydencleary",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 140,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 20,
                   "pullRequestToReward": 13,
                   "issueToReward": 1,
                   "codeReviewToReward": 6,
                   "hidden": false
                 },
                 {
                   "githubUserId": 34384633,
                   "login": "tdelabro",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                   "isRegistered": false,
                   "id": null,
                   "contributionCount": 146,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 1,
                   "pullRequestToReward": 1,
                   "issueToReward": 0,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 16590657,
                   "login": "PierreOucif",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 148,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 12,
                   "pullRequestToReward": 1,
                   "issueToReward": 0,
                   "codeReviewToReward": 11,
                   "hidden": false
                 },
                 {
                   "githubUserId": 21149076,
                   "login": "oscarwroche",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 213,
                   "rewardCount": 1,
                   "earned": {
                     "totalAmount": 1010.00,
                     "details": [
                       {
                         "amount": 1000,
                         "prettyAmount": 1000,
                         "currency": {
                           "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                           "code": "USDC",
                           "name": "USD Coin",
                           "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                           "decimals": 6
                         },
                         "usdEquivalent": 1010.00,
                         "usdConversionRate": null
                       }
                     ]
                   },
                   "contributionToRewardCount": 2,
                   "pullRequestToReward": 2,
                   "issueToReward": 0,
                   "codeReviewToReward": 0,
                   "hidden": false
                 },
                 {
                   "githubUserId": 4435377,
                   "login": "Bernardstanislas",
                   "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 380,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 7,
                   "pullRequestToReward": 6,
                   "issueToReward": 0,
                   "codeReviewToReward": 1,
                   "hidden": false
                 },
                 {
                   "githubUserId": 595505,
                   "login": "ofux",
                   "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 574,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 21,
                   "pullRequestToReward": 18,
                   "issueToReward": 2,
                   "codeReviewToReward": 1,
                   "hidden": false
                 },
                 {
                   "githubUserId": 43467246,
                   "login": "AnthonyBuisset",
                   "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                   "isRegistered": true,
                   "id": null,
                   "contributionCount": 888,
                   "rewardCount": 0,
                   "earned": {
                     "totalAmount": 0,
                     "details": []
                   },
                   "contributionToRewardCount": 24,
                   "pullRequestToReward": 11,
                   "issueToReward": 11,
                   "codeReviewToReward": 2,
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "id": null,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 0,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "id": null,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 3,
                  "pullRequestToReward": 3,
                  "issueToReward": 0,
                  "codeReviewToReward": 0,
                  "hidden": false
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "id": null,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 26,
                  "pullRequestToReward": 17,
                  "issueToReward": 0,
                  "codeReviewToReward": 9,
                  "hidden": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "id": null,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 20,
                  "pullRequestToReward": 13,
                  "issueToReward": 1,
                  "codeReviewToReward": 6,
                  "hidden": false
                }
              ]
            }
            """;

    private static final String GET_PROJECT_CONTRIBUTORS_ANONYMOUS = """
            {
              "totalPageNumber": 1,
              "totalItemNumber": 20,
              "hasMore": false,
              "nextPageIndex": 0,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 45336124,
                  "login": "MaximeBeasse",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45336124?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 102823832,
                  "login": "SamuelKer",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 8495664,
                  "login": "doomed-theory",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/8495664?v=4",
                  "isRegistered": false,
                  "contributionCount": 1,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
                  "isRegistered": false,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
                  "isRegistered": true,
                  "contributionCount": 2,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
                  "isRegistered": false,
                  "contributionCount": 20,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "hidden": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "AbdelStark",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "isRegistered": false,
                  "contributionCount": 21,
                  "rewardCount": 1,
                  "earned": {
                    "totalAmount": 1515.00,
                    "details": [
                      {
                        "amount": 1500,
                        "prettyAmount": 1500,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        },
                        "usdEquivalent": 1515.00
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                  "isRegistered": false,
                  "contributionCount": 102,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "isRegistered": true,
                  "contributionCount": 127,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "isRegistered": false,
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "contributionCount": 148,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "isRegistered": true,
                  "contributionCount": 213,
                  "rewardCount": 1,
                  "earned": {
                    "totalAmount": 1010.00,
                    "details": [
                      {
                        "amount": 1000,
                        "prettyAmount": 1000,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        },
                        "usdEquivalent": 1010.00
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "isRegistered": true,
                  "contributionCount": 380,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                  "isRegistered": true,
                  "contributionCount": 574,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                  "isRegistered": true,
                  "contributionCount": 888,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "isRegistered": false,
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
                  "isRegistered": true,
                  "contributionCount": 46,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "isRegistered": false,
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "isRegistered": true,
                  "contributionCount": 140,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
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

    private static final String GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES = """
            {
              "totalPageNumber": 9,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 1,
              "hasHiddenContributors": false,
              "contributors": [
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "id": null,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 35210.00,
                    "details": [
                      {
                        "amount": 450,
                        "prettyAmount": 450,
                        "currency": {
                          "id": "00ca98a5-0197-4b76-a208-4bfc55ea8256",
                          "code": "OP",
                          "name": "Optimism",
                          "logoUrl": null,
                          "decimals": 18
                        },
                        "usdEquivalent": 643.5,
                        "usdConversionRate": null
                      },
                      {
                        "amount": 2000,
                        "prettyAmount": 2000,
                        "currency": {
                          "id": "48388edb-fda2-4a32-b228-28152a147500",
                          "code": "APT",
                          "name": "Aptos Coin",
                          "logoUrl": null,
                          "decimals": 8
                        },
                        "usdEquivalent": 1120,
                        "usdConversionRate": null
                      },
                      {
                        "amount": 2000,
                        "prettyAmount": 2000,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        },
                        "usdEquivalent": 2020.00,
                        "usdConversionRate": null
                      },
                      {
                        "amount": 20.5,
                        "prettyAmount": 20.5,
                        "currency": {
                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                          "code": "ETH",
                          "name": "Ether",
                          "logoUrl": null,
                          "decimals": 18
                        },
                        "usdEquivalent": 31426.5,
                        "usdConversionRate": null
                      },
                      {
                        "amount": 500000,
                        "prettyAmount": 500000,
                        "currency": {
                          "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                          "code": "STRK",
                          "name": "StarkNet Token",
                          "logoUrl": null,
                          "decimals": 18
                        },
                        "usdEquivalent": 0,
                        "usdConversionRate": null
                      }
                    ]
                  },
                  "contributionToRewardCount": 11,
                  "pullRequestToReward": 1,
                  "issueToReward": 0,
                  "codeReviewToReward": 10,
                  "hidden": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "AbdelStark",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "isRegistered": false,
                  "id": null,
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 0,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
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
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "isRegistered": true,
                  "id": null,
                  "contributionCount": 147,
                  "rewardCount": 6,
                  "earned": {
                    "totalAmount": 6060.00,
                    "details": [
                      {
                        "amount": 6000,
                        "prettyAmount": 6000,
                        "currency": {
                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                          "code": "USDC",
                          "name": "USD Coin",
                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                          "decimals": 6
                        },
                        "usdEquivalent": 6060.00,
                        "usdConversionRate": null
                      }
                    ]
                  },
                  "contributionToRewardCount": 11,
                  "pullRequestToReward": 1,
                  "issueToReward": 0,
                  "codeReviewToReward": 10,
                  "hidden": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "AbdelStark",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "isRegistered": false,
                  "id": null,
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": []
                  },
                  "contributionToRewardCount": 0,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
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
        final var projectId = UUID.fromString("594ca5ca-48f7-49a8-9c26-84b949d4fdd9");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "100", "sort", "CONTRIBUTION_COUNT")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_ANONYMOUS);
    }

    @Test
    @Order(1)
    void should_find_project_contributors_as_anonymous_user_with_login_filter() {
        // Given
        final var projectId = UUID.fromString("594ca5ca-48f7-49a8-9c26-84b949d4fdd9");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
        final var projectId = UUID.fromString("594ca5ca-48f7-49a8-9c26-84b949d4fdd9");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "4", "sort", "CONTRIBUTION_COUNT", "direction", "DESC")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_0);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
        final String jwt = userAuthHelper.authenticateGregoire().jwt();
        final var projectId = UUID.fromString("594ca5ca-48f7-49a8-9c26-84b949d4fdd9");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "100", "sort", "CONTRIBUTION_COUNT")))
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var veryDustyBot = 129528947L;

        // When
        client.post()
                .uri(getApiURI(String.format(PROJECTS_HIDE_CONTRIBUTOR, projectId, veryDustyBot)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                .expectStatus().isNoContent();

        // Then
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final var reward1 = rewardRepository.findById(UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0")).orElseThrow();
        reward1.currencyId(currencyRepository.findByCode("ETH").orElseThrow().id());
        reward1.amount(BigDecimal.valueOf(20.5));
        rewardRepository.save(reward1);
        rewardStatusStorage.updateUsdAmount(RewardId.of(reward1.id()),
                new ConvertedAmount(Amount.of(BigDecimal.valueOf(31426.5)), BigDecimal.valueOf(31426.5).divide(reward1.amount())));

        final var reward2 = rewardRepository.findById(UUID.fromString("e1498a17-5090-4071-a88a-6f0b0c337c3a")).orElseThrow();
        reward2.currencyId(currencyRepository.findByCode("APT").orElseThrow().id());
        reward2.amount(BigDecimal.valueOf(2000));
        rewardRepository.save(reward2);
        rewardStatusStorage.updateUsdAmount(RewardId.of(reward2.id()),
                new ConvertedAmount(Amount.of(BigDecimal.valueOf(1120)), BigDecimal.valueOf(1120).divide(reward2.amount())));

        final var reward3 = rewardRepository.findById(UUID.fromString("40fda3c6-2a3f-4cdd-ba12-0499dd232d53")).orElseThrow();
        reward3.currencyId(currencyRepository.findByCode("OP").orElseThrow().id());
        reward3.amount(BigDecimal.valueOf(450));
        rewardRepository.save(reward3);
        rewardStatusStorage.updateUsdAmount(RewardId.of(reward3.id()),
                new ConvertedAmount(Amount.of(BigDecimal.valueOf(643.5)), BigDecimal.valueOf(643.5).divide(reward3.amount())));

        final var reward4 = rewardRepository.findById(UUID.fromString("5b96ca1e-4ad2-41c1-8819-520b885d9223")).orElseThrow();
        reward4.currencyId(currencyRepository.findByCode("STRK").orElseThrow().id());
        reward4.amount(BigDecimal.valueOf(500000));
        rewardRepository.save(reward4);
        rewardStatusStorage.updateUsdAmount(RewardId.of(reward4.id()), null);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
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
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
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
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "0", "pageSize", "2", "sort", "EARNED", "direction", "DESC")))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json(GET_PROJECTS_CONTRIBUTORS_WITH_MULTI_CURRENCIES_AND_IGNORED_CONTRIBUTIONS);
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            ",DESC",
            ",ASC",
            "LOGIN, DESC",
            "LOGIN, ASC",
            "LOGIN,",
            "CONTRIBUTION_COUNT, DESC",
            "CONTRIBUTION_COUNT, ASC",
            "CONTRIBUTION_COUNT,",
            "REWARD_COUNT, DESC",
            "REWARD_COUNT, ASC",
            "REWARD_COUNT,",
            "EARNED, DESC",
            "EARNED, ASC",
            "EARNED,",
            "TO_REWARD_COUNT, DESC",
            "TO_REWARD_COUNT, ASC",
            "TO_REWARD_COUNT,"
    })
    @Order(10)
    void should_find_project_contributors_with_sorting(String sort, String direction) {
        // Given
        final String jwt = userAuthHelper.authenticatePierre().jwt();
        final var projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final var queryParams = new HashMap<>(Map.of("pageIndex", "0", "pageSize", "10000"));
        if (sort != null) {
            queryParams.put("sort", sort);
        }
        if (direction != null) {
            queryParams.put("direction", direction);
        }

        // When
        final var contributors = client.get()
                .uri(getApiURI(String.format(PROJECTS_CONTRIBUTORS, projectId), queryParams))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(ContributorsPageResponse.class)
                .returnResult()
                .getResponseBody().getContributors();

        final var first = "DESC".equals(direction) ? contributors.get(contributors.size() - 1) : contributors.get(0);
        final var last = "DESC".equals(direction) ? contributors.get(0) : contributors.get(contributors.size() - 1);

        switch (Optional.ofNullable(sort).orElse("")) {
            default:
            case "LOGIN":
                assertThat(first.getLogin().compareTo(last.getLogin())).isLessThanOrEqualTo(0);
                break;
            case "CONTRIBUTION_COUNT":
                assertThat(first.getContributionCount()).isLessThanOrEqualTo(last.getContributionCount());
                break;
            case "REWARD_COUNT":
                assertThat(first.getRewardCount()).isLessThanOrEqualTo(last.getRewardCount());
                break;
            case "EARNED":
                assertThat(first.getEarned().getTotalAmount()).isLessThanOrEqualTo(last.getEarned().getTotalAmount());
                break;
            case "TO_REWARD_COUNT":
                assertThat(first.getContributionToRewardCount()).isLessThanOrEqualTo(last.getContributionToRewardCount());
                break;
        }
    }
}
