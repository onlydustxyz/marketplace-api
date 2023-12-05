package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CryptoUsdQuotesEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.CryptoUsdQuotesRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectsGetContributorsApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_0 = """
            {
               "totalPageNumber": 5,
               "totalItemNumber": 18,
               "hasMore": true,
               "nextPageIndex": 1,
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
                     "totalAmount": 0,
                     "details": null
                   },
                   "contributionToRewardCount": null,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null
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
                     "totalAmount": 0,
                     "details": null
                   },
                   "contributionToRewardCount": null,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null
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
                     "totalAmount": 0,
                     "details": null
                   },
                   "contributionToRewardCount": null,
                   "pullRequestToReward": null,
                   "issueToReward": null,
                   "codeReviewToReward": null
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
    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_1 = """
            {
              "totalPageNumber": 5,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 2,
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
                    "totalAmount": 6000,
                    "details": [
                      {
                        "totalAmount": 6000,
                        "totalDollarsEquivalent": 6000,
                        "currency": "USD"
                      }
                    ]
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null
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
                  "codeReviewToReward": null
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
                           "codeReviewToReward": 0
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
                           "codeReviewToReward": 1
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
                           "codeReviewToReward": 2
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
                           "codeReviewToReward": 1
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
                           "codeReviewToReward": 12
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 21,
                           "pullRequestToReward": 8,
                           "issueToReward": 0,
                           "codeReviewToReward": 13
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 36,
                           "pullRequestToReward": 25,
                           "issueToReward": 0,
                           "codeReviewToReward": 11
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
                           "codeReviewToReward": 12
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
                           "codeReviewToReward": 47
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
                           "codeReviewToReward": 26
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
                           "codeReviewToReward": 39
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
                           "codeReviewToReward": 82
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 146,
                           "pullRequestToReward": 107,
                           "issueToReward": 0,
                           "codeReviewToReward": 39
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
                             "totalAmount": 6000,
                             "details": [
                               {
                                 "totalAmount": 6000,
                                 "totalDollarsEquivalent": 6000,
                                 "currency": "USD"
                               }
                             ]
                           },
                           "contributionToRewardCount": 122,
                           "pullRequestToReward": 5,
                           "issueToReward": 0,
                           "codeReviewToReward": 117
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 153,
                           "pullRequestToReward": 57,
                           "issueToReward": 0,
                           "codeReviewToReward": 96
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 374,
                           "pullRequestToReward": 115,
                           "issueToReward": 0,
                           "codeReviewToReward": 259
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 569,
                           "pullRequestToReward": 247,
                           "issueToReward": 2,
                           "codeReviewToReward": 320
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
                             "totalAmount": 0,
                             "details": null
                           },
                           "contributionToRewardCount": 797,
                           "pullRequestToReward": 416,
                           "issueToReward": 11,
                           "codeReviewToReward": 370
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
                         "totalAmount": 0,
                         "details": null
                       },
                       "contributionToRewardCount": 36,
                       "pullRequestToReward": 25,
                       "issueToReward": 0,
                       "codeReviewToReward": 11
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
                       "codeReviewToReward": 12
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
                       "codeReviewToReward": 26
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
                       "codeReviewToReward": 82
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
                      "codeReviewToReward": null
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
                      "codeReviewToReward": null
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
                      "codeReviewToReward": null
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
                      "codeReviewToReward": null
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
                      "codeReviewToReward": null
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
                        "totalAmount": 0,
                        "details": null
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                        "totalAmount": 0,
                        "details": null
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
                        "totalAmount": 0,
                        "details": null
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                        "totalAmount": 6000,
                        "details": [
                          {
                            "totalAmount": 6000,
                            "totalDollarsEquivalent": 6000,
                            "currency": "USD"
                          }
                        ]
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                        "totalAmount": 0,
                        "details": null
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                        "totalAmount": 0,
                        "details": null
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                        "totalAmount": 0,
                        "details": null
                      },
                      "contributionToRewardCount": null,
                      "pullRequestToReward": null,
                      "issueToReward": null,
                      "codeReviewToReward": null
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
                    "totalAmount": 0,
                    "details": null
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
                    "totalAmount": 35190.00,
                    "details": [
                      {
                        "totalAmount": 450,
                        "totalDollarsEquivalent": 643.50,
                        "currency": "OP"
                      },
                      {
                        "totalAmount": 500000,
                        "totalDollarsEquivalent": 0,
                        "currency": "STARK"
                      },
                      {
                        "totalAmount": 20.5,
                        "totalDollarsEquivalent": 31426.5,
                        "currency": "ETH"
                      },
                      {
                        "totalAmount": 2000,
                        "totalDollarsEquivalent": 1120.00,
                        "currency": "APT"
                      },
                      {
                        "totalAmount": 2000,
                        "totalDollarsEquivalent": 2000,
                        "currency": "USD"
                      }
                    ]
                  },
                  "contributionToRewardCount": 120,
                  "pullRequestToReward": 4,
                  "issueToReward": 0,
                  "codeReviewToReward": 116
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
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370
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
                    "totalAmount": 6000,
                    "details": [
                      {
                        "totalAmount": 6000,
                        "totalDollarsEquivalent": 6000,
                        "currency": "USD"
                      }
                    ]
                  },
                  "contributionToRewardCount": 120,
                  "pullRequestToReward": 4,
                  "issueToReward": 0,
                  "codeReviewToReward": 116
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
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370
                }
              ]
            }
            """;


    @Autowired
    HasuraUserHelper userHelper;
    @Autowired
    IgnoredContributionsRepository ignoredContributionsRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    CryptoUsdQuotesRepository cryptoUsdQuotesRepository;

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
        final String jwt = userHelper.authenticatePierre().jwt();
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
    @Order(4)
    void should_find_project_contributors_as_project_lead_with_login_filter() {
        // Given
        final String jwt = userHelper.authenticatePierre().jwt();
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
        final String jwt = userHelper.authenticatePierre().jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        final PaymentRequestEntity reward1 = paymentRequestRepository.findById(UUID.fromString("8fe07ae1-cf3b-4401" +
                                                                                               "-8958-a9e0b0aec7b0")).orElseThrow();
        reward1.setCurrency(CurrencyEnumEntity.eth);
        reward1.setAmount(BigDecimal.valueOf(20.5));
        paymentRequestRepository.save(reward1);

        final PaymentRequestEntity reward2 = paymentRequestRepository.findById(UUID.fromString("e1498a17-5090-4071" +
                                                                                               "-a88a-6f0b0c337c3a")).orElseThrow();
        reward2.setCurrency(CurrencyEnumEntity.apt);
        reward2.setAmount(BigDecimal.valueOf(2000));
        paymentRequestRepository.save(reward2);

        final PaymentRequestEntity reward3 = paymentRequestRepository.findById(UUID.fromString("40fda3c6-2a3f-4cdd" +
                                                                                               "-ba12-0499dd232d53")).orElseThrow();
        reward3.setCurrency(CurrencyEnumEntity.op);
        reward3.setAmount(BigDecimal.valueOf(450));
        paymentRequestRepository.save(reward3);

        final PaymentRequestEntity reward4 = paymentRequestRepository.findById(UUID.fromString("5b96ca1e-4ad2-41c1" +
                                                                                               "-8819-520b885d9223")).orElseThrow();
        reward4.setCurrency(CurrencyEnumEntity.stark);
        reward4.setAmount(BigDecimal.valueOf(500000));
        paymentRequestRepository.save(reward4);
        cryptoUsdQuotesRepository.deleteAll();
        cryptoUsdQuotesRepository.save(new CryptoUsdQuotesEntity(CurrencyEnumEntity.eth, BigDecimal.valueOf(1533),
                new Date()));
        cryptoUsdQuotesRepository.save(new CryptoUsdQuotesEntity(CurrencyEnumEntity.op, BigDecimal.valueOf(1.43),
                new Date()));
        cryptoUsdQuotesRepository.save(new CryptoUsdQuotesEntity(CurrencyEnumEntity.apt, BigDecimal.valueOf(0.56),
                new Date()));


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
        final String jwt = userHelper.authenticatePierre().jwt();
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
