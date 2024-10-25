package onlydust.com.marketplace.api.stellar;

import com.github.tomakehurst.wiremock.WireMockServer;
import onlydust.com.marketplace.api.stellar.adapters.StellarTransactionStorageAdapter;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransferTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class StellarTransactionStorageAdapterTest {

    private StellarTransactionStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        final var sorobanServerMock = wiremock("soroban", "https://soroban-rpc.mainnet.stellar.gateway.fm");
        final var horizonServerMock = wiremock("horizon", "https://stellar.public-rpc.com/http/stellar_horizon");

        final var sorobanClient = new SorobanClient(new SorobanClient.Properties(sorobanServerMock.baseUrl(),
                "GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL"));

        final var horizonClient = new HorizonClient(new HorizonClient.Properties(horizonServerMock.baseUrl()));

        adapter = new StellarTransactionStorageAdapter(sorobanClient, horizonClient);
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
    void should_create_transactions_from_payment_operation_usdc() {
        final var transaction = adapter.get(Stellar.transactionHash("97157d6c947af69ea379edee2883562cb4b18a7882d366d368b595d899d82835")).orElseThrow();
        assertThat(transaction.timestamp()).isEqualTo(ZonedDateTime.parse("2024-09-02T14:41:14Z"));
        assertThat(transaction.blockchain()).isEqualTo(Blockchain.STELLAR);
        assertThat(transaction.reference()).isEqualTo("97157d6c947af69ea379edee2883562cb4b18a7882d366d368b595d899d82835");
        assertThat(transaction.status()).isEqualTo(Blockchain.Transaction.Status.CONFIRMED);

        assertThat(transaction).isInstanceOf(StellarTransferTransaction.class);
        final var transfer = (Blockchain.TransferTransaction) transaction;
        assertThat(transfer.senderAddress()).isEqualTo("GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL");
        assertThat(transfer.recipientAddress()).isEqualTo("GD2VXOJ7SJS6GYWT3LIKS5KWBXYTZGIJ5TBLP5TGIKBPPWRALCZMGXEF");
        assertThat(transfer.contractAddress()).contains("CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75");
        assertThat(transfer.amount()).isEqualByComparingTo(BigDecimal.valueOf(0.1837502));
    }

    @Test
    void should_create_transactions_from_payment_operation_xlm() {
        final var transaction = adapter.get(Stellar.transactionHash("01cab5c04cf265b2995a2e5c4e961cad82d38bfb9e950ec3f6e33e5ff28500d8")).orElseThrow();
        assertThat(transaction.timestamp()).isEqualTo(ZonedDateTime.parse("2024-09-02T12:29:13Z"));
        assertThat(transaction.blockchain()).isEqualTo(Blockchain.STELLAR);
        assertThat(transaction.reference()).isEqualTo("01cab5c04cf265b2995a2e5c4e961cad82d38bfb9e950ec3f6e33e5ff28500d8");
        assertThat(transaction.status()).isEqualTo(Blockchain.Transaction.Status.CONFIRMED);

        assertThat(transaction).isInstanceOf(StellarTransferTransaction.class);
        final var transfer = (Blockchain.TransferTransaction) transaction;
        assertThat(transfer.senderAddress()).isEqualTo("GAIYZIEWGAEYIVMX5TMSD43HROWXX5WG35KTL6467P52S477IQQJIUEL");
        assertThat(transfer.recipientAddress()).isEqualTo("GD2VXOJ7SJS6GYWT3LIKS5KWBXYTZGIJ5TBLP5TGIKBPPWRALCZMGXEF");
        assertThat(transfer.contractAddress()).isEmpty();
        assertThat(transfer.amount()).isEqualByComparingTo(BigDecimal.valueOf(1.276));
    }

    @Test
    void should_create_transactions_from_account_creation_operation() {
        final var transaction = adapter.get(Stellar.transactionHash("69e9fe804c4a2416aa3e041c5ed585bfc853a26c3226ff0f8da034b4091b71c2")).orElseThrow();
        assertThat(transaction.timestamp()).isEqualTo(ZonedDateTime.parse("2024-10-24T08:53:53Z"));
        assertThat(transaction.blockchain()).isEqualTo(Blockchain.STELLAR);
        assertThat(transaction.reference()).isEqualTo("69e9fe804c4a2416aa3e041c5ed585bfc853a26c3226ff0f8da034b4091b71c2");
        assertThat(transaction.status()).isEqualTo(Blockchain.Transaction.Status.CONFIRMED);

        assertThat(transaction).isInstanceOf(StellarTransferTransaction.class);
        final var transfer = (Blockchain.TransferTransaction) transaction;
        assertThat(transfer.senderAddress()).isEqualTo("GCY2AHYGO4DBKMMNITVD7ZHYG5W2PEYFW7XOCJVUPI3GAOYNR5HVRN3O");
        assertThat(transfer.recipientAddress()).isEqualTo("GAY2YYJRUHE2YEXVNTHGTS3W74RCAA5FH4X3L7ECM254YZATR5IOQLZB");
        assertThat(transfer.contractAddress()).isEmpty();
        assertThat(transfer.amount()).isEqualByComparingTo(BigDecimal.valueOf(5303.6883360));
    }
}