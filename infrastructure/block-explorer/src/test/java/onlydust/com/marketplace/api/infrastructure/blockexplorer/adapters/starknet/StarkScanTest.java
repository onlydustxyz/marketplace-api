package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.starknet;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.Environment;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class StarkScanTest {
    @Nested
    class Mainnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.MAINNET)
                .build();

        final StarkScan explorer = new StarkScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(StarkNet.transactionHash("0x1")).toString())
                    .isEqualTo("https://starkscan.co/tx/0x01");
        }
    }

    @Nested
    class Testnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.TESTNET)
                .build();

        final StarkScan explorer = new StarkScan(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(StarkNet.transactionHash("0x1")).toString())
                    .isEqualTo("https://sepolia.starkscan.co/tx/0x01");
        }
    }

}