package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ApplicationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@TagProject
public class ProjectGoodFirstIssuesApiIT extends AbstractMarketplaceApiIT {
    private final static UUID CAL_DOT_COM = UUID.fromString("1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e");

    @Autowired
    private ApplicationRepository applicationRepository;

    private UserAuthHelper.AuthenticatedUser antho;

    @BeforeEach
    void setUp() {
        antho = userAuthHelper.authenticateAnthony();
        final var pierre = userAuthHelper.authenticatePierre();
        final var ofux = userAuthHelper.authenticateOlivier();

        applicationRepository.saveAll(List.of(
                new ApplicationEntity(
                        UUID.fromString("f3706f53-bd79-4991-8a76-7dd12aef81dd"),
                        ZonedDateTime.of(2023, 11, 5, 9, 40, 41, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        pierre.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1980935024L,
                        1111L,
                        "I would like to work on this issue",
                        "I would do this and that"),
                new ApplicationEntity(
                        UUID.fromString("609231c0-b38c-4d5c-b21d-6307595f520f"),
                        ZonedDateTime.of(2023, 11, 7, 15, 26, 35, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        ofux.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1980935024L,
                        1112L,
                        "I am very interested!",
                        null),
                new ApplicationEntity(
                        UUID.fromString("bf6a909e-243d-4698-aa9f-5f40e3fb4826"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        CAL_DOT_COM,
                        antho.user().getGithubUserId(),
                        Application.Origin.MARKETPLACE,
                        1980935024L,
                        1113L,
                        "I could do it",
                        "No idea yet ¯\\_(ツ)_/¯"),
                new ApplicationEntity(
                        UUID.fromString("536532eb-ed7b-4461-884d-20e54ba9bec6"),
                        ZonedDateTime.of(2023, 11, 7, 20, 2, 11, 0, ZoneOffset.UTC),
                        UUID.fromString("97f6b849-1545-4064-83f1-bc5ded33a8b3"),
                        antho.user().getGithubUserId(),
                        Application.Origin.GITHUB,
                        1980935024L,
                        1113L,
                        "I could do it",
                        null)
        ));
    }

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
                              ],
                              "applicants": [
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                                },
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                }
                              ],
                              "currentUserApplication": null,
                              "languages": [
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                                  "slug": "go",
                                  "name": "Go",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                                },
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
                              ],
                              "applicants": [],
                              "currentUserApplication": null,
                              "languages": [
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                                  "slug": "go",
                                  "name": "Go",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                                },
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
                              ],
                              "applicants": [],
                              "currentUserApplication": null,
                              "languages": [
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                                  "slug": "go",
                                  "name": "Go",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                                },
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
                              ],
                              "applicants": [],
                              "currentUserApplication": null,
                              "languages": [
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                                  "slug": "go",
                                  "name": "Go",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                                },
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
                              ],
                              "applicants": [],
                              "currentUserApplication": null,
                              "languages": [
                                {
                                  "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                                  "slug": "python",
                                  "name": "Python",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                                },
                                {
                                  "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                                  "slug": "go",
                                  "name": "Go",
                                  "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                                  "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                                },
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
                              ]
                            }
                          ]
                        }
                        """);
    }

    @Test
    void should_get_good_first_issues_as_registered_user() {
        // When
        client.get()
                .uri(getApiURI(PROJECT_GOOD_FIRST_ISSUES.formatted(CAL_DOT_COM), Map.of(
                        "pageIndex", "0",
                        "pageSize", "5"
                )))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "issues": [
                            {
                              "id": 1980935024,
                              "applicants": [
                                {
                                  "githubUserId": 595505,
                                  "login": "ofux",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp"
                                },
                                {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp"
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                                }
                              ],
                              "currentUserApplication": {
                                "id": "bf6a909e-243d-4698-aa9f-5f40e3fb4826",
                                "applicant": {
                                  "githubUserId": 43467246,
                                  "login": "AnthonyBuisset",
                                  "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/11725380531262934574.webp",
                                  "isRegistered": true
                                },
                                "motivations": "I could do it",
                                "problemSolvingApproach": "No idea yet ¯\\\\_(ツ)_/¯"
                              }
                            },
                            {
                              "id": 1980731400
                            },
                            {
                              "id": 1979376167
                            },
                            {
                              "id": 1975516187
                            },
                            {
                              "id": 1943730312
                            }
                          ]
                        }
                        """);
    }
}
