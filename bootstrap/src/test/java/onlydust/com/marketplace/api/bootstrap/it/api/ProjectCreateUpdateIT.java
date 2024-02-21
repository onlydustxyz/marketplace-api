package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.CreateProjectResponse;
import onlydust.com.marketplace.api.contract.model.OnlyDustError;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCreateUpdateIT extends AbstractMarketplaceApiIT {

    private static UUID projectId;


    @Autowired
    EventRepository eventRepository;

    @BeforeEach
    public void setup() {
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/43467246"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/16590657"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/5160414"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/777"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(500)));
    }

    @SneakyThrows
    @Test
    @Order(1)
    public void should_create_a_new_project() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [498695724, 602953043],
                          "unlinkedRepoIds": []
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));

        // When

        final var response = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project",
                          "shortDescription": "This is a super project",
                          "longDescription": "This is a super awesome project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar",
                              "value": "foobar"
                            }
                          ],
                          "isLookingForContributors": true,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505, 43467246, 5160414
                          ],
                          "githubRepoIds": [
                            498695724, 602953043
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                          "ecosystemIds" : ["b599313c-a074-440f-af04-a466529ab2e7","99b6c284-f9bb-4f89-8ce7-03771465ef8e"]
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
        projectId = response.getProjectId();

        assertThat(response.getProjectSlug()).isEqualTo("super-project");

        // Then
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
                .jsonPath("$.moreInfos[0].url").isEqualTo("https://t.me/foobar")
                .jsonPath("$.moreInfos[0].value").isEqualTo("foobar")
                .jsonPath("$.moreInfos.length()").isEqualTo(1)
                .jsonPath("$.leaders.length()").isEqualTo(1)
                .jsonPath("$.leaders[0].login").isEqualTo("PierreOucif")
                .jsonPath("$.invitedLeaders.length()").isEqualTo(3)
                .jsonPath("$.invitedLeaders[0].login").isEqualTo("ofux")
                .jsonPath("$.invitedLeaders[1].login").isEqualTo("haydencleary")
                .jsonPath("$.invitedLeaders[2].login").isEqualTo("AnthonyBuisset")
                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[0].id").isEqualTo(498695724)
                .jsonPath("$.repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.repos[1].id").isEqualTo(602953043)
                .jsonPath("$.repos[1].name").isEqualTo("cool-repo-A")
                .jsonPath("$.organizations.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[0].installationId").isEqualTo(44741576)
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].repos[0].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.organizations[0].repos[0].description").isEqualTo("Contributions marketplace backend " +
                                                                               "services")
                .jsonPath("$.organizations[1].login").isEqualTo("od-mocks")
                .jsonPath("$.organizations[1].installationId").isEqualTo(null)
                .jsonPath("$.organizations[1].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[1].repos[0].name").isEqualTo("cool-repo-A")
                .jsonPath("$.organizations[1].repos[0].description").isEqualTo("This is repo A for our e2e tests")
                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isNotEmpty()
                .jsonPath("$.ecosystems.length()").isEqualTo(2)
                .jsonPath("$.ecosystems[0].name").isEqualTo("Starknet")
                .jsonPath("$.ecosystems[1].name").isEqualTo("Zama");

        final var events = eventRepository.findAll();
        final var event = events.get(events.size() - 1);
        assertThat(event.getIndex()).isNotNull();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getAggregateId()).isEqualTo(projectId);
        assertThat(event.getAggregateName()).isEqualTo("PROJECT");
        assertThat(event.getPayload()).isEqualTo("{\"Created\": {\"id\": \"%s\"}}".formatted(response.getProjectId()));

        runJobs();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("Created")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderAssigned")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.leader_id",
                        equalTo("fc92397c-3431-4a84-8054-845376b630a0"))) // PierreOucif
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderInvited")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.github_user_id", equalTo("595505")))
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderInvited")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.github_user_id", equalTo("43467246")))
        );
    }

    @SneakyThrows
    @Test
    @Order(2)
    public void accept_leader_invitation_for_next_tests() {
        client.put()
                .uri(getApiURI(format(ME_ACCEPT_PROJECT_LEADER_INVITATION, projectId)))
                .header("Authorization", BEARER_PREFIX + userAuthHelper.authenticateOlivier().jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath(format("$.leaders[?(@.githubUserId==%d)]", 595505L)).exists()
                .jsonPath(format("$.leaders[?(@.githubUserId==%d)]", 16590657L)).exists();

        runJobs();
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderAssigned")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.leader_id",
                        equalTo("e461c019-ba23-4671-9b6c-3a5a18748af9"))) // ofux
        );
    }

    @Test
    @Order(3)
    public void should_fail_to_update_the_project_when_indexer_responds_with_500() {

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "isLookingForContributors": false,
                          "inviteGithubUserIdsAsProjectLeads": [
                            777
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is5xxServerError();
    }

    @SneakyThrows
    @Test
    @Order(10)
    public void should_update_the_project() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [452047076],
                          "unlinkedRepoIds": [602953043]
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar/updated",
                              "value": "foobar-updated"
                            },
                            {
                              "url": "https://foobar.com",
                              "value": "foobar-updated2"
                            }
                          ],
                          "isLookingForContributors": false,
                          "inviteGithubUserIdsAsProjectLeads": [
                            16590657, 43467246
                          ],
                          "projectLeadsToKeep": [
                            "e461c019-ba23-4671-9b6c-3a5a18748af9"
                          ],
                          "githubRepoIds": [
                            498695724, 452047076
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          },
                          "ecosystemIds": ["99b6c284-f9bb-4f89-8ce7-03771465ef8e","6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectId").isEqualTo(projectId.toString())
                .jsonPath("$.projectSlug").isEqualTo("updated-project");

        // And Then
        assertProjectWasUpdated();

        runJobs();
        indexerApiWireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "linkedRepoIds": [452047076],
                          "unlinkedRepoIds": [602953043]
                        }
                        """))
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("Updated")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderUnassigned")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.leader_id",
                        equalTo("fc92397c-3431-4a84-8054-845376b630a0"))) // PierreOucif
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderInvited")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.github_user_id", equalTo("16590657")))
        );
        webhookWireMockServer.verify(1, postRequestedFor(urlEqualTo("/"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.aggregate_name", equalTo("Project")))
                .withRequestBody(matchingJsonPath("$.event_name", equalTo("LeaderInvitationCancelled")))
                .withRequestBody(matchingJsonPath("$.environment", equalTo("local-it")))
                .withRequestBody(matchingJsonPath("$.payload.id", equalTo(projectId.toString())))
                .withRequestBody(matchingJsonPath("$.payload.github_user_id", equalTo("5160414")))
        );
    }

    private void runJobs() {
        notificationOutboxJob.run();
        indexerOutboxJob.run();
    }

    @Test
    @Order(11)
    public void should_update_lists_only_when_present() {

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "isLookingForContributors": false,
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectId").isEqualTo(projectId.toString())
                .jsonPath("$.projectSlug").isEqualTo("updated-project");

        // And Then
        assertProjectWasUpdated();
    }

    @Test
    @Order(12)
    public void should_update_reward_settings_only_when_present() {

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "isLookingForContributors": false,
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectId").isEqualTo(projectId.toString())
                .jsonPath("$.projectSlug").isEqualTo("updated-project");

        // And Then
        assertProjectWasUpdated();
    }

    @Test
    @Order(13)
    public void should_update_and_preserve_order_of_more_infos() {

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://foobar.com",
                              "value": "foobar-updated2"
                            },
                            {
                              "url": "https://t.me/foobar/updated",
                              "value": "foobar-updated"
                            },
                            {
                              "url": "https://yolo.croute",
                              "value": "yolo-croute"
                            }
                          ],
                          "isLookingForContributors": false,
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectId").isEqualTo(projectId.toString())
                .jsonPath("$.projectSlug").isEqualTo("updated-project");

        // And Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.moreInfos.length()").isEqualTo(3)
                .jsonPath("$.moreInfos[0].url").isEqualTo("https://foobar.com")
                .jsonPath("$.moreInfos[0].value").isEqualTo("foobar-updated2")
                .jsonPath("$.moreInfos[1].url").isEqualTo("https://t.me/foobar/updated")
                .jsonPath("$.moreInfos[1].value").isEqualTo("foobar-updated")
                .jsonPath("$.moreInfos[2].url").isEqualTo("https://yolo.croute")
                .jsonPath("$.moreInfos[2].value").isEqualTo("yolo-croute");

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfos": [],
                          "isLookingForContributors": false,
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.projectId").isEqualTo(projectId.toString())
                .jsonPath("$.projectSlug").isEqualTo("updated-project");

        // And Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.moreInfos").isEmpty();
    }

    @Test
    @Order(20)
    public void should_return_400_when_trying_to_add_a_leader_directly() {

        // And When
        final var response = client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar/updated",
                              "value": "foobar-updated"
                            }
                          ],
                          "isLookingForContributors": false,
                          "projectLeadsToKeep": [
                            "f20e6812-8de8-432b-9c31-2920434fe7d0"
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest()
                .expectBody(OnlyDustError.class)
                .returnResult().getResponseBody();

        assertThat(response.getMessage()).contains("Project leaders to keep must be a subset of current project " +
                                                   "leaders");
    }


    @Test
    @Order(21)
    public void should_return_a_400_when_input_is_invalid() {
        // When
        final OnlyDustError response = client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar/updated",
                              "value": "foobar-updated"
                            }
                          ],
                          "isLookingForContributors": false,
                          "projectLeadsToKeep": [],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          }
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest()
                .expectBody(OnlyDustError.class)
                .returnResult().getResponseBody();

        assertThat(response.getMessage()).contains("name: must not be null");
        assertThat(response.getMessage()).contains("shortDescription: must not be null");
        assertThat(response.getMessage()).contains("longDescription: must not be null");
    }


    @Test
    @Order(23)
    public void should_return_403_when_caller_is_not_lead() {

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateAnthony().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "This is a super updated project",
                          "longDescription": "This is a super awesome updated project with a nice description",
                          "moreInfos": [
                            {
                              "url": "https://t.me/foobar/updated",
                              "value": "foobar-updated"
                            }
                          ],
                          "isLookingForContributors": false,
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(30)
    void should_create_project_without_more_infos() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [498695724, 602953043],
                          "unlinkedRepoIds": []
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));

        // When
        final CreateProjectResponse responseBody = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Project 2",
                          "shortDescription": "This is a super project 2",
                          "longDescription": "This is a super awesome project with a nice description 2",
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

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + responseBody.getProjectId()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.moreInfos").isEmpty();
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
                .jsonPath("$.slug").isEqualTo("updated-project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super updated project")
                .jsonPath("$.longDescription").isEqualTo("This is a super awesome updated project with a nice " +
                                                         "description")
                .jsonPath("$.logoUrl").isEqualTo("https://avatars.githubusercontent.com/u/yyyyyyyyyyyy")
                .jsonPath("$.hiring").isEqualTo(false)
                .jsonPath("$.moreInfos.length()").isEqualTo(2)
                .jsonPath("$.moreInfos[0].url").isEqualTo("https://t.me/foobar/updated")
                .jsonPath("$.moreInfos[0].value").isEqualTo("foobar-updated")
                .jsonPath("$.moreInfos[1].url").isEqualTo("https://foobar.com")
                .jsonPath("$.moreInfos[1].value").isEqualTo("foobar-updated2")

                .jsonPath("$.leaders.length()").isEqualTo(1)
                .jsonPath("$.leaders[0].login").isEqualTo("ofux")

                .jsonPath("$.invitedLeaders.length()").isEqualTo(2)
                .jsonPath("$.invitedLeaders[0].login").isEqualTo("PierreOucif")
                .jsonPath("$.invitedLeaders[1].login").isEqualTo("AnthonyBuisset")

                .jsonPath("$.repos.length()").isEqualTo(2)
                .jsonPath("$.repos[0].name").isEqualTo("bretzel-site")
                .jsonPath("$.repos[1].name").isEqualTo("marketplace-frontend")
                .jsonPath("$.organizations.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].login").isEqualTo("gregcha")
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].repos[0].name").isEqualTo("bretzel-site")
                .jsonPath("$.organizations[1].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[1].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[1].repos[0].name").isEqualTo("marketplace-frontend")

                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isEqualTo("2021-01-01T00:00:00Z")

                .jsonPath("$.ecosystems[0].name").isEqualTo("Ethereum")
                .jsonPath("$.ecosystems[1].name").isEqualTo("Starknet");
    }
}
