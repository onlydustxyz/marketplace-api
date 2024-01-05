package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;


public class ProjectMarkInvoiceAsReceivedRewardsApiIT extends AbstractMarketplaceApiIT {

    @Autowired
    public UserAuthHelper userAuthHelper;

    @Test
    void should_forward_mark_invoice_as_received_to_old_api() {
        // Given
        final UserAuthHelper.AuthenticatedUser gregoire = userAuthHelper.authenticateGregoire();
        final String jwt = gregoire.jwt();

        // When
        rustApiWireMockServer.stubFor(WireMock.put(
                        WireMock.urlEqualTo("/api/payments/invoiceReceivedAt"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("Api-Key", equalTo("some-rust-api-key"))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "payments": [
                                "64fb2732-5632-4b09-a8b1-217485648129",
                                "fab7aaf4-9b0c-4e52-bc9b-72ce08131617",
                                "5f9060a7-6f9e-4ef7-a1e4-1aaa4c85f03c"
                            ]
                        }
                        """))
                .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));

        client.post()
                .uri(getApiURI(ME_POST_MARK_INVOICE_AS_RECEIVED))
                .header("Authorization", BEARER_PREFIX + jwt)
                .header("Content-Type", "application/json")
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);
    }

}
