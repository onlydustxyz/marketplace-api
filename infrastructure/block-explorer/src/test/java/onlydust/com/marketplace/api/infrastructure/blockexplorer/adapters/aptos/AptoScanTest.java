package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.aptos;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.Environment;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AptoScanTest {
    @Nested
    class Mainnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.MAINNET)
                .build();

        final AptoScan explorer = new AptoScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Aptos.transactionHash("0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23")).toString())
                    .isEqualTo("https://aptoscan.com/transaction/0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23?network=mainnet");
        }
    }

    @Nested
    class Testnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.TESTNET)
                .build();

        final AptoScan explorer = new AptoScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Aptos.transactionHash("0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23")).toString())
                    .isEqualTo("https://aptoscan.com/transaction/0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23?network=testnet");
        }
    }
}