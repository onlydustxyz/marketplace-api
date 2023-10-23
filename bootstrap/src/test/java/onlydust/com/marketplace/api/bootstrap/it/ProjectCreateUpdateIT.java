package onlydust.com.marketplace.api.bootstrap.it;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"hasura_auth"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectCreateUpdateIT extends AbstractMarketplaceApiIT {

    @Test
    @Order(3)
    public void should_create_a_new_project() {
        // When
        final var response = client.post()
                .uri(getApiURI(PROJECTS_POST))
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
                            498695724, 698096830
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
//                .is2xxSuccessful()
//                .expectBody(CreateProjectResponse.class)
//                .returnResult().getResponseBody();
//
//        assertThat(response).isNotNull();
//        assertThat(response.getProjectId()).isNotNull();

        // When
//        client.get()
//                .uri(getApiURI(PROJECTS_GET_BY_ID + "/" + response.getProjectId()))
//                .exchange()
//                // Then
//                .expectStatus()
//                .is2xxSuccessful()
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(response.getProjectId().toString())
//                .jsonPath("$.name").isEqualTo("Super Project")
//                .jsonPath("$.shortDescription").isEqualTo("This is a super project")
//                .jsonPath("$.hiring").isEqualTo(true)
//                .jsonPath("$.repos[0].name").isEqualTo("marketplace-frontend");
    }
}
