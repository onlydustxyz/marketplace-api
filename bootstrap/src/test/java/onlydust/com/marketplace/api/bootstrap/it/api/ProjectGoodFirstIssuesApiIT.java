package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import org.junit.jupiter.api.Test;

import java.util.Map;


@TagProject
public class ProjectGoodFirstIssuesApiIT extends AbstractMarketplaceApiIT {
    private final static String CAL_DOT_COM = "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e";

    @Test
    void should_get_good_first_issues() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5"
                )))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 3,
                          "totalItemNumber": 11,
                          "hasMore": true,
                          "nextPageIndex": 1,
                          "issues": [
                            {
                              "id": 1980935024,
                              "number": 12255,
                              "title": "[CAL-2679] Nice find. Unit testable (could be follow up)?",
                              "status": "OPEN",
                              "createdAt": "2023-11-07T09:40:41Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12255",
                              "body": "> Nice find. Unit testable (could be follow up)?\\n\\n\\\\*Originally posted by @keithwillcode in \\\\*[*https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626*](https://github.com/calcom/cal.com/pull/12194#discussion_r1380012626)\\n\\nWrite unit/integration tests for defaultResponder and defaultHandler that can ensure that it doesn't add the header again if already added.\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2679](https://linear.app/calcom/issue/CAL-2679/nice-find-unit-testable-could-be-follow-up)</sub>",
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "commentCount": 1,
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "api",
                                  "description": "area: API, enterprise API, access token, OAuth"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ]
                            },
                            {
                              "id": 1980731400,
                              "number": 12253,
                              "title": "[CAL-2678] No loader on clicking install button from apps listing page.",
                              "status": "OPEN",
                              "createdAt": "2023-11-07T07:36:17Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12253",
                              "body": "https://www.loom.com/share/f934ebd75f0343928715860d7ec39787\\n\\n<sub>[CAL-2678](https://linear.app/calcom/issue/CAL-2678/no-loader-on-clicking-install-button-from-apps-listing-page)</sub>",
                              "author": {
                                "githubUserId": 1780212,
                                "login": "hariombalhara",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/1780212?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "commentCount": 5,
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                }
                              ]
                            },
                            {
                              "id": 1979376167,
                              "number": 12244,
                              "title": "Read skippable events to calculate realistic availability",
                              "status": "OPEN",
                              "createdAt": "2023-11-06T15:01:09Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12244",
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWhen connecting packed calendars, there can be little room for availability. Being able to distinguish events that can be marked with a different color or events with optional rsvp to be skippable, may help users with little availability to better accommodate new meetings through Cal.\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAs mentioned, having the chance to read rsvp from events to calculate availability can help free up more slots for new meetings.\\r\\n",
                              "author": {
                                "githubUserId": 467258,
                                "login": "leog",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/467258?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "commentCount": 6,
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ]
                            },
                            {
                              "id": 1975516187,
                              "number": 12215,
                              "title": "[CAL-2671] Individual Insights Page has Team Insights",
                              "status": "OPEN",
                              "createdAt": "2023-11-03T06:12:34Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/12215",
                              "body": "Individual insights page has insights from from \\"most booked\\" and \\"least booked\\" members. It should just be data that pretains to them:\\n\\n![](https://uploads.linear.app/e86bf957-d82f-465e-b205-135559f4b623/e74bdb02-5e26-41e8-87be-8dda7e382fd4/f48ddf61-24ba-4a09-b94c-ed1aa2b50df4?signature=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwYXRoIjoiL2U4NmJmOTU3LWQ4MmYtNDY1ZS1iMjA1LTEzNTU1OWY0YjYyMy9lNzRiZGIwMi01ZTI2LTQxZTgtODdiZS04ZGRhN2UzODJmZDQvZjQ4ZGRmNjEtMjRiYS00YTA5LWI5NGMtZWQxYWEyYjUwZGY0IiwiaWF0IjoxNjk4OTkxOTYyLCJleHAiOjE2OTkwNzgzNjJ9.dSRqQBHSC2TupIr9T7wnYSh8Vm3x1KlTsSEo1NIejwI)\\n\\n<sub>From [SyncLinear.com](https://synclinear.com) | [CAL-2671](https://linear.app/calcom/issue/CAL-2671/individual-insights-page-has-team-insights)</sub>",
                              "author": {
                                "githubUserId": 16177678,
                                "login": "shirazdole",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16177678?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "commentCount": 5,
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "good first issue",
                                  "description": null
                                },
                                {
                                  "name": "insights",
                                  "description": "area: insights, analytics"
                                },
                                {
                                  "name": "\\uD83D\\uDC1B bug",
                                  "description": "Something isn't working"
                                }
                              ]
                            },
                            {
                              "id": 1943730312,
                              "number": 11900,
                              "title": "Conditional Questions on Event Types",
                              "status": "OPEN",
                              "createdAt": "2023-10-15T05:42:44Z",
                              "closedAt": null,
                              "htmlUrl": "https://github.com/calcom/cal.com/issues/11900",
                              "body": "### Is your proposal related to a problem?\\r\\n\\r\\nWe would like to have the ability to ask \\"follow-up questions\\" based on the answers to previous questions in the booking request.  For example, we could have a mandatory question that asks \\"how did you hear about us?\\" with possible answers \\"web\\", \\"print\\", \\"social media\\", \\"personal reference\\" - and If they choose \\"web\\", we'd like to ask \\"source web site\\" (a text field), or if they choose \\"social media\\", we'd like to ask \\"social media site\\" (a drop down with entries for X/tweeter, Mastodon, Facebook, other).\\r\\n\\r\\n### Describe the solution you'd like\\r\\n\\r\\nAdd an option question type, which in addition to the existing question template, asks for the parent question identifier, and a value (or a list of values) that would make this question appear below the parent question.\\r\\n\\r\\n### Describe alternatives you've considered\\r\\n\\r\\nWe haven't come up with an alternative we liked.\\r\\n\\r\\n### Additional context\\r\\n\\r\\nDid not find this already suggested in either \\"Issues\\" or \\"Pull Requests\\"\\r\\n\\r\\n### Requirement/Document\\r\\n\\r\\nWe haven't written one up.\\r\\n",
                              "author": {
                                "githubUserId": 10899980,
                                "login": "moilejter",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/10899980?v=4",
                                "isRegistered": false
                              },
                              "repository": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "commentCount": 9,
                              "labels": [
                                {
                                  "name": "Low priority",
                                  "description": "Created by Linear-GitHub Sync"
                                },
                                {
                                  "name": "booking-page",
                                  "description": "area: booking page, public booking page, booker"
                                },
                                {
                                  "name": "✅ good first issue",
                                  "description": "Good for newcomers"
                                },
                                {
                                  "name": "✨ feature",
                                  "description": "New feature or request"
                                }
                              ]
                            }
                          ]
                        }
                        """);
    }
}
