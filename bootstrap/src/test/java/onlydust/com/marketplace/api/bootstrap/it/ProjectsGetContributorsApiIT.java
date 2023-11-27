package onlydust.com.marketplace.api.bootstrap.it;

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
              "totalPageNumber": 6,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 1,
              "contributors": [
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                }
              ]
            }
                        
            """;
    private static final String GET_PROJECT_CONTRIBUTORS_PAGE_1 = """
            {
              "totalPageNumber": 6,
              "totalItemNumber": 18,
              "hasMore": true,
              "nextPageIndex": 2,
              "contributors": [
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "contributionCount": 133,
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
                  "codeReviewToReward": null,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 129528947,
                  "login": "VeryDustyBot",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 698957,
                  "login": "ltoussaint",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 112474158,
                  "login": "onlydust-contributor",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 98529704,
                  "login": "tekkac",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "abdelhamidbakhta",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 21,
                  "pullRequestToReward": 8,
                  "issueToReward": 0,
                  "codeReviewToReward": 13,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 36,
                  "pullRequestToReward": 25,
                  "issueToReward": 0,
                  "codeReviewToReward": 11,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "contributionCount": 56,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 56,
                  "pullRequestToReward": 44,
                  "issueToReward": 0,
                  "codeReviewToReward": 12,
                  "isRegistered": false
                },
                {
                  "githubUserId": 143011364,
                  "login": "pixelfact",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                  "contributionCount": 72,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 72,
                  "pullRequestToReward": 38,
                  "issueToReward": 0,
                  "codeReviewToReward": 34,
                  "isRegistered": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "contributionCount": 100,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 100,
                  "pullRequestToReward": 37,
                  "issueToReward": 1,
                  "codeReviewToReward": 62,
                  "isRegistered": true
                },
                {
                  "githubUserId": 31901905,
                  "login": "kaelsky",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
                  "contributionCount": 104,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 104,
                  "pullRequestToReward": 81,
                  "issueToReward": 0,
                  "codeReviewToReward": 23,
                  "isRegistered": true
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "contributionCount": 133,
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
                  "contributionToRewardCount": 108,
                  "pullRequestToReward": 4,
                  "issueToReward": 0,
                  "codeReviewToReward": 104,
                  "isRegistered": true
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 146,
                  "pullRequestToReward": 107,
                  "issueToReward": 0,
                  "codeReviewToReward": 39,
                  "isRegistered": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 153,
                  "pullRequestToReward": 57,
                  "issueToReward": 0,
                  "codeReviewToReward": 96,
                  "isRegistered": true
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 374,
                  "pullRequestToReward": 115,
                  "issueToReward": 0,
                  "codeReviewToReward": 259,
                  "isRegistered": true
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 569,
                  "pullRequestToReward": 247,
                  "issueToReward": 2,
                  "codeReviewToReward": 320,
                  "isRegistered": true
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 36,
                  "pullRequestToReward": 25,
                  "issueToReward": 0,
                  "codeReviewToReward": 11,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "contributionCount": 56,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 56,
                  "pullRequestToReward": 44,
                  "issueToReward": 0,
                  "codeReviewToReward": 12,
                  "isRegistered": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "contributionCount": 100,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 100,
                  "pullRequestToReward": 37,
                  "issueToReward": 1,
                  "codeReviewToReward": 62,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/102823832?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 129528947,
                  "login": "VeryDustyBot",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/129528947?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 698957,
                  "login": "ltoussaint",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/698957?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 112474158,
                  "login": "onlydust-contributor",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/112474158?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 98529704,
                  "login": "tekkac",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/98529704?v=4",
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
                  "isRegistered": false
                },
                {
                  "githubUserId": 45264458,
                  "login": "abdelhamidbakhta",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/45264458?v=4",
                  "contributionCount": 21,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10167015,
                  "login": "lechinoix",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "contributionCount": 56,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 143011364,
                  "login": "pixelfact",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/143011364?v=4",
                  "contributionCount": 72,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "contributionCount": 100,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 31901905,
                  "login": "kaelsky",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/31901905?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 16590657,
                  "login": "PierreOucif",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "contributionCount": 133,
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
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 34384633,
                  "login": "tdelabro",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/34384633?v=4",
                  "contributionCount": 146,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 21149076,
                  "login": "oscarwroche",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/21149076?v=4",
                  "contributionCount": 213,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 4435377,
                  "login": "Bernardstanislas",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/4435377?v=4",
                  "contributionCount": 375,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 595505,
                  "login": "ofux",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/595505?v=4",
                  "contributionCount": 570,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10167015?v=4",
                  "contributionCount": 36,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 10922658,
                  "login": "alexbensimon",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/10922658?v=4",
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
                  "isRegistered": true
                },
                {
                  "githubUserId": 17259618,
                  "login": "alexbeno",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/17259618?v=4",
                  "contributionCount": 56,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": false
                },
                {
                  "githubUserId": 5160414,
                  "login": "haydencleary",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/5160414?v=4",
                  "contributionCount": 100,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": null,
                  "pullRequestToReward": null,
                  "issueToReward": null,
                  "codeReviewToReward": null,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "contributionCount": 133,
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
                  "contributionToRewardCount": 108,
                  "pullRequestToReward": 4,
                  "issueToReward": 0,
                  "codeReviewToReward": 104,
                  "isRegistered": true
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "isRegistered": true
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
                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                  "contributionCount": 133,
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
                  "contributionToRewardCount": 106,
                  "pullRequestToReward": 3,
                  "issueToReward": 0,
                  "codeReviewToReward": 103,
                  "isRegistered": true
                },
                {
                  "githubUserId": 43467246,
                  "login": "AnthonyBuisset",
                  "avatarUrl": "https://avatars.githubusercontent.com/u/43467246?v=4",
                  "contributionCount": 885,
                  "rewardCount": 0,
                  "earned": {
                    "totalAmount": 0,
                    "details": null
                  },
                  "contributionToRewardCount": 797,
                  "pullRequestToReward": 416,
                  "issueToReward": 11,
                  "codeReviewToReward": 370,
                  "isRegistered": true
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
                        Map.of("pageIndex", "0", "pageSize", "3", "sort", "CONTRIBUTION_COUNT", "direction", "DESC")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_0);

        // When
        client.get()
                .uri(getApiURI(String.format(PROJECTS_GET_CONTRIBUTORS, projectId),
                        Map.of("pageIndex", "1", "pageSize", "3", "sort", "CONTRIBUTION_COUNT", "direction", "DESC")))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody()
                .json(GET_PROJECT_CONTRIBUTORS_PAGE_1);
    }

    @Test
    @Order(3)
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
    @Order(3)
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
    @Order(4)
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
