package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeaderInvitationEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeaderInvitationRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeClaimProjectApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    HasuraUserHelper hasuraUserHelper;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    ProjectLeaderInvitationRepository projectLeaderInvitationRepository;

    @Test
    @Order(1)
    void should_not_claim_project_given_current_user_is_external_from_one_organization() {
        // Given
        final String githubPAT = faker.rickAndMorty().character();
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre(githubPAT);
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        projectLeadRepository.delete(ProjectLeadEntity.builder()
                .projectId(projectId)
                .userId(pierre.user().getId())
                .build());
        projectLeadRepository.delete(ProjectLeadEntity.builder()
                .projectId(projectId)
                .userId(UUID.fromString("45e98bf6-25c2-4edf-94da-e340daba8964"))
                .build());
        projectLeaderInvitationRepository.delete(new ProjectLeaderInvitationEntity(UUID.fromString("02615584-4ff6" +
                                                                                                   "-4f82-82f7" +
                                                                                                   "-0e136b676310"),
                projectId, 98735421L));


        // When
        githubWireMockServer.stubFor(get(urlEqualTo("/orgs/onlydustxyz/memberships/PierreOucif")).withHeader(
                        "Authorization", equalTo("Bearer " + githubPAT))
                .willReturn(jsonResponse("""
                        {
                            "message": "You must be a member of Barbicane-fr to see membership information for PierreOucif.",
                            "documentation_url": "https://docs.github.com/rest/orgs/members#get-organization-membership-for-a-user"
                        }""", 403)));
        client.put()
                .uri(getApiURI(String.format(ME_CLAIM_PROJECT, projectId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403);
    }

    @Test
    @Order(2)
    void should_claim_project() {
        // Given
        final String githubPAT = faker.rickAndMorty().character();
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre(githubPAT);
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");

        // When
        githubWireMockServer.stubFor(get(urlEqualTo("/orgs/onlydustxyz/memberships/PierreOucif")).withHeader(
                        "Authorization", equalTo("Bearer " + githubPAT))
                .willReturn(jsonResponse(GET_PIERRE_BARBICANE_GITHUB_MEMBERSHIP_JSON_RESPONSE, 200)));
        client.put()
                .uri(getApiURI(String.format(ME_CLAIM_PROJECT, projectId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);

        assertTrue(projectLeadRepository.findById(new ProjectLeadEntity.PrimaryKey(projectId,
                pierre.user().getId()
        )).isPresent());
    }

    private static final String GET_PIERRE_BARBICANE_GITHUB_MEMBERSHIP_JSON_RESPONSE = """
            {
                "url": "https://api.github.com/orgs/Barbicane-fr/memberships/PierreOucif",
                "state": "active",
                "role": "admin",
                "organization_url": "https://api.github.com/orgs/Barbicane-fr",
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
                "organization": {
                    "login": "Barbicane-fr",
                    "id": 58205251,
                    "node_id": "MDEyOk9yZ2FuaXphdGlvbjU4MjA1MjUx",
                    "url": "https://api.github.com/orgs/Barbicane-fr",
                    "repos_url": "https://api.github.com/orgs/Barbicane-fr/repos",
                    "events_url": "https://api.github.com/orgs/Barbicane-fr/events",
                    "hooks_url": "https://api.github.com/orgs/Barbicane-fr/hooks",
                    "issues_url": "https://api.github.com/orgs/Barbicane-fr/issues",
                    "members_url": "https://api.github.com/orgs/Barbicane-fr/members{/member}",
                    "public_members_url": "https://api.github.com/orgs/Barbicane-fr/public_members{/member}",
                    "avatar_url": "https://avatars.githubusercontent.com/u/58205251?v=4",
                    "description": ""
                }
            }""";
}
