package onlydust.com.marketplace.api.it.bo;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Map;

@TagBO
public class BackofficeBiApiIT extends AbstractMarketplaceBackOfficeApiIT {
    UserAuthHelper.AuthenticatedBackofficeUser mehdi;

    @Test
    @Order(21)
    void should_reject_invoice_download_if_wrong_token() {

        client.get()
                .uri(getApiURI(EXTERNAL_GET_CONTRIBUTORS_BI, Map.of("contributorLogins", "AnthonyBuisset,enitrat", "token", "INVALID_TOKEN")))
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_return_contributors_bi_data() {
        // When
        client.get()
                .uri(getApiURI(EXTERNAL_GET_CONTRIBUTORS_BI, Map.of("contributorLogins", "AnthonyBuisset,enitrat", "token", "BO_TOKEN")))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "contributors": [
                            {
                              "id": 43467246,
                              "login": "AnthonyBuisset",
                              "telegram": "https://t.me/abuisset",
                              "maintainedProjectCount": 2,
                              "contributor": {
                                "globalRank": 1,
                                "globalRankPercentile": 0.000041734485205125,
                                "bio": "FullStack engineerr",
                                "signedUpOnGithubAt": "2018-09-21T06:45:50Z",
                                "signedUpAt": "2022-12-12T08:51:58.48559Z",
                                "contacts": [
                                  {
                                    "channel": "TELEGRAM",
                                    "contact": "https://t.me/abuisset",
                                    "visibility": "public"
                                  },
                                  {
                                    "channel": "TWITTER",
                                    "contact": "https://twitter.com/abuisset",
                                    "visibility": "public"
                                  },
                                  {
                                    "channel": "DISCORD",
                                    "contact": "antho",
                                    "visibility": "public"
                                  }
                                ],
                                "githubUserId": 43467246,
                                "githubLogin": null,
                                "githubAvatarUrl": null,
                                "email": null,
                                "isRegistered": true,
                                "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4"
                              },
                              "globalData": {
                                "languages": [
                                  "Rust",
                                  "Cairo",
                                  "Javascript",
                                  "Python",
                                  "Java",
                                  "Typescript"
                                ],
                                "ecosystems": [
                                  "Aptos",
                                  "Avail",
                                  "Aztec",
                                  "Ethereum",
                                  "Lava",
                                  "Optimism",
                                  "Starknet",
                                  "Zama"
                                ],
                                "categories": null,
                                "completedContributionCount": 435,
                                "completedIssueCount": 44,
                                "completedPrCount": 265,
                                "completedCodeReviewCount": 126,
                                "odCompletedContributionCount": 137,
                                "odCompletedIssueCount": 13,
                                "odCompletedPrCount": 102,
                                "odCompletedCodeReviewCount": 22,
                                "inProgressIssueCount": 0,
                                "odInProgressIssueCount": 0,
                                "last1MonthCompletedPrCount": 0,
                                "last1MonthOdCompletedPrCount": 0,
                                "last3MonthCompletedPrCount": 0,
                                "last3MonthOdCompletedPrCount": 0,
                                "lastContributionDate": "2023-12-04",
                                "lastOdContributionDate": "2023-09-19",
                                "lastPrDate": "2023-12-04",
                                "lastOdPrDate": "2023-09-19",
                                "applicationsPending": 0,
                                "totalApplications": 0,
                                "rewardCount": 21,
                                "rewardedUsdTotal": 2692632.5
                              },
                              "perProjectData": [
                                {
                                  "projectId": "29cdf359-f60c-41a0-8b11-18d6841311f6",
                                  "projectName": "kaaper 3",
                                  "projectSlug": "kaaper-3",
                                  "languages": null,
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 3,
                                  "rewardedUsdTotal": 2525.0
                                },
                                {
                                  "projectId": "d4e8ab3b-a4a8-493d-83bd-a4c8283b94f9",
                                  "projectName": "oscar's awesome project",
                                  "projectSlug": "oscars-awesome-project",
                                  "languages": null,
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 1,
                                  "rewardedUsdTotal": 1010.0
                                },
                                {
                                  "projectId": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                                  "projectName": "Pizzeria Yoshi !",
                                  "projectSlug": "pizzeria-yoshi-",
                                  "languages": [
                                    "Typescript",
                                    "Solidity",
                                    "Python",
                                    "Cairo"
                                  ],
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 2,
                                  "rewardedUsdTotal": 4260.0
                                },
                                {
                                  "projectId": "00490be6-2c03-4720-993b-aea3e07edd81",
                                  "projectName": "Zama",
                                  "projectSlug": "zama",
                                  "languages": [
                                    "Python",
                                    "Cairo"
                                  ],
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 2,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 1,
                                  "completedCodeReviewCount": 1,
                                  "odCompletedContributionCount": 2,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 1,
                                  "odCompletedCodeReviewCount": 1,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2022-06-01",
                                  "lastOdContributionDate": "2022-06-01",
                                  "lastPrDate": "2022-04-12",
                                  "lastOdPrDate": "2022-04-12",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                },
                                {
                                  "projectId": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                                  "projectName": "B Conseil",
                                  "projectSlug": "b-conseil",
                                  "languages": null,
                                  "ecosystems": [
                                    "Starknet",
                                    "Optimism",
                                    "Lava"
                                  ],
                                  "categories": null,
                                  "completedContributionCount": 5,
                                  "completedIssueCount": 2,
                                  "completedPrCount": 2,
                                  "completedCodeReviewCount": 1,
                                  "odCompletedContributionCount": 5,
                                  "odCompletedIssueCount": 2,
                                  "odCompletedPrCount": 2,
                                  "odCompletedCodeReviewCount": 1,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-04-24",
                                  "lastOdContributionDate": "2023-04-24",
                                  "lastPrDate": "2023-04-24",
                                  "lastOdPrDate": "2023-04-24",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                },
                                {
                                  "projectId": "5aabf0f1-7495-4bff-8de2-4396837ce6b4",
                                  "projectName": "Marketplace 2",
                                  "projectSlug": "marketplace-2",
                                  "languages": null,
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 1,
                                  "rewardedUsdTotal": 890990.0
                                },
                                {
                                  "projectId": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "projectName": "kaaper",
                                  "projectSlug": "kaaper",
                                  "languages": [
                                    "Javascript",
                                    "Typescript",
                                    "Rust",
                                    "Cairo"
                                  ],
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 127,
                                  "completedIssueCount": 11,
                                  "completedPrCount": 98,
                                  "completedCodeReviewCount": 18,
                                  "odCompletedContributionCount": 127,
                                  "odCompletedIssueCount": 11,
                                  "odCompletedPrCount": 98,
                                  "odCompletedCodeReviewCount": 18,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-09-19",
                                  "lastOdContributionDate": "2023-09-19",
                                  "lastPrDate": "2023-09-19",
                                  "lastOdPrDate": "2023-09-19",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 12,
                                  "rewardedUsdTotal": 1792080.0
                                },
                                {
                                  "projectId": "a0c91aee-9770-4000-a893-953ddcbd62a7",
                                  "projectName": "Aldébaran du Taureau",
                                  "projectSlug": "aldbaran-du-taureau",
                                  "languages": null,
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 2,
                                  "rewardedUsdTotal": 1767.5
                                },
                                {
                                  "projectId": "7d04163c-4187-4313-8066-61504d34fc56",
                                  "projectName": "Bretzel",
                                  "projectSlug": "bretzel",
                                  "languages": [
                                    "Typescript"
                                  ],
                                  "ecosystems": [
                                    "Ethereum",
                                    "Aptos",
                                    "Zama"
                                  ],
                                  "categories": null,
                                  "completedContributionCount": 0,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 0,
                                  "completedCodeReviewCount": 0,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": null,
                                  "lastOdContributionDate": null,
                                  "lastPrDate": null,
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                },
                                {
                                  "projectId": null,
                                  "projectName": null,
                                  "projectSlug": null,
                                  "languages": null,
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 298,
                                  "completedIssueCount": 31,
                                  "completedPrCount": 163,
                                  "completedCodeReviewCount": 104,
                                  "odCompletedContributionCount": 0,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 0,
                                  "odCompletedCodeReviewCount": 0,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-12-04",
                                  "lastOdContributionDate": null,
                                  "lastPrDate": "2023-12-04",
                                  "lastOdPrDate": null,
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                },
                                {
                                  "projectId": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                                  "projectName": "Mooooooonlight",
                                  "projectSlug": "mooooooonlight",
                                  "languages": [
                                    "Javascript",
                                    "Typescript",
                                    "Rust"
                                  ],
                                  "ecosystems": [
                                    "Starknet",
                                    "Aztec"
                                  ],
                                  "categories": null,
                                  "completedContributionCount": 3,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 1,
                                  "completedCodeReviewCount": 2,
                                  "odCompletedContributionCount": 3,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 1,
                                  "odCompletedCodeReviewCount": 2,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-07-10",
                                  "lastOdContributionDate": "2023-07-10",
                                  "lastPrDate": "2023-07-10",
                                  "lastOdPrDate": "2023-07-10",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                }
                              ]
                            },
                            {
                              "id": 60658558,
                              "login": "enitrat",
                              "telegram": null,
                              "maintainedProjectCount": 0,
                              "contributor": {
                                "globalRank": 17,
                                "globalRankPercentile": 0.0007094862484871249,
                                "bio": null,
                                "signedUpOnGithubAt": "2020-02-04T13:19:48Z",
                                "signedUpAt": null,
                                "contacts": null,
                                "githubUserId": 60658558,
                                "githubLogin": null,
                                "githubAvatarUrl": null,
                                "email": null,
                                "isRegistered": false,
                                "id": null
                              },
                              "globalData": {
                                "languages": [
                                  "Cairo",
                                  "Python",
                                  "Solidity",
                                  "Typescript"
                                ],
                                "ecosystems": null,
                                "categories": null,
                                "completedContributionCount": 46,
                                "completedIssueCount": 11,
                                "completedPrCount": 27,
                                "completedCodeReviewCount": 8,
                                "odCompletedContributionCount": 46,
                                "odCompletedIssueCount": 11,
                                "odCompletedPrCount": 27,
                                "odCompletedCodeReviewCount": 8,
                                "inProgressIssueCount": 2,
                                "odInProgressIssueCount": 2,
                                "last1MonthCompletedPrCount": 0,
                                "last1MonthOdCompletedPrCount": 0,
                                "last3MonthCompletedPrCount": 0,
                                "last3MonthOdCompletedPrCount": 0,
                                "lastContributionDate": "2023-11-30",
                                "lastOdContributionDate": "2023-11-30",
                                "lastPrDate": "2023-11-30",
                                "lastOdPrDate": "2023-11-30",
                                "applicationsPending": 0,
                                "totalApplications": 0,
                                "rewardCount": 1,
                                "rewardedUsdTotal": 2020.0
                              },
                              "perProjectData": [
                                {
                                  "projectId": "3c22af5d-2cf8-48a1-afa0-c3441df7fb3b",
                                  "projectName": "Taco Tuesday",
                                  "projectSlug": "taco-tuesday",
                                  "languages": [
                                    "Typescript",
                                    "Rust",
                                    "Cairo"
                                  ],
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 40,
                                  "completedIssueCount": 11,
                                  "completedPrCount": 22,
                                  "completedCodeReviewCount": 7,
                                  "odCompletedContributionCount": 40,
                                  "odCompletedIssueCount": 11,
                                  "odCompletedPrCount": 22,
                                  "odCompletedCodeReviewCount": 7,
                                  "inProgressIssueCount": 2,
                                  "odInProgressIssueCount": 2,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-11-30",
                                  "lastOdContributionDate": "2023-11-30",
                                  "lastPrDate": "2023-11-30",
                                  "lastOdPrDate": "2023-11-30",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 1,
                                  "rewardedUsdTotal": 2020.0
                                },
                                {
                                  "projectId": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                                  "projectName": "kaaper",
                                  "projectSlug": "kaaper",
                                  "languages": [
                                    "Javascript",
                                    "Typescript",
                                    "Rust",
                                    "Cairo"
                                  ],
                                  "ecosystems": null,
                                  "categories": null,
                                  "completedContributionCount": 6,
                                  "completedIssueCount": 0,
                                  "completedPrCount": 5,
                                  "completedCodeReviewCount": 1,
                                  "odCompletedContributionCount": 6,
                                  "odCompletedIssueCount": 0,
                                  "odCompletedPrCount": 5,
                                  "odCompletedCodeReviewCount": 1,
                                  "inProgressIssueCount": 0,
                                  "odInProgressIssueCount": 0,
                                  "last1MonthCompletedPrCount": 0,
                                  "last1MonthOdCompletedPrCount": 0,
                                  "last3MonthCompletedPrCount": 0,
                                  "last3MonthOdCompletedPrCount": 0,
                                  "lastContributionDate": "2023-02-09",
                                  "lastOdContributionDate": "2023-02-09",
                                  "lastPrDate": "2023-01-23",
                                  "lastOdPrDate": "2023-01-23",
                                  "applicationsPending": 0,
                                  "totalApplications": 0,
                                  "rewardCount": 0,
                                  "rewardedUsdTotal": 0.0
                                }
                              ]
                            }
                          ]
                        }
                        
                        """);
    }

}
