package onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.stellar;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.kernel.model.Environment;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StellarExpertTest {
    @Nested
    class Mainnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.MAINNET)
                .build();

        final StellarExpert explorer = new StellarExpert(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Stellar.transactionHash("123")).toString())
                    .isEqualTo("https://stellar.expert/explorer/public/tx/0123");
        }
    }

    @Nested
    class Testnet {
        final BlockExplorerProperties properties = BlockExplorerProperties.builder()
                .environment(Environment.TESTNET)
                .build();

        final StellarExpert explorer = new StellarExpert(properties);

        @Test
        void should_generate_transaction_url() {
            assertThat(explorer.url(Stellar.transactionHash("123")).toString())
                    .isEqualTo("https://stellar.expert/explorer/testnet/tx/0123");
        }
    }
}