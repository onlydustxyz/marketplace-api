package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.optimism;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.Environment;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtherScanTest {
    @Nested
    class Mainnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.MAINNET)
                .build();

        final EtherScan explorer = new EtherScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Optimism.transactionHash("0x1")).toString())
                    .isEqualTo("https://optimistic.etherscan.io/tx/0x01");
        }
    }

    @Nested
    class Testnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.TESTNET)
                .build();

        final EtherScan explorer = new EtherScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Optimism.transactionHash("0x1")).toString())
                    .isEqualTo("https://sepolia-optimism.etherscan.io/tx/0x01");
        }
    }
}