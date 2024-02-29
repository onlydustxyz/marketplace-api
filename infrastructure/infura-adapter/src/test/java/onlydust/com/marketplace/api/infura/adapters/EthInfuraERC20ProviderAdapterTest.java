package onlydust.com.marketplace.api.infura.adapters;

import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmContractAddress;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class EthInfuraERC20ProviderAdapterTest {
    final InfuraClient.Properties properties =
            new InfuraClient.Properties("https://mainnet.infura.io/v3", "<API_KEY>", "<PRIVATE_KEY>", Blockchain.ETHEREUM); // https://key.tokenpocket.pro/#/?network=ETH
    final EthInfuraERC20ProviderAdapter adapter = new EthInfuraERC20ProviderAdapter(properties);
    final EvmContractAddress USDC = Ethereum.contractAddress("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB49");

    //    @Test
    void should_get_erc20_info_from_smart_contract() {
        final var token = adapter.get(USDC).orElseThrow();
        assertThat(token.getAddress()).isEqualTo(USDC);
        assertThat(token.getName()).isEqualTo("USD Coin");
        assertThat(token.getSymbol()).isEqualTo("USDC");
        assertThat(token.getDecimals()).isEqualTo(6);
        assertThat(token.getTotalSupply()).isEqualTo(new BigInteger("22548692993579353"));
    }
}
