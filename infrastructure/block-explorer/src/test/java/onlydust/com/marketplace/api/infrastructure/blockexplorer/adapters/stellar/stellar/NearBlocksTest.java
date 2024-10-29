package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.stellar.stellar;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.near.NearBlocks;
import onlydust.com.marketplace.kernel.model.Environment;
import onlydust.com.marketplace.kernel.model.blockchain.Near;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearBlocksTest {
    @Nested
    class Mainnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.MAINNET)
                .build();

        final NearBlocks explorer = new NearBlocks(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Near.transactionHash("Es4Rsp9bt1x7g14PNn9V7GCfqH2HRJL4FuiQa1cjETYR")).toString())
                    .isEqualTo("https://nearblocks.io/txns/Es4Rsp9bt1x7g14PNn9V7GCfqH2HRJL4FuiQa1cjETYR");
        }
    }

    @Nested
    class Testnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.TESTNET)
                .build();

        final NearBlocks explorer = new NearBlocks(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Near.transactionHash("Es4Rsp9bt1x7g14PNn9V7GCfqH2HRJL4FuiQa1cjETYR")).toString())
                    .isEqualTo("https://testnet.nearblocks.io/txns/Es4Rsp9bt1x7g14PNn9V7GCfqH2HRJL4FuiQa1cjETYR");
        }
    }
}