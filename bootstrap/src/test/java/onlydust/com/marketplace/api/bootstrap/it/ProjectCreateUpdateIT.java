package onlydust.com.marketplace.api.bootstrap.it;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.CreateProjectResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCreateUpdateIT extends AbstractMarketplaceApiIT {

    private static UUID projectId;
    @Autowired
    HasuraUserHelper userHelper;
    private String jwt;

    @BeforeEach
    public void setup() {
        jwt = userHelper.authenticatePierre().jwt();
    }

    @Test
    @Order(1)
    public void should_create_a_new_project() {
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
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
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
                .jsonPath("$.visibility").isEqualTo("PUBLIC")
                .jsonPath("$.name").isEqualTo("Super Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super project")
                .jsonPath("$.longDescription").isEqualTo("This is a super awesome project with a nice description")
                .jsonPath("$.logoUrl").isEqualTo("https://avatars.githubusercontent.com/u/16590657?v=4")
                .jsonPath("$.hiring").isEqualTo(true)
                .jsonPath("$.moreInfoUrl").isEqualTo("https://t.me/foobar")
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

        projectId = response.getProjectId();
    }

    @Test
    @Order(2)
    public void should_update_the_project() {

        // And When
        client.put()
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
                          "projectLeads": [
                            "e461c019-ba23-4671-9b6c-3a5a18748af9",
                            "f20e6812-8de8-432b-9c31-2920434fe7d0"
                          ],
                          "githubRepoIds": [
                            498695724, 452047076
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": true,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // And Then
        assertProjectWasUpdated();
    }

    @Test
    @Order(3)
    public void should_update_lists_only_when_present() {

        // And When
        client.put()
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
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": true,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // And Then
        assertProjectWasUpdated();
    }

    @Test
    @Order(4)
    public void should_update_reward_settings_only_when_present() {

        // And When
        client.put()
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
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // And Then
        assertProjectWasUpdated();
    }

    @Test
    @Order(5)
    public void should_update_revoke_leader_when_not_present_in_the_list() {

        // And When
        client.put()
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
                          "projectLeads": [
                            "f20e6812-8de8-432b-9c31-2920434fe7d0"
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": true,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // And Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.leaders.length()").isEqualTo(1)
                .jsonPath("$.leaders[0].login").isEqualTo("pacovilletard");

        // And When
        client.put()
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
                          "projectLeads": [],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": true,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // And Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.leaders.length()").isEqualTo(0);
    }


    @Test
    @Order(6)
    public void should_return_a_400_when_input_is_invalid() {
        // When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "moreInfo": [
                            {
                              "url": "https://t.me/foobar-updated",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": false,
                          "projectLeads": [],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": true,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @Order(7)
    public void should_return_a_404_when_project_is_not_found() {
        // When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, UUID.randomUUID())))
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
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isNotFound();
    }

    private void assertProjectWasUpdated() {
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.visibility").isEqualTo("PUBLIC")
                .jsonPath("$.name").isEqualTo("Updated Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super updated project")
                .jsonPath("$.longDescription").isEqualTo("This is a super awesome updated project with a nice " +
                                                         "description")
                .jsonPath("$.logoUrl").isEqualTo("https://avatars.githubusercontent.com/u/yyyyyyyyyyyy")
                .jsonPath("$.hiring").isEqualTo(false)
                .jsonPath("$.moreInfoUrl").isEqualTo("https://t.me/foobar-updated")

                .jsonPath("$.leaders.length()").isEqualTo(2)
                .jsonPath("$.leaders[0].login").isEqualTo("ofux")
                .jsonPath("$.leaders[1].login").isEqualTo("pacovilletard")

                .jsonPath("$.invitedLeaders.length()").isEqualTo(1)
                .jsonPath("$.invitedLeaders[0].login").isEqualTo("PierreOucif")

                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.repos[1].name").isEqualTo("bretzel-site")
                .jsonPath("$.organizations.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.organizations[1].login").isEqualTo("gregcha")
                .jsonPath("$.organizations[1].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[1].repos[0].name").isEqualTo("bretzel-site")

                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isEqualTo("2021-01-01T00:00:00Z");
    }
}
