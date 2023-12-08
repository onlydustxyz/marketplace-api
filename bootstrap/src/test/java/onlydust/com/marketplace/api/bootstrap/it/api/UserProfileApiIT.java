package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@ActiveProfiles({"hasura_auth"})
public class UserProfileApiIT extends AbstractMarketplaceApiIT {

    private static final String GET_ANTHONY_PRIVATE_PROFILE_JSON_RESPONSE = """
             
            {
               "githubUserId": 43467246,
               "login": "AnthonyBuisset",
               "htmlUrl": "https://github.com/AnthonyBuisset",
               "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
               "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
               "location": "Vence, France",
               "bio": "FullStack engineerr",
               "website": "https://linktr.ee/abuisset",
               "technologies": {
                 "CSS": 4473,
                 "Rust": 404344,
                 "Procfile": 507,
                 "PLpgSQL": 10486,
                 "Makefile": 8658,
                 "HTML": 435,
                 "HCL": 23317,
                 "TypeScript": 2030401,
                 "Dockerfile": 951,
                 "Shell": 1455,
                 "Cairo": 42100,
                 "JavaScript": 2205,
                 "Nix": 12902,
                 "Python": 45301
               },
               "createdAt": "2022-12-12T09:51:58.48559Z",
               "lastSeenAt": "2023-10-05T19:06:50.034Z",
               "firstContributedAt": "2022-04-13T09:00:48Z",
               "cover": "BLUE",
               "contacts": [
                 {
                   "channel": "LINKEDIN",
                   "contact": "",
                   "visibility": "public"
                 },
                 {
                   "channel": "TWITTER",
                   "contact": "https://twitter.com/abuisset",
                   "visibility": "public"
                 },
                 {
                   "channel": "WHATSAPP",
                   "contact": "",
                   "visibility": "public"
                 },
                 {
                   "channel": "EMAIL",
                   "contact": "abuisset@gmail.com",
                   "visibility": "private"
                 },
                 {
                   "channel": "TELEGRAM",
                   "contact": "https://t.me/abuisset",
                   "visibility": "public"
                 },
                 {
                   "channel": "DISCORD",
                   "contact": "antho",
                   "visibility": "public"
                 }
               ],
               "projects": [
                 {
                   "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                   "name": "No sponsors",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": null,
                   "contributorCount": 18,
                   "totalGranted": 10000,
                   "userContributionCount": 888,
                   "userLastContributedAt": "2023-10-12T08:57:23Z",
                   "slug": "no-sponsors",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                   "name": "Zama",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                   "contributorCount": 18,
                   "totalGranted": 0,
                   "userContributionCount": 25,
                   "userLastContributedAt": "2022-06-01T10:34:49Z",
                   "slug": "zama",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                   "name": "OnlyDust Marketplace",
                   "isLead": true,
                   "leadSince": "2023-07-09T07:30:12.544103Z",
                   "logoUrl": null,
                   "contributorCount": 10,
                   "totalGranted": 1000,
                   "userContributionCount": null,
                   "userLastContributedAt": null,
                   "slug": "onlydust-marketplace",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "27ca7e18-9e71-468f-8825-c64fe6b79d66",
                   "name": "B Conseil",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11012050846615405488.png",
                   "contributorCount": 3,
                   "totalGranted": 0,
                   "userContributionCount": 7,
                   "userLastContributedAt": "2023-08-30T09:20:48Z",
                   "slug": "b-conseil",
                   "visibility": "PRIVATE"
                 },
                 {
                   "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                   "name": "Mooooooonlight",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                   "contributorCount": 20,
                   "totalGranted": 4500,
                   "userContributionCount": 888,
                   "userLastContributedAt": "2023-10-12T08:57:23Z",
                   "slug": "mooooooonlight",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                   "name": "Zero title 4",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": null,
                   "contributorCount": 4,
                   "totalGranted": 2314,
                   "userContributionCount": 3,
                   "userLastContributedAt": "2023-07-10T15:41:24Z",
                   "slug": "zero-title-4",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "f39b827f-df73-498c-8853-99bc3f562723",
                   "name": "QA new contributions",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": null,
                   "contributorCount": 18,
                   "totalGranted": 6000,
                   "userContributionCount": 885,
                   "userLastContributedAt": "2023-10-12T08:57:23Z",
                   "slug": "qa-new-contributions",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                   "name": "Pizzeria Yoshi !",
                   "isLead": false,
                   "leadSince": null,
                   "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                   "contributorCount": 886,
                   "totalGranted": 20000,
                   "userContributionCount": 7,
                   "userLastContributedAt": "2023-08-30T09:20:48Z",
                   "slug": "pizzeria-yoshi-",
                   "visibility": "PUBLIC"
                 },
                 {
                   "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                   "name": "kaaper",
                   "isLead": true,
                   "leadSince": "2022-12-23T11:11:51.388471Z",
                   "logoUrl": null,
                   "contributorCount": 21,
                   "totalGranted": 24000,
                   "userContributionCount": 903,
                   "userLastContributedAt": "2023-10-12T08:57:23Z",
                   "slug": "kaaper",
                   "visibility": "PUBLIC"
                 }
               ],
               "stats": {
                 "contributedProjectCount": 8,
                 "leadedProjectCount": 2,
                 "totalsEarned": {
                   "totalAmount": 2692470.00,
                   "details": [
                     {
                       "totalAmount": 19500,
                       "totalDollarsEquivalent": 19500,
                       "currency": "USD"
                     },
                     {
                       "totalAmount": 1500,
                       "totalDollarsEquivalent": 2672970.00,
                       "currency": "ETH"
                     },
                     {
                       "totalAmount": 2000,
                       "totalDollarsEquivalent": 0,
                       "currency": "STARK"
                     }
                   ]
                 },
                 "contributionCount": 1348,
                 "contributionCountPerWeeks": [
                   {
                     "year": 2022,
                     "week": 9,
                     "codeReviewCount": 0,
                     "issueCount": 0,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 10,
                     "codeReviewCount": 0,
                     "issueCount": 1,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 11,
                     "codeReviewCount": 5,
                     "issueCount": 6,
                     "pullRequestCount": 3
                   },
                   {
                     "year": 2022,
                     "week": 12,
                     "codeReviewCount": 1,
                     "issueCount": 1,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2022,
                     "week": 13,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 14,
                     "codeReviewCount": 0,
                     "issueCount": 0,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 15,
                     "codeReviewCount": 0,
                     "issueCount": 0,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2022,
                     "week": 16,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 0
                   },
                   {
                     "year": 2022,
                     "week": 17,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2022,
                     "week": 18,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 3
                   },
                   {
                     "year": 2022,
                     "week": 19,
                     "codeReviewCount": 11,
                     "issueCount": 0,
                     "pullRequestCount": 2
                   },
                   {
                     "year": 2022,
                     "week": 20,
                     "codeReviewCount": 12,
                     "issueCount": 4,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2022,
                     "week": 21,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 2
                   },
                   {
                     "year": 2022,
                     "week": 22,
                     "codeReviewCount": 1,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2022,
                     "week": 23,
                     "codeReviewCount": 0,
                     "issueCount": 8,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2022,
                     "week": 24,
                     "codeReviewCount": 3,
                     "issueCount": 5,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2022,
                     "week": 25,
                     "codeReviewCount": 7,
                     "issueCount": 3,
                     "pullRequestCount": 3
                   },
                   {
                     "year": 2022,
                     "week": 26,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 5
                   },
                   {
                     "year": 2022,
                     "week": 27,
                     "codeReviewCount": 15,
                     "issueCount": 9,
                     "pullRequestCount": 21
                   },
                   {
                     "year": 2022,
                     "week": 28,
                     "codeReviewCount": 11,
                     "issueCount": 5,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2022,
                     "week": 29,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 30,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 1
                   },
                   {
                     "year": 2022,
                     "week": 31,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 20
                   },
                   {
                     "year": 2022,
                     "week": 32,
                     "codeReviewCount": 1,
                     "issueCount": 0,
                     "pullRequestCount": 0
                   },
                   {
                     "year": 2022,
                     "week": 33,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 2
                   },
                   {
                     "year": 2022,
                     "week": 34,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 5
                   },
                   {
                     "year": 2022,
                     "week": 35,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 16
                   },
                   {
                     "year": 2022,
                     "week": 36,
                     "codeReviewCount": 12,
                     "issueCount": 0,
                     "pullRequestCount": 16
                   },
                   {
                     "year": 2022,
                     "week": 37,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2022,
                     "week": 38,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2022,
                     "week": 39,
                     "codeReviewCount": 5,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2022,
                     "week": 40,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2022,
                     "week": 41,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2022,
                     "week": 42,
                     "codeReviewCount": 5,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2022,
                     "week": 43,
                     "codeReviewCount": 9,
                     "issueCount": 0,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2022,
                     "week": 44,
                     "codeReviewCount": 0,
                     "issueCount": 0,
                     "pullRequestCount": 3
                   },
                   {
                     "year": 2022,
                     "week": 45,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2022,
                     "week": 46,
                     "codeReviewCount": 14,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2022,
                     "week": 47,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2022,
                     "week": 48,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2022,
                     "week": 49,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2022,
                     "week": 50,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2022,
                     "week": 51,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 12
                   },
                   {
                     "year": 2022,
                     "week": 52,
                     "codeReviewCount": 5,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2023,
                     "week": 1,
                     "codeReviewCount": 9,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 2,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2023,
                     "week": 3,
                     "codeReviewCount": 13,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2023,
                     "week": 4,
                     "codeReviewCount": 18,
                     "issueCount": 0,
                     "pullRequestCount": 11
                   },
                   {
                     "year": 2023,
                     "week": 5,
                     "codeReviewCount": 19,
                     "issueCount": 0,
                     "pullRequestCount": 19
                   },
                   {
                     "year": 2023,
                     "week": 6,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 8,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2023,
                     "week": 9,
                     "codeReviewCount": 13,
                     "issueCount": 0,
                     "pullRequestCount": 14
                   },
                   {
                     "year": 2023,
                     "week": 10,
                     "codeReviewCount": 11,
                     "issueCount": 0,
                     "pullRequestCount": 20
                   },
                   {
                     "year": 2023,
                     "week": 11,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 12,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 18
                   },
                   {
                     "year": 2023,
                     "week": 13,
                     "codeReviewCount": 5,
                     "issueCount": 4,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2023,
                     "week": 14,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 7
                   },
                   {
                     "year": 2023,
                     "week": 15,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2023,
                     "week": 16,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2023,
                     "week": 17,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 18
                   },
                   {
                     "year": 2023,
                     "week": 18,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 17
                   },
                   {
                     "year": 2023,
                     "week": 19,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 20,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 14
                   },
                   {
                     "year": 2023,
                     "week": 21,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 14
                   },
                   {
                     "year": 2023,
                     "week": 22,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2023,
                     "week": 23,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 24,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 5
                   },
                   {
                     "year": 2023,
                     "week": 25,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2023,
                     "week": 26,
                     "codeReviewCount": 6,
                     "issueCount": 0,
                     "pullRequestCount": 13
                   },
                   {
                     "year": 2023,
                     "week": 27,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 28,
                     "codeReviewCount": 8,
                     "issueCount": 0,
                     "pullRequestCount": 22
                   },
                   {
                     "year": 2023,
                     "week": 29,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 5
                   },
                   {
                     "year": 2023,
                     "week": 30,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 2
                   },
                   {
                     "year": 2023,
                     "week": 31,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2023,
                     "week": 32,
                     "codeReviewCount": 7,
                     "issueCount": 0,
                     "pullRequestCount": 17
                   },
                   {
                     "year": 2023,
                     "week": 35,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 3
                   },
                   {
                     "year": 2023,
                     "week": 36,
                     "codeReviewCount": 3,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   },
                   {
                     "year": 2023,
                     "week": 37,
                     "codeReviewCount": 1,
                     "issueCount": 0,
                     "pullRequestCount": 4
                   },
                   {
                     "year": 2023,
                     "week": 38,
                     "codeReviewCount": 5,
                     "issueCount": 0,
                     "pullRequestCount": 13
                   },
                   {
                     "year": 2023,
                     "week": 39,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 16
                   },
                   {
                     "year": 2023,
                     "week": 40,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 9
                   },
                   {
                     "year": 2023,
                     "week": 41,
                     "codeReviewCount": 4,
                     "issueCount": 0,
                     "pullRequestCount": 27
                   },
                   {
                     "year": 2023,
                     "week": 42,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 13
                   },
                   {
                     "year": 2023,
                     "week": 43,
                     "codeReviewCount": 11,
                     "issueCount": 0,
                     "pullRequestCount": 8
                   },
                   {
                     "year": 2023,
                     "week": 44,
                     "codeReviewCount": 2,
                     "issueCount": 0,
                     "pullRequestCount": 10
                   },
                   {
                     "year": 2023,
                     "week": 45,
                     "codeReviewCount": 16,
                     "issueCount": 0,
                     "pullRequestCount": 25
                   },
                   {
                     "year": 2023,
                     "week": 46,
                     "codeReviewCount": 5,
                     "issueCount": 0,
                     "pullRequestCount": 21
                   },
                   {
                     "year": 2023,
                     "week": 47,
                     "codeReviewCount": 10,
                     "issueCount": 0,
                     "pullRequestCount": 25
                   },
                   {
                     "year": 2023,
                     "week": 48,
                     "codeReviewCount": 14,
                     "issueCount": 0,
                     "pullRequestCount": 21
                   },
                   {
                     "year": 2023,
                     "week": 49,
                     "codeReviewCount": 0,
                     "issueCount": 0,
                     "pullRequestCount": 6
                   }
                 ],
                 "contributionCountVariationSinceLastWeek": -29
               },
               "allocatedTimeToContribute": "NONE",
               "isLookingForAJob": false
            }
             """;

    private static final String GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE = """
            {
              "githubUserId": 43467246,
              "login": "AnthonyBuisset",
              "htmlUrl": "https://github.com/AnthonyBuisset",
              "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
              "id": "747e663f-4e68-4b42-965b-b5aebedcd4c4",
              "location": "Vence, France",
              "bio": "FullStack engineerr",
              "website": "https://linktr.ee/abuisset",
              "technologies": {
                "CSS": 4473,
                "Rust": 404344,
                "Procfile": 507,
                "PLpgSQL": 10486,
                "Makefile": 8658,
                "HTML": 435,
                "HCL": 23317,
                "TypeScript": 2030401,
                "Dockerfile": 951,
                "Shell": 1455,
                "Cairo": 42100,
                "JavaScript": 2205,
                "Nix": 12902,
                "Python": 45301
              },
              "createdAt": "2022-12-12T09:51:58.48559Z",
              "lastSeenAt": "2023-10-05T19:06:50.034Z",
              "firstContributedAt": "2022-04-13T09:00:48Z",
              "cover": "BLUE",
              "contacts": [
                {
                  "channel": "LINKEDIN",
                  "contact": "",
                  "visibility": "public"
                },
                {
                  "channel": "TWITTER",
                  "contact": "https://twitter.com/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "WHATSAPP",
                  "contact": "",
                  "visibility": "public"
                },
                {
                  "channel": "TELEGRAM",
                  "contact": "https://t.me/abuisset",
                  "visibility": "public"
                },
                {
                  "channel": "DISCORD",
                  "contact": "antho",
                  "visibility": "public"
                }
              ],
              "projects": [
                {
                  "id": "90fb751a-1137-4815-b3c4-54927a5db059",
                  "name": "No sponsors",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": null,
                  "contributorCount": 18,
                  "totalGranted": 10000,
                  "userContributionCount": 888,
                  "userLastContributedAt": "2023-10-12T08:57:23Z",
                  "slug": "no-sponsors",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "00490be6-2c03-4720-993b-aea3e07edd81",
                  "name": "Zama",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": "https://dl.airtable.com/.attachments/f776b6ea66adbe46d86adaea58626118/610d50f6/15TqNyRwTMGoVeAX2u1M",
                  "contributorCount": 18,
                  "totalGranted": 0,
                  "userContributionCount": 25,
                  "userLastContributedAt": "2022-06-01T10:34:49Z",
                  "slug": "zama",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "a852e8fd-de3c-4a14-813e-4b592af40d54",
                  "name": "OnlyDust Marketplace",
                  "isLead": true,
                  "leadSince": "2023-07-09T07:30:12.544103Z",
                  "logoUrl": null,
                  "contributorCount": 10,
                  "totalGranted": 1000,
                  "userContributionCount": null,
                  "userLastContributedAt": null,
                  "slug": "onlydust-marketplace",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "594ca5ca-48f7-49a8-9c26-84b949d4fdd9",
                  "name": "Mooooooonlight",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/1913921207486176664.jpg",
                  "contributorCount": 20,
                  "totalGranted": 4500,
                  "userContributionCount": 888,
                  "userLastContributedAt": "2023-10-12T08:57:23Z",
                  "slug": "mooooooonlight",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "dc60d963-4b5f-4a96-928c-8440b4657138",
                  "name": "Zero title 4",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": null,
                  "contributorCount": 4,
                  "totalGranted": 2314,
                  "userContributionCount": 3,
                  "userLastContributedAt": "2023-07-10T15:41:24Z",
                  "slug": "zero-title-4",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "f39b827f-df73-498c-8853-99bc3f562723",
                  "name": "QA new contributions",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": null,
                  "contributorCount": 18,
                  "totalGranted": 6000,
                  "userContributionCount": 885,
                  "userLastContributedAt": "2023-10-12T08:57:23Z",
                  "slug": "qa-new-contributions",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "57f76bd5-c6fb-4ef0-8a0a-74450f4ceca8",
                  "name": "Pizzeria Yoshi !",
                  "isLead": false,
                  "leadSince": null,
                  "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/14305950553200301786.png",
                  "contributorCount": 886,
                  "totalGranted": 20000,
                  "userContributionCount": 7,
                  "userLastContributedAt": "2023-08-30T09:20:48Z",
                  "slug": "pizzeria-yoshi-",
                  "visibility": "PUBLIC"
                },
                {
                  "id": "298a547f-ecb6-4ab2-8975-68f4e9bf7b39",
                  "name": "kaaper",
                  "isLead": true,
                  "leadSince": "2022-12-23T11:11:51.388471Z",
                  "logoUrl": null,
                  "contributorCount": 21,
                  "totalGranted": 24000,
                  "userContributionCount": 903,
                  "userLastContributedAt": "2023-10-12T08:57:23Z",
                  "slug": "kaaper",
                  "visibility": "PUBLIC"
                }
              ],
              "stats": {
                "contributedProjectCount": 8,
                "leadedProjectCount": 2,
                "totalsEarned": {
                  "totalAmount": 2692470.00,
                  "details": [
                    {
                      "totalAmount": 19500,
                      "totalDollarsEquivalent": 19500,
                      "currency": "USD"
                    },
                    {
                      "totalAmount": 1500,
                      "totalDollarsEquivalent": 2672970.00,
                      "currency": "ETH"
                    },
                    {
                      "totalAmount": 2000,
                      "totalDollarsEquivalent": 0,
                      "currency": "STARK"
                    }
                  ]
                },
                "contributionCount": 1348,
                "contributionCountPerWeeks": [
                  {
                    "year": 2022,
                    "week": 9,
                    "codeReviewCount": 0,
                    "issueCount": 0,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 10,
                    "codeReviewCount": 0,
                    "issueCount": 1,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 11,
                    "codeReviewCount": 5,
                    "issueCount": 6,
                    "pullRequestCount": 3
                  },
                  {
                    "year": 2022,
                    "week": 12,
                    "codeReviewCount": 1,
                    "issueCount": 1,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2022,
                    "week": 13,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 14,
                    "codeReviewCount": 0,
                    "issueCount": 0,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 15,
                    "codeReviewCount": 0,
                    "issueCount": 0,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2022,
                    "week": 16,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 0
                  },
                  {
                    "year": 2022,
                    "week": 17,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2022,
                    "week": 18,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 3
                  },
                  {
                    "year": 2022,
                    "week": 19,
                    "codeReviewCount": 11,
                    "issueCount": 0,
                    "pullRequestCount": 2
                  },
                  {
                    "year": 2022,
                    "week": 20,
                    "codeReviewCount": 12,
                    "issueCount": 4,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2022,
                    "week": 21,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 2
                  },
                  {
                    "year": 2022,
                    "week": 22,
                    "codeReviewCount": 1,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2022,
                    "week": 23,
                    "codeReviewCount": 0,
                    "issueCount": 8,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2022,
                    "week": 24,
                    "codeReviewCount": 3,
                    "issueCount": 5,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2022,
                    "week": 25,
                    "codeReviewCount": 7,
                    "issueCount": 3,
                    "pullRequestCount": 3
                  },
                  {
                    "year": 2022,
                    "week": 26,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 5
                  },
                  {
                    "year": 2022,
                    "week": 27,
                    "codeReviewCount": 15,
                    "issueCount": 9,
                    "pullRequestCount": 21
                  },
                  {
                    "year": 2022,
                    "week": 28,
                    "codeReviewCount": 11,
                    "issueCount": 5,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2022,
                    "week": 29,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 30,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 1
                  },
                  {
                    "year": 2022,
                    "week": 31,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 20
                  },
                  {
                    "year": 2022,
                    "week": 32,
                    "codeReviewCount": 1,
                    "issueCount": 0,
                    "pullRequestCount": 0
                  },
                  {
                    "year": 2022,
                    "week": 33,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 2
                  },
                  {
                    "year": 2022,
                    "week": 34,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 5
                  },
                  {
                    "year": 2022,
                    "week": 35,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 16
                  },
                  {
                    "year": 2022,
                    "week": 36,
                    "codeReviewCount": 12,
                    "issueCount": 0,
                    "pullRequestCount": 16
                  },
                  {
                    "year": 2022,
                    "week": 37,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2022,
                    "week": 38,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2022,
                    "week": 39,
                    "codeReviewCount": 5,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2022,
                    "week": 40,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2022,
                    "week": 41,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2022,
                    "week": 42,
                    "codeReviewCount": 5,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2022,
                    "week": 43,
                    "codeReviewCount": 9,
                    "issueCount": 0,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2022,
                    "week": 44,
                    "codeReviewCount": 0,
                    "issueCount": 0,
                    "pullRequestCount": 3
                  },
                  {
                    "year": 2022,
                    "week": 45,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2022,
                    "week": 46,
                    "codeReviewCount": 14,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2022,
                    "week": 47,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2022,
                    "week": 48,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2022,
                    "week": 49,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2022,
                    "week": 50,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2022,
                    "week": 51,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 12
                  },
                  {
                    "year": 2022,
                    "week": 52,
                    "codeReviewCount": 5,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2023,
                    "week": 1,
                    "codeReviewCount": 9,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 2,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2023,
                    "week": 3,
                    "codeReviewCount": 13,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2023,
                    "week": 4,
                    "codeReviewCount": 18,
                    "issueCount": 0,
                    "pullRequestCount": 11
                  },
                  {
                    "year": 2023,
                    "week": 5,
                    "codeReviewCount": 19,
                    "issueCount": 0,
                    "pullRequestCount": 19
                  },
                  {
                    "year": 2023,
                    "week": 6,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 8,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2023,
                    "week": 9,
                    "codeReviewCount": 13,
                    "issueCount": 0,
                    "pullRequestCount": 14
                  },
                  {
                    "year": 2023,
                    "week": 10,
                    "codeReviewCount": 11,
                    "issueCount": 0,
                    "pullRequestCount": 20
                  },
                  {
                    "year": 2023,
                    "week": 11,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 12,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 18
                  },
                  {
                    "year": 2023,
                    "week": 13,
                    "codeReviewCount": 5,
                    "issueCount": 4,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2023,
                    "week": 14,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 7
                  },
                  {
                    "year": 2023,
                    "week": 15,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2023,
                    "week": 16,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2023,
                    "week": 17,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 18
                  },
                  {
                    "year": 2023,
                    "week": 18,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 17
                  },
                  {
                    "year": 2023,
                    "week": 19,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 20,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 14
                  },
                  {
                    "year": 2023,
                    "week": 21,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 14
                  },
                  {
                    "year": 2023,
                    "week": 22,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2023,
                    "week": 23,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 24,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 5
                  },
                  {
                    "year": 2023,
                    "week": 25,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2023,
                    "week": 26,
                    "codeReviewCount": 6,
                    "issueCount": 0,
                    "pullRequestCount": 13
                  },
                  {
                    "year": 2023,
                    "week": 27,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 28,
                    "codeReviewCount": 8,
                    "issueCount": 0,
                    "pullRequestCount": 22
                  },
                  {
                    "year": 2023,
                    "week": 29,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 5
                  },
                  {
                    "year": 2023,
                    "week": 30,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 2
                  },
                  {
                    "year": 2023,
                    "week": 31,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2023,
                    "week": 32,
                    "codeReviewCount": 7,
                    "issueCount": 0,
                    "pullRequestCount": 17
                  },
                  {
                    "year": 2023,
                    "week": 35,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 3
                  },
                  {
                    "year": 2023,
                    "week": 36,
                    "codeReviewCount": 3,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  },
                  {
                    "year": 2023,
                    "week": 37,
                    "codeReviewCount": 1,
                    "issueCount": 0,
                    "pullRequestCount": 4
                  },
                  {
                    "year": 2023,
                    "week": 38,
                    "codeReviewCount": 5,
                    "issueCount": 0,
                    "pullRequestCount": 13
                  },
                  {
                    "year": 2023,
                    "week": 39,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 16
                  },
                  {
                    "year": 2023,
                    "week": 40,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 9
                  },
                  {
                    "year": 2023,
                    "week": 41,
                    "codeReviewCount": 4,
                    "issueCount": 0,
                    "pullRequestCount": 27
                  },
                  {
                    "year": 2023,
                    "week": 42,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 13
                  },
                  {
                    "year": 2023,
                    "week": 43,
                    "codeReviewCount": 11,
                    "issueCount": 0,
                    "pullRequestCount": 8
                  },
                  {
                    "year": 2023,
                    "week": 44,
                    "codeReviewCount": 2,
                    "issueCount": 0,
                    "pullRequestCount": 10
                  },
                  {
                    "year": 2023,
                    "week": 45,
                    "codeReviewCount": 16,
                    "issueCount": 0,
                    "pullRequestCount": 25
                  },
                  {
                    "year": 2023,
                    "week": 46,
                    "codeReviewCount": 5,
                    "issueCount": 0,
                    "pullRequestCount": 21
                  },
                  {
                    "year": 2023,
                    "week": 47,
                    "codeReviewCount": 10,
                    "issueCount": 0,
                    "pullRequestCount": 25
                  },
                  {
                    "year": 2023,
                    "week": 48,
                    "codeReviewCount": 14,
                    "issueCount": 0,
                    "pullRequestCount": 21
                  },
                  {
                    "year": 2023,
                    "week": 49,
                    "codeReviewCount": 0,
                    "issueCount": 0,
                    "pullRequestCount": 6
                  }
                ],
                "contributionCountVariationSinceLastWeek": -29
              }
            }
            """;

    @Autowired
    HasuraUserHelper userHelper;


    @Test
    void should_return_a_not_found_error() {
        // Given
        final long notExistingUserId = 1;

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + notExistingUserId))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(404);
    }

    @Test
    void should_get_public_user_profile() {
        // Given
        final long anthonyId = 43467246L;

        // When
        client.get()
                .uri(getApiURI(USERS_GET + "/" + anthonyId))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").doesNotExist()
                .jsonPath("$.isLookingForAJob").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.projects[?(@.visibility=='PRIVATE')]").doesNotExist()
                .json(GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE);
    }

    @Test
    void should_get_public_user_profile_by_login() {
        // Given
        final String anthonyLogin = "AnthonyBuisset";

        // When
        client.get()
                .uri(getApiURI(USERS_GET_BY_LOGIN + "/" + anthonyLogin))
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").doesNotExist()
                .jsonPath("$.isLookingForAJob").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')]").doesNotExist()
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.projects[?(@.visibility=='PRIVATE')]").doesNotExist()
                .json(GET_ANTHONY_PUBLIC_PROFILE_JSON_RESPONSE);
    }

    @Test
    void should_get_private_user_profile() {
        // Given
        final String jwt = userHelper.authenticateAnthony().jwt();

        // When
        client.get()
                .uri(getApiURI(ME_GET_PROFILE))
                .header("Authorization", BEARER_PREFIX + jwt)
                .exchange()
                // Then
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.allocatedTimeToContribute").isEqualTo("NONE")
                .jsonPath("$.isLookingForAJob").isEqualTo(false)
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].visibility").isEqualTo("private")
                .jsonPath("$.contacts[?(@.contact=='abuisset@gmail.com')].channel").isEqualTo("EMAIL")
                .jsonPath("$.contacts[?(@.contact=='antho')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='antho')].channel").isEqualTo("DISCORD")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://twitter.com/abuisset')].channel").isEqualTo("TWITTER")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].visibility").isEqualTo("public")
                .jsonPath("$.contacts[?(@.contact=='https://t.me/abuisset')].channel").isEqualTo("TELEGRAM")
                .jsonPath("$.projects[?(@.visibility=='PRIVATE')]").exists()
                .json(GET_ANTHONY_PRIVATE_PROFILE_JSON_RESPONSE);
    }
}
