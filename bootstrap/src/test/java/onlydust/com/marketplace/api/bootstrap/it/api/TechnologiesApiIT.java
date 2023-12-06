package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.SuggestTechnologyRequest;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;

@ActiveProfiles({"hasura_auth"})
public class TechnologiesApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    private HasuraUserHelper userHelper;

    @Test
    void should_create_linear_issue_upon_new_technology_suggested() {
        // Given
        final var requester = userHelper.authenticatePierre();

        linearWireMockServer.stubFor(post(urlEqualTo("/graphql"))
                .withHeader("Authorization", equalTo("some-linear-api-key"))
                .withHeader("Content-type", equalTo("application/json"))
                .withRequestBody(equalToJson("""
                        {
                            "query" : "mutation($title: String!, $description: String!, $teamId: String!, $stateId: String!, $labelId: String!) {\\n    issueCreate(input: { title: $title, description: $description, teamId: $teamId, stateId: $stateId, labelIds: [$labelId] }) {\\n        success\\n  \s }\\n}\\n",
                            "variables" : {
                              "title" : "New technology suggestion: Rust",
                              "teamId" : "fac4208d-473d-47c4-9356-c277878146a8",
                              "stateId" : "35c07f98-74f2-4b43-bfd1-d394addf337b",
                              "description" : "Suggested by: PierreOucif",
                              "labelId" : "08975455-9a1b-427d-9824-c4e172ada649"
                            }
                          }
                        """))
                .willReturn(jsonResponse("""
                        {
                            "data": {
                                "issueCreate": {
                                    "success": true
                                }
                            }
                        }
                        """, HttpStatus.CREATED.value())
                ));

        final var request = new SuggestTechnologyRequest().technology("Rust");

        // When
        client.post()
                .uri(getApiURI(SUGGEST_NEW_TECHNOLOGY))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + requester.jwt())
                .body(BodyInserters.fromValue(request))
                // Then
                .exchange()
                .expectStatus()
                .isCreated();

        linearWireMockServer.verify(postRequestedFor(urlEqualTo("/graphql")));
    }

    @Test
    void require_authenticated_user_to_suggest_new_technology() {
        // Given
        final var request = new SuggestTechnologyRequest().technology("Rust");

        // When
        client.post()
                .uri(getApiURI(SUGGEST_NEW_TECHNOLOGY))
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void should_return_error_upon_linear_reject() {
        // Given
        final var requester = userHelper.authenticatePierre();

        linearWireMockServer.stubFor(post(urlEqualTo("/graphql"))
                .willReturn(jsonResponse("""
                        {
                            "data": {
                                "issueCreate": {
                                    "success": false
                                }
                            }
                        }
                        """, HttpStatus.OK.value())
                ));

        final var request = new SuggestTechnologyRequest().technology("Rust");

        // When
        client.post()
                .uri(getApiURI(SUGGEST_NEW_TECHNOLOGY))
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + requester.jwt())
                .body(BodyInserters.fromValue(request))
                // Then
                .exchange()
                .expectStatus()
                .is5xxServerError();

        linearWireMockServer.verify(postRequestedFor(urlEqualTo("/graphql")));
    }
}
