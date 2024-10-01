package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagProject;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@TagProject
public class ProjectContributorLabelApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser projectLead;
    ProjectId projectId;

    @BeforeEach
    void setUp() {
        projectLead = userAuthHelper.create();
        projectId = projectHelper.create(projectLead).getLeft();
    }

    @Test
    void should_create_project_contributor_label() {
        client.post()
                .uri(getApiURI(PROJECT_CONTRIBUTOR_LABELS.formatted(projectId.value())))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + projectLead.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Super Label"
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Super Label");
    }
}
