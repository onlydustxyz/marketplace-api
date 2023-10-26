package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.CreateProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"hasura_auth"})
public class ProjectCreateUpdateIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper userHelper;

    @Test
    public void should_create_and_update_a_new_project() throws JsonProcessingException {
        final String jwt = userHelper.authenticatePierre().jwt();

        // When
        final var response = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project",
                          "shortDescription": "This is a super project",
                          "longDescription": "This is a super awesome project with a nice description",
                          "moreInfo": [
                            {
                              "url": "https://t.me/foobar",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": true,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505, 43467246
                          ],
                          "githubRepoIds": [
                            498695724, 602953043
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CreateProjectResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isNotNull();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + response.getProjectId()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(response.getProjectId().toString())
                .jsonPath("$.name").isEqualTo("Super Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super project")
                .jsonPath("$.hiring").isEqualTo(true)
                .jsonPath("$.leaders.length()").isEqualTo(0)
                .jsonPath("$.invitedLeaders.length()").isEqualTo(2)
                .jsonPath("$.invitedLeaders[0].login").isEqualTo("ofux")
                .jsonPath("$.invitedLeaders[1].login").isEqualTo("AnthonyBuisset")
                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.repos[1].name").isEqualTo("cool-repo-A")
                .jsonPath("$.organizations.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.organizations[1].login").isEqualTo("od-mocks")
                .jsonPath("$.organizations[1].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[1].repos[0].name").isEqualTo("cool-repo-A")
                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isEqualTo(null);
    }

    private void should_update_project(String jwt, UUID projectId) {
        // Given

        // When
        final var response = client.post()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfo": [
                            {
                              "url": "https://t.me/foobar-updated",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": false,
                          "inviteGithubUserIdsAsProjectLeads": [
                            16590657
                          ],
                          "githubRepoIds": [
                            498695724, 698096830
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CreateProjectResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isNotNull();

        // When
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + response.getProjectId()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(response.getProjectId().toString())
                .jsonPath("$.name").isEqualTo("Updated Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super updated project")
                .jsonPath("$.hiring").isEqualTo(false)
                .jsonPath("$.leaders.length()").isEqualTo(0)
                .jsonPath("$.invitedLeaders.length()").isEqualTo(1)
                .jsonPath("$.invitedLeaders[0].login").isEqualTo("pierre")
                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.repos[1].name").isEqualTo("marketplace-frontend");
    }
}
