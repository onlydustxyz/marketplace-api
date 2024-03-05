package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.backoffice.api.contract.model.PayRewardRequest;
import onlydust.com.backoffice.api.contract.model.TransactionNetwork;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiHttpClient;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.api_key.ApiKeyAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

public class BackOfficeV0ApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Autowired
    OdRustApiHttpClient.Properties odRustApiHttpClientProperties;
    @Autowired
    ApiKeyAuthenticationService.Config backOfficeApiKeyAuthenticationConfig;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Test
    void should_pay_reward_on_multiple_currency() {
        // Given
        final PaymentRequestEntity usdReward = paymentRequestRepository.findAll().stream()
                .filter(pre -> pre.getCurrency().equals(CurrencyEnumEntity.usd))
                .findFirst()
                .orElseThrow();
        final String iban = faker.internet().macAddress();
        final String transaction = faker.internet().macAddress();


        rustApiWireMockServer.stubFor(
                WireMock.post("/api/payments/%s/receipts".formatted(usdReward.getId()))
                        .withHeader("Api-Key", WireMock.equalTo(odRustApiHttpClientProperties.getApiKey()))
                        .withRequestBody(WireMock.equalToJson(
                                """
                                        {
                                           "amount": %s,
                                           "currency": "USD",
                                           "recipientWallet": null,
                                           "recipientIban" : "%s",
                                           "transactionReference" : "%s"
                                        }
                                            """.formatted(usdReward.getAmount().toString(), iban, transaction)
                        ))
                        .willReturn(ResponseDefinitionBuilder.okForJson("""
                                {
                                    "receipt_id": "%s"
                                }""".formatted(UUID.randomUUID()))));
        final PayRewardRequest payRewardRequest = new PayRewardRequest()
                .network(TransactionNetwork.SEPA)
                .recipientAccount(iban)
                .reference(transaction);

        // When
        client.post()
                .uri(getApiURI(POST_REWARDS_PAY_VO.formatted(usdReward.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payRewardRequest)
                .header("Api-Key", backOfficeApiKeyAuthenticationConfig.getApiKey())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .isEmpty();
    }
}
