package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.HasuraUserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectMarkInvoiceAsReceivedRewardsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    public HasuraUserHelper hasuraUserHelper;

    @Test
    void should_forward_mark_invoice_as_received_to_old_api() {
        // Given
        final HasuraUserHelper.AuthenticatedUser pierre = hasuraUserHelper.authenticatePierre();
        final String jwt = pierre.jwt();
        final UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723");
        final var rewardId1 = UUID.randomUUID();
        final var rewardId2 = UUID.randomUUID();

        // When
        rustApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/payments/invoiceReceivedAt"))
                .withHeader("Authorization", equalTo(BEARER_PREFIX + jwt))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "payments": [
                                "%s", "%s"
                            ]
                        }
                        """.formatted(rewardId1, rewardId2)))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.put()
                .uri(getApiURI(String.format(PROJECTS_REWARD_MARK_INVOICE_AS_RECEIVED, projectId)))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header("Content-Type", "application/json")
                .bodyValue("""
                        {
                            "rewardIds": [
                                "%s", "%s"
                            ]
                        }
                        """.formatted(rewardId1, rewardId2))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);
    }

}
