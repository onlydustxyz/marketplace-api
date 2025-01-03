package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ContributorProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributorProjectContributorLabelRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectCategory;
import onlydust.com.marketplace.project.domain.port.input.ProjectCategoryFacadePort;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCreateUpdateIT extends AbstractMarketplaceApiIT {

    private static final AtomicBoolean setupDone = new AtomicBoolean();
    private static UUID projectId;
    private static UUID projectId2;
    private static ProjectCategory gameCategory;
    private static ProjectCategory tutorialCategory;
    private static ProjectCategory cryptoCategory;
    private static GithubRepo repo1;
    private static GithubRepo repo2;
    private static GithubRepo repo3;
    private static UUID label1Id;


    @Autowired
    ContributorProjectContributorLabelRepository contributorProjectContributorLabelRepository;

    @Autowired
    private ProjectCategoryFacadePort projectCategoryFacadePort;

    @Autowired
    private SlackApiAdapter slackApiAdapter;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    public void setup() {
        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/595505?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/16590657?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/5160414?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        indexerApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/v1/users/777?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(500)));

        if (setupDone.compareAndExchange(false, true)) return;

        repo1 = githubHelper.createRepo("repo1", "onlydustxyz");
        repo2 = githubHelper.createRepo("repo2", "od-mocks");
        repo3 = githubHelper.createRepo("repo3", "onlydustxyz");
        gameCategory = projectCategoryFacadePort.createCategory("Game", "Games are fun", "game", null);
        tutorialCategory = projectCategoryFacadePort.createCategory("Tutorial", "I love learning", "tuto", null);
        cryptoCategory = projectCategoryFacadePort.createCategory("Crypto", "Crypto is cool", "crypto", null);
    }

    @SneakyThrows
    @Test
    @Order(1)
    public void should_create_a_new_project() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [%d, %d],
                          "unlinkedRepoIds": []
                        }
                        """.formatted(repo1.getId(), repo2.getId()), true, false))
                .willReturn(WireMock.noContent()));
        final var user = userAuthHelper.authenticatePierre();

        // When
        final var response = client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.jwt())
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
                            %d, %d
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                          "ecosystemIds" : ["b599313c-a074-440f-af04-a466529ab2e7","99b6c284-f9bb-4f89-8ce7-03771465ef8e"],
                          "categoryIds": ["%s", "%s"],
                          "categorySuggestions": ["finance"],
                          "contributorLabels": [{"name": "l1"}, {"name": "l2"}]
                        }
                        """.formatted(repo1.getId(), repo2.getId(), gameCategory.id(), tutorialCategory.id()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CreateProjectResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isNotNull();
        projectId = response.getProjectId();
        runJobs();

        assertThat(response.getProjectSlug()).isEqualTo("super-project");

        MutableObject<String> l1 = new MutableObject<>();
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
                .jsonPath("$.organizations.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[0].installationId").isEqualTo(44741576)
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].repos[0].name").isEqualTo("repo1")
                .jsonPath("$.organizations[0].repos[0].description").isNotEmpty()
                .jsonPath("$.organizations[1].login").isEqualTo("od-mocks")
                .jsonPath("$.organizations[1].installationId").isEqualTo(null)
                .jsonPath("$.organizations[1].repos.length()").isEqualTo(1)
                .jsonPath("$.organizations[1].repos[0].name").isEqualTo("repo2")
                .jsonPath("$.organizations[1].repos[0].description").isNotEmpty()
                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isNotEmpty()
                .jsonPath("$.ecosystems.length()").isEqualTo(2)
                .jsonPath("$.ecosystems[0].name").isEqualTo("Starknet")
                .jsonPath("$.ecosystems[1].name").isEqualTo("Zama")
                .jsonPath("$.categories[0].name").isEqualTo("Game")
                .jsonPath("$.categories[1].name").isEqualTo("Tutorial")
                .jsonPath("$.categorySuggestions[0]").isEqualTo("finance")
                .jsonPath("$.contributorLabels[0].name").isEqualTo("l1")
                .jsonPath("$.contributorLabels[0].id").value(l1::setValue)
                .jsonPath("$.contributorLabels[1].name").isEqualTo("l2")
        ;

        verify(slackApiAdapter).onProjectCategorySuggested("finance", user.userId());

        final var entity = projectRepository.findById(projectId).orElseThrow();
        assertThat(entity.isBotNotifyExternalApplications()).isTrue();
        label1Id = UUID.fromString(l1.getValue());
    }

    @SneakyThrows
    @Test
    @Order(2)
    public void should_fail_to_create_a_new_project_when_repos_belong_to_other_projects() {
        // Given
        final var user = userAuthHelper.authenticatePierre();

        // When
        client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Another super Project with same repo",
                          "shortDescription": "This is a super project",
                          "longDescription": "This is a super awesome project with a nice description",
                          "isLookingForContributors": true,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505
                          ],
                          "githubRepoIds": [
                            498695724
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot link repos ([498695724]) because they are already linked to other projects");
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
    @Order(8)
    public void should_fail_to_update_the_project_when_repos_belong_to_other_projects() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [452047076],
                          "unlinkedRepoIds": [602953043]
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/16590657?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        final var user = userAuthHelper.authenticatePierre();

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.jwt())
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
                          "ecosystemIds": ["99b6c284-f9bb-4f89-8ce7-03771465ef8e","6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"],
                          "categoryIds": ["%s"],
                          "categorySuggestions": ["defi"]
                        }
                        """.formatted(cryptoCategory.id()))
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cannot link repos ([452047076, 498695724]) because they are already linked to other projects");
    }

    @SneakyThrows
    @Test
    @Order(9)
    public void should_fail_to_update_the_project_when_label_ids_are_missing() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [452047076],
                          "unlinkedRepoIds": [602953043]
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/16590657?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        final var user = userAuthHelper.authenticatePierre();

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.jwt())
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
                            %d, %d
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          },
                          "ecosystemIds": ["99b6c284-f9bb-4f89-8ce7-03771465ef8e","6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"],
                          "categoryIds": ["%s"],
                          "categorySuggestions": ["defi"],
                          "contributorLabels": [{"name": "l1"}, {"name": "l2"}]
                        }
                        """.formatted(repo1.getId(), repo3.getId(), cryptoCategory.id()))
                .exchange()
                // Then
                .expectStatus()
                .isBadRequest();
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
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/16590657?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        final var user = userAuthHelper.authenticatePierre();

        // And When
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.jwt())
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
                            %d, %d
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/yyyyyyyyyyyy",
                          "rewardSettings": {
                            "ignorePullRequests": false,
                            "ignoreIssues": true,
                            "ignoreCodeReviews": true,
                            "ignoreContributionsBefore": "2021-01-01T00:00:00Z"
                          },
                          "ecosystemIds": ["99b6c284-f9bb-4f89-8ce7-03771465ef8e","6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"],
                          "categoryIds": ["%s"],
                          "categorySuggestions": ["defi"],
                          "contributorLabels": [{"id": "%s", "name": "l1"}, {"name": "l3"}]
                        }
                        """.formatted(repo1.getId(), repo3.getId(), cryptoCategory.id(), label1Id.toString()))
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
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/16590657?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        indexerApiWireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "linkedRepoIds": [%d],
                          "unlinkedRepoIds": [%d]
                        }
                        """.formatted(repo3.getId(), repo2.getId())))
        );
        verify(slackApiAdapter).onProjectCategorySuggested("defi", user.userId());

        final var entity = projectRepository.findById(projectId).orElseThrow();
        assertThat(entity.isBotNotifyExternalApplications()).isTrue();
    }

    private void runJobs() {
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateAntho().jwt())
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
        final var otherRepo = githubHelper.createRepo();
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [%d],
                          "unlinkedRepoIds": []
                        }
                        """.formatted(otherRepo.getId()), true, false))
                .willReturn(WireMock.noContent()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/595505?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
        indexerApiWireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-indexer-api-key"))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

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
                            %d
                          ],
                          "logoUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                        }
                        """.formatted(otherRepo.getId()))
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

        projectId2 = responseBody.getProjectId();

        runJobs();

        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/43467246?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        indexerApiWireMockServer.verify(1, putRequestedFor(urlEqualTo("/api/v1/users/595505?forceRefresh=false"))
                .withHeader("Content-Type", equalTo("application/json"))
        );
        indexerApiWireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                          "linkedRepoIds": [%d],
                          "unlinkedRepoIds": []
                        }
                        """.formatted(otherRepo.getId())))
        );
    }

    @SneakyThrows
    @Test
    @Order(40)
    public void should_fail_to_create_a_new_project_with_existing_slug() {
        // Given
        indexerApiWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/events/on-repo-link-changed"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                          "linkedRepoIds": [498695724],
                          "unlinkedRepoIds": []
                        }
                        """, true, false))
                .willReturn(WireMock.noContent()));

        // When
        client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "Testing conflict",
                          "longDescription": "CONFLICT",
                          "isLookingForContributors": false,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505
                          ],
                          "githubRepoIds": [
                            498695724
                          ],
                          "logoUrl": "https://foo.bar",
                          "ecosystemIds" : ["b599313c-a074-440f-af04-a466529ab2e7"]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();

        // Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + "updated-project"))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.visibility").isEqualTo("PUBLIC")
                .jsonPath("$.name").isEqualTo("Updated Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super updated project")
                .jsonPath("$.longDescription").isEqualTo("This is a super awesome updated project with a nice description");
    }

    @SneakyThrows
    @Test
    @Order(40)
    public void should_create_a_new_project_with_same_contributor_labels() {

        // When
        client.post()
                .uri(getApiURI(PROJECTS_POST))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Some projects with same labels",
                          "shortDescription": "Testing",
                          "longDescription": "Labeles",
                          "isLookingForContributors": false,
                          "inviteGithubUserIdsAsProjectLeads": [
                            595505
                          ],
                          "logoUrl": "https://foo.bar",
                          "ecosystemIds" : ["b599313c-a074-440f-af04-a466529ab2e7"],
                          "contributorLabels": [{"name": "l1"}, {"name": "l2"}, {"name": "l3"}]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @SneakyThrows
    @Test
    @Order(41)
    public void should_fail_to_update_the_project_with_existing_slug() {
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
                .uri(getApiURI(format(PROJECTS_PUT, projectId2)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Project",
                          "shortDescription": "Testing conflict",
                          "longDescription": "CONFLICT",
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
                .is4xxClientError();

        // And Then
        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_SLUG + "/" + "updated-project"))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isEqualTo(projectId.toString())
                .jsonPath("$.visibility").isEqualTo("PUBLIC")
                .jsonPath("$.name").isEqualTo("Updated Project")
                .jsonPath("$.shortDescription").isEqualTo("This is a super updated project")
                .jsonPath("$.longDescription").isEqualTo("This is a super awesome updated project with a nice description");
    }

    @Test
    void should_update_contributor_labels() {
        final var lead = userAuthHelper.create();
        final var contributor = userAuthHelper.create();
        final var projectId = projectHelper.create(lead).getLeft();
        final var project = projectHelper.get(projectId);

        final var label1Id = new MutableObject<UUID>();
        final var label2Id = new MutableObject<UUID>();

        // Add new labels
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + lead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateProjectRequest(project)
                        .contributorLabels(List.of(
                                new UpdateProjectRequestContributorLabelsInner("l1"),
                                new UpdateProjectRequestContributorLabelsInner("l2"))
                        ))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributorLabels[0].id").<String>value(v -> label1Id.setValue(UUID.fromString(v)))
                .jsonPath("$.contributorLabels[0].name").isEqualTo("l1")
                .jsonPath("$.contributorLabels[1].id").<String>value(v -> label2Id.setValue(UUID.fromString(v)))
                .jsonPath("$.contributorLabels[1].name").isEqualTo("l2")
        ;

        // Assign labels to contributors
        client.patch()
                .uri(getApiURI(PROJECTS_CONTRIBUTORS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + lead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ContributorsLabelsRequest()
                        .contributorsLabels(List.of(
                                new ContributorLabelsRequest()
                                        .githubUserId(contributor.githubUserId().value())
                                        .labels(List.of(label1Id.getValue(), label2Id.getValue()))
                        )))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Remove label1, update label2 and add label3
        client.put()
                .uri(getApiURI(format(PROJECTS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + lead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateProjectRequest(project)
                        .contributorLabels(List.of(
                                new UpdateProjectRequestContributorLabelsInner("l2-updated").id(label2Id.getValue()),
                                new UpdateProjectRequestContributorLabelsInner("l3"))
                        ))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + projectId))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.contributorLabels[0].id").isEqualTo(label2Id.getValue().toString())
                .jsonPath("$.contributorLabels[0].name").isEqualTo("l2-updated")
                .jsonPath("$.contributorLabels[1].id").isNotEmpty()
                .jsonPath("$.contributorLabels[1].name").isEqualTo("l3")
        ;

        // Check that the contributor has the updated labels
        final var labelsByContributor = contributorProjectContributorLabelRepository.findAll().stream()
                .collect(groupingBy(ContributorProjectContributorLabelEntity::getGithubUserId,
                        mapping(ContributorProjectContributorLabelEntity::getLabelId,
                                toList())));

        assertThat(labelsByContributor).contains(
                entry(contributor.githubUserId().value(), List.of(label2Id.getValue()))
        );
    }

    private UpdateProjectRequest updateProjectRequest(Project project) {
        return new UpdateProjectRequest()
                .name(project.getName())
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .isLookingForContributors(true)
                .contributorLabels(List.of());
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

                .jsonPath("$.organizations.length()").isEqualTo(1)
                .jsonPath("$.organizations[0].login").isEqualTo("onlydustxyz")
                .jsonPath("$.organizations[0].repos.length()").isEqualTo(2)
                .jsonPath("$.organizations[0].repos[0].name").value(v -> assertThat(v).isIn("repo1", "repo3"))
                .jsonPath("$.organizations[0].repos[1].name").value(v -> assertThat(v).isIn("repo1", "repo3"))

                .jsonPath("$.rewardSettings.ignorePullRequests").isEqualTo(false)
                .jsonPath("$.rewardSettings.ignoreIssues").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreCodeReviews").isEqualTo(true)
                .jsonPath("$.rewardSettings.ignoreContributionsBefore").isEqualTo("2021-01-01T00:00:00Z")

                .jsonPath("$.ecosystems[0].name").isEqualTo("Ethereum")
                .jsonPath("$.ecosystems[1].name").isEqualTo("Starknet")

                .jsonPath("$.categories[0].name").isEqualTo("Crypto")
                .jsonPath("$.categorySuggestions[0]").isEqualTo("defi")
        ;
    }
}
