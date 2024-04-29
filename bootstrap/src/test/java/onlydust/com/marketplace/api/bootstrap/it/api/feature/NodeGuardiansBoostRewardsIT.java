package onlydust.com.marketplace.api.bootstrap.it.api.feature;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.node.guardians.NodeGuardiansApiProperties;
import onlydust.com.marketplace.api.postgres.adapter.repository.BoostNodeGuardiansRewardsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.NodeGuardianBoostRewardRepository;
import onlydust.com.marketplace.project.domain.port.input.BoostNodeGuardiansRewardsPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class NodeGuardiansBoostRewardsIT extends AbstractMarketplaceApiIT {

    @Autowired
    BoostNodeGuardiansRewardsPort boostNodeGuardiansRewardsPort;
    @Autowired
    NodeGuardiansApiProperties nodeGuardiansApiProperties;
    @Autowired
    NodeGuardianBoostRewardRepository nodeGuardianBoostRewardRepository;
    @Autowired
    BoostNodeGuardiansRewardsRepository boostNodeGuardiansRewardsRepository;

    public void setupNodeGuardiansApiMocks() {
        nodeGuardiansWireMockServer.stubFor(WireMock.get("/api/partnership/only-dust/PierreOucif").withHeader("Authorization",
                WireMock.equalTo("Bearer %s".formatted(nodeGuardiansApiProperties.getApiKey()))).willReturn(ResponseDefinitionBuilder.okForJson("""
                {
                  "level": 2
                }
                """)));
    }

    @Test
    void should_boost_rewards() {
        // Given
        setupNodeGuardiansApiMocks();
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticatePierre();
        final UUID projectId = UUID.fromString("45ca43d6-130e-4bf7-9776-2b1eb1dcb782"); // Marketplace
        final Long githubRepoId = faker.number().randomNumber();
        final UUID ecosystemId = UUID.fromString("6ab7fa6c-c418-4997-9c5f-55fb021a8e5c"); // Ethereum linked to Bretzel

        // When
        boostNodeGuardiansRewardsPort.boostProject(projectId, authenticatedUser.user().getId(), githubRepoId, ecosystemId);

        // Then
        Assertions.assertEquals(0, nodeGuardianBoostRewardRepository.count());
        Assertions.assertEquals(0, boostNodeGuardiansRewardsRepository.count());
    }
}
