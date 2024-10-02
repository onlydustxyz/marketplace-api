package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ContributorProjectContributorLabelEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributorProjectContributorLabelRepository;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;
import onlydust.com.marketplace.project.domain.port.output.ProjectContributorLabelStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TagProject
public class ProjectContributorLabelApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectContributorLabelStoragePort projectContributorLabelStoragePort;
    @Autowired
    ContributorProjectContributorLabelRepository contributorProjectContributorLabelRepository;

    UserAuthHelper.AuthenticatedUser projectLead;
    ProjectId projectId;

    @BeforeEach
    void setUp() {
        projectLead = userAuthHelper.create();
        projectId = projectHelper.create(projectLead).getLeft();
    }

    @Test
    void should_forbid_access_when_not_project_lead() {
        final var other = userAuthHelper.create();
        final var label = ProjectContributorLabel.of(projectId, "Label 403");
        projectContributorLabelStoragePort.save(label);

        client.post()
                .uri(getApiURI(PROJECT_CONTRIBUTOR_LABELS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + other.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "New Label 403"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();

        client.put()
                .uri(getApiURI(CONTRIBUTOR_LABEL_BY_ID.formatted(label.id().value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + other.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Label 403"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isForbidden();

        client.delete()
                .uri(getApiURI(CONTRIBUTOR_LABEL_BY_ID.formatted(label.id().value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + other.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();
    }

    @Test
    void should_create_project_contributor_label() {
        client.post()
                .uri(getApiURI(PROJECT_CONTRIBUTOR_LABELS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Label 42"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Label 42");
    }

    @Test
    void should_update_project_contributor_label() {
        final var label = ProjectContributorLabel.of(projectId, "Label 100");
        projectContributorLabelStoragePort.save(label);

        client.put()
                .uri(getApiURI(CONTRIBUTOR_LABEL_BY_ID.formatted(label.id().value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Updated Label 100"
                        }
                        """)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(PROJECT_CONTRIBUTOR_LABELS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.labels.length()").isEqualTo(1)
                .jsonPath("$.labels[0].id").isEqualTo(label.id().value().toString())
                .jsonPath("$.labels[0].name").isEqualTo("Updated Label 100");
    }

    @Test
    void should_delete_project_contributor_label() {
        final var label = ProjectContributorLabel.of(projectId, "Label 200");
        projectContributorLabelStoragePort.save(label);

        client.delete()
                .uri(getApiURI(CONTRIBUTOR_LABEL_BY_ID.formatted(label.id().value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        client.get()
                .uri(getApiURI(PROJECT_CONTRIBUTOR_LABELS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.labels.length()").isEqualTo(0);
    }

    @Test
    void should_update_labels_of_contributors() {
        final var label1 = ProjectContributorLabel.of(projectId, "Label 1001");
        final var label2 = ProjectContributorLabel.of(projectId, "Label 1002");
        final var label3 = ProjectContributorLabel.of(projectId, "Label 1003");
        final var label4 = ProjectContributorLabel.of(projectId, "Label 1004");
        projectContributorLabelStoragePort.save(label1);
        projectContributorLabelStoragePort.save(label2);
        projectContributorLabelStoragePort.save(label3);
        projectContributorLabelStoragePort.save(label4);

        final var olivier = userAuthHelper.authenticateOlivier();
        final var pierre = userAuthHelper.authenticatePierre();

        // When
        client.patch()
                .uri(getApiURI(PROJECTS_CONTRIBUTORS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributorsLabels": [
                            {
                              "githubUserId": %d,
                              "labels": ["%s", "%s", "%s"]
                            }
                          ]
                        }
                        """.formatted(olivier.githubUserId().value(), label1.id().value(), label2.id().value(), label3.id().value()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        Map<Long, List<UUID>> result = contributorProjectContributorLabelRepository.findAll().stream()
                .collect(Collectors.groupingBy(ContributorProjectContributorLabelEntity::getGithubUserId,
                        Collectors.mapping(ContributorProjectContributorLabelEntity::getLabelId, Collectors.toList())));
        assertThat(result.keySet()).hasSize(1);
        assertThat(result.get(olivier.githubUserId().value())).containsExactlyInAnyOrder(label1.id().value(), label2.id().value(), label3.id().value());

        // When
        client.patch()
                .uri(getApiURI(PROJECTS_CONTRIBUTORS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributorsLabels": [
                            {
                              "githubUserId": %d,
                              "labels": ["%s", "%s"]
                            },{
                              "githubUserId": %d,
                              "labels": ["%s", "%s"]
                            }
                          ]
                        }
                        """.formatted(pierre.githubUserId().value(), label1.id().value(), label2.id().value(),
                        olivier.githubUserId().value(), label1.id().value(), label4.id().value()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        // Then
        result = contributorProjectContributorLabelRepository.findAll().stream()
                .collect(Collectors.groupingBy(ContributorProjectContributorLabelEntity::getGithubUserId,
                        Collectors.mapping(ContributorProjectContributorLabelEntity::getLabelId, Collectors.toList())));
        assertThat(result.keySet()).hasSize(2);
        assertThat(result.get(olivier.githubUserId().value())).containsExactlyInAnyOrder(label1.id().value(), label4.id().value());
        assertThat(result.get(pierre.githubUserId().value())).containsExactlyInAnyOrder(label1.id().value(), label2.id().value());
    }
}
