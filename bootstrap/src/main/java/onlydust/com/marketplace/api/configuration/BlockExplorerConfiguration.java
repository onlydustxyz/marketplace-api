package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.infrastructure.blockexplorer.BlockExplorerProperties;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.aptos.AptoScan;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.ethereum.EtherScan;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.starknet.StarkScan;
import onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.stellar.StellarExpert;
import onlydust.com.marketplace.kernel.model.blockchain.BlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.MetaBlockExplorer;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetTransaction;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarTransaction;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockExplorerConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.block-explorer", ignoreUnknownFields = false)
    public BlockExplorerProperties blockExplorerProperties() {
        return new BlockExplorerProperties();
    }

    @Bean
    public BlockExplorer<AptosTransaction.Hash> aptosBlockExplorer(final BlockExplorerProperties blockExplorerProperties) {
        return new AptoScan(blockExplorerProperties);
    }

    @Bean
    public BlockExplorer<EvmTransaction.Hash> ethereumBlockExplorer(final BlockExplorerProperties blockExplorerProperties) {
        return new EtherScan(blockExplorerProperties);
    }

    @Bean
    public BlockExplorer<EvmTransaction.Hash> optimismBlockExplorer(final BlockExplorerProperties blockExplorerProperties) {
        return new onlydust.com.marketplace.api.infrastructure.blockexplorer.adapters.optimism.EtherScan(blockExplorerProperties);
    }

    @Bean
    public BlockExplorer<StarknetTransaction.Hash> starknetBlockExplorer(final BlockExplorerProperties blockExplorerProperties) {
        return new StarkScan(blockExplorerProperties);
    }

    @Bean
    public BlockExplorer<StellarTransaction.Hash> stellarBlockExplorer(final BlockExplorerProperties blockExplorerProperties) {
        return new StellarExpert(blockExplorerProperties);
    }

    @Bean
    public MetaBlockExplorer blockExplorer(final BlockExplorer<AptosTransaction.Hash> aptosBlockExplorer,
                                           final BlockExplorer<EvmTransaction.Hash> ethereumBlockExplorer,
                                           final BlockExplorer<EvmTransaction.Hash> optimismBlockExplorer,
                                           final BlockExplorer<StarknetTransaction.Hash> starknetBlockExplorer,
                                           final BlockExplorer<StellarTransaction.Hash> stellarBlockExplorer) {
        return new MetaBlockExplorer(
                aptosBlockExplorer,
                ethereumBlockExplorer,
                optimismBlockExplorer,
                starknetBlockExplorer,
                stellarBlockExplorer);
    }
}
