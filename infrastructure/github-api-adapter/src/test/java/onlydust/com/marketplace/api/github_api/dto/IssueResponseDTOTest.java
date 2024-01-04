package onlydust.com.marketplace.api.github_api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class IssueResponseDTOTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void should_deserialize_issue() throws JsonProcessingException {
    // Given
    final String issueJson = """
        {
                              "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25",
                              "repository_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend",
                              "labels_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/labels{/name}",
                              "comments_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/comments",
                              "events_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/events",
                              "html_url": "https://github.com/onlydustxyz/marketplace-frontend/issues/25",
                              "id": 1840630179,
                              "node_id": "I_kwDOJ4YlT85ttcmj",
                              "number": 25,
                              "title": "issue-title",
                              "user": {
                                "login": "PierreOucif",
                                "id": 16590657,
                                "node_id": "MDQ6VXNlcjE2NTkwNjU3",
                                "avatar_url": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "gravatar_id": "",
                                "url": "https://api.github.com/users/PierreOucif",
                                "html_url": "https://github.com/PierreOucif",
                                "followers_url": "https://api.github.com/users/PierreOucif/followers",
                                "following_url": "https://api.github.com/users/PierreOucif/following{/other_user}",
                                "gists_url": "https://api.github.com/users/PierreOucif/gists{/gist_id}",
                                "starred_url": "https://api.github.com/users/PierreOucif/starred{/owner}{/repo}",
                                "subscriptions_url": "https://api.github.com/users/PierreOucif/subscriptions",
                                "organizations_url": "https://api.github.com/users/PierreOucif/orgs",
                                "repos_url": "https://api.github.com/users/PierreOucif/repos",
                                "events_url": "https://api.github.com/users/PierreOucif/events{/privacy}",
                                "received_events_url": "https://api.github.com/users/PierreOucif/received_events",
                                "type": "User",
                                "site_admin": false
                              },
                              "labels": [],
                              "state": "open",
                              "locked": false,
                              "assignee": null,
                              "assignees": [],
                              "milestone": null,
                              "comments": 0,
                              "created_at": "2023-08-08T06:11:35Z",
                              "updated_at": "2023-08-08T06:11:35Z",
                              "closed_at": null,
                              "author_association": "MEMBER",
                              "active_lock_reason": null,
                              "body": null,
                              "closed_by": null,
                              "reactions": {
                                "url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/reactions",
                                "total_count": 0,
                                "+1": 0,
                                "-1": 0,
                                "laugh": 0,
                                "hooray": 0,
                                "confused": 0,
                                "heart": 0,
                                "rocket": 0,
                                "eyes": 0
                              },
                              "timeline_url": "https://api.github.com/repos/onlydustxyz/marketplace-frontend/issues/25/timeline",
                              "performed_via_github_app": null,
                              "state_reason": null
                            }""";

    // When
    final IssueResponseDTO issueResponseDTO = objectMapper.readValue(issueJson, IssueResponseDTO.class);

    // Then
    assertNotNull(issueResponseDTO);
    assertEquals(25, issueResponseDTO.getNumber());
  }
}
