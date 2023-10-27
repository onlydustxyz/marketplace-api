package onlydust.com.marketplace.api.bootstrap.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import onlydust.com.marketplace.api.contract.model.CurrencyContract;
import onlydust.com.marketplace.api.contract.model.RewardRequest;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.IMPERSONATION_HEADER;

@ActiveProfiles({"hasura_auth"})
public class ProjectDeleteRewardsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    public HasuraUserHelper hasuraUserHelper;
    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    AuthenticationService authenticationService;

    @Test
    public void should_be_unauthorized() {
        final RewardRequest rewardRequest = new RewardRequest()
                .amount(BigDecimal.ONE)
                .currency(CurrencyContract.USD)
                .recipientId(1L);
        final UUID projectId = UUID.randomUUID();
        final UUID rewardId = UUID.randomUUID();

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(401);
    }

    @Test
    void should_be_forbidden_given_authenticated_user_not_project_lead() throws JsonProcessingException {
        // Given
        hasuraUserHelper.newFakeUser(UUID.randomUUID(), 1L, faker.rickAndMorty().character(), faker.internet().url(),
                false);
        final String jwt = hasuraUserHelper.authenticateUser(1L).jwt();
        final UUID projectId = projectRepository.findAll().get(0).getId();
        final UUID rewardId = UUID.randomUUID();

        // When
        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(403)
                .expectBody()
                .jsonPath("$.message").isEqualTo("FORBIDDEN");
    }

    @Test
    void should_request_reward_to_old_api_given_a_project_lead() throws JsonProcessingException {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var rewardId = UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0");

        // When
        rustApiWireMockServer.stubFor(WireMock.delete(
                        WireMock.urlEqualTo("/api/payments/8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                .withHeader("Authorization", equalTo(BEARER_PREFIX + jwt))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);
    }

    @Test
    void should_request_reward_to_old_api_given_a_project_lead_impersonated() throws JsonProcessingException {
        // Given
        final String jwt = hasuraUserHelper.newFakeUser(UUID.randomUUID(), 2L, faker.rickAndMorty().character(),
                faker.internet().url(), true).jwt();
        hasuraUserHelper.authenticateUser(2L);
        final String impersonatePierreHeader =
                hasuraUserHelper.getImpersonationHeaderToImpersonatePierre();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var rewardId = UUID.fromString("8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0");

        // When
        rustApiWireMockServer.stubFor(WireMock.delete(
                        WireMock.urlEqualTo("/api/payments/8fe07ae1-cf3b-4401-8958-a9e0b0aec7b0"))
                .withHeader("Authorization", equalTo(BEARER_PREFIX + jwt))
                .withHeader(IMPERSONATION_HEADER, equalTo(impersonatePierreHeader))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.delete()
                .uri(getApiURI(String.format(PROJECTS_REWARD, projectId, rewardId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header(IMPERSONATION_HEADER, impersonatePierreHeader)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);
    }


}
