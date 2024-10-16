package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagProject;
import org.junit.jupiter.api.Test;

import java.util.Map;


@TagProject
public class ContributionsApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_get_contributions() {
        // When
        client.get()
                .uri(getApiURI(CONTRIBUTIONS, Map.of("pageSize", "1")))
                // Then
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "contributions": [
                            {
                              "type": "CODE_REVIEW",
                              "repo": {
                                "id": 350360184,
                                "owner": "calcom",
                                "name": "cal.com",
                                "description": "Scheduling infrastructure for absolutely everyone.",
                                "htmlUrl": "https://github.com/calcom/cal.com"
                              },
                              "githubAuthor": {
                                "githubUserId": 22683064,
                                "login": "shivamklr",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/22683064?v=4"
                              },
                              "githubId": 1503740037,
                              "githubNumber": 11166,
                              "githubStatus": "PENDING",
                              "githubTitle": "chore: add Wizard in storybook (CALCOM-10760)",
                              "githubHtmlUrl": "https://github.com/calcom/cal.com/pull/11166",
                              "githubBody": "Here is the GitStart Ticket for this pull request: [CALCOM-10760](https://developers.gitstart.com/client/calcom/ticket/CALCOM-10760)\\r\\n\\r\\n- Please mark your PR as \\"ready for review\\" when you have completed all requirements.\\r\\n- Please ensure that your PR is reviewed by at least 2 people after you have completed it.\\r\\n- Comments for review or requested changes from the upstream client will be notified to you by email and will also show on the [ticket page](https://developers.gitstart.com/client/calcom/ticket/CALCOM-10760).\\r\\n- After the PR is merged on the upstream, please merge this PR.\\r\\n- If there exist merge conflicts on your current PR and you have to close the PR instead of merging it, please go to the [ticket page](https://developers.gitstart.com/client/calcom/ticket/CALCOM-10760) to manually finish the task that is related to this PR.\\r\\n\\r\\nHappy hacking! \\uD83C\\uDF89\\r\\n\\r\\n## DEMO\\r\\n\\r\\nhttps://www.loom.com/share/29c39c63054b40be880f1ea34ce35ff9?sid=71d9de65-b996-4649-b245-136d8dc7b7bd\\r\\n\\r\\nWe added an explanation about why we can't use the next button to go to another step:\\r\\n\\r\\n![image](https://github.com/calcom/cal.com/assets/121884634/24c354a3-305b-44e4-996d-7b4276e94643)\\r\\n\\r\\nWe already have a storybook file for steps component so we can test it there",
                              "githubLabels": null,
                              "lastUpdatedAt": "2023-09-06T00:03:24Z",
                              "id": "00002090335968f5e3c802d4fca91e48c621e3a34ba03e24ca440721d52ba5a2",
                              "createdAt": "2023-09-06T02:03:24Z",
                              "completedAt": "2023-09-06T15:22:03Z",
                              "activityStatus": "DONE",
                              "project": {
                                "id": "1bdddf7d-46e1-4a3f-b8a3-85e85a6df59e",
                                "slug": "calcom",
                                "name": "Cal.com",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5271998260751715005.png"
                              },
                              "contributors": [
                                {
                                  "githubUserId": 22683064,
                                  "login": "shivamklr",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/22683064?v=4"
                                }
                              ],
                              "applicants": null,
                              "languages": null,
                              "linkedIssues": null,
                              "totalRewardedUsdAmount": null
                            }
                          ]
                        }
                        """);
    }
}
