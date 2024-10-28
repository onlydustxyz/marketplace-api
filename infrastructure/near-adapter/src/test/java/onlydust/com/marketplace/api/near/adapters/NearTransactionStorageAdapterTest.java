package onlydust.com.marketplace.api.near.adapters;

import com.github.tomakehurst.wiremock.WireMockServer;
import onlydust.com.marketplace.api.near.NearClient;
import onlydust.com.marketplace.kernel.model.blockchain.Near;
import onlydust.com.marketplace.kernel.model.blockchain.near.NearTransferTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.NEAR;
import static onlydust.com.marketplace.kernel.model.blockchain.Blockchain.Transaction.Status.CONFIRMED;
import static org.assertj.core.api.Assertions.assertThat;

class NearTransactionStorageAdapterTest {

    private NearTransactionStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        final var nearServerMock = wiremock("near", "https://rpc.testnet.near.org");

        final var nearClient = NearClient.create(new NearClient.Properties(nearServerMock.baseUrl()));

        adapter = new NearTransactionStorageAdapter(nearClient);
    }

    private WireMockServer wiremock(String name, String url) {
        final var server = new WireMockServer(options()
                .dynamicPort()
                .globalTemplating(true)
                .usingFilesUnderClasspath(name));
//        server.startRecording(url);
        server.start();
        return server;
    }

    @Test
    void transaction_success() {
        final var transaction = adapter.get(Near.transactionHash("4exboD32LtvFes5xzun372LcebX3oC359W6Um2hw9eoV")).orElseThrow();

        assertThat(transaction.status()).isEqualTo(CONFIRMED);
        assertThat(transaction.blockchain()).isEqualTo(NEAR);
        assertThat(transaction.reference()).isEqualTo("4exboD32LtvFes5xzun372LcebX3oC359W6Um2hw9eoV");
        assertThat(transaction).isInstanceOf(NearTransferTransaction.class);

        final var transfer = (NearTransferTransaction) transaction;
        assertThat(transfer.senderAddress()).isEqualTo("abuisset.testnet");
        assertThat(transfer.recipientAddress()).isEqualTo("onlydust.testnet");
        assertThat(transfer.amount()).isEqualByComparingTo(BigDecimal.valueOf(1.2));
        assertThat(transfer.contractAddress()).isEmpty();
        assertThat(transfer.timestamp()).isEqualToIgnoringNanos(ZonedDateTime.parse("2024-10-28T13:25:31.926254318Z"));
    }

    @Test
    void transaction_not_found() {
        final var transaction = adapter.get(Near.transactionHash("4exboD32LtvFes5xzun372LcebX3oC359W6Um2hw9eox"));
        assertThat(transaction).isEmpty();
    }
}