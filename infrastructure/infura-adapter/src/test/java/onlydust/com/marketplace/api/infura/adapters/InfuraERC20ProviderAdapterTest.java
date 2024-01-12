package onlydust.com.marketplace.api.infura.adapters;

import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InfuraERC20ProviderAdapterTest {
    final InfuraClient.Properties properties = new InfuraClient.Properties("https://mainnet.infura.io/v3", "<API_KEY>", "<PRIVATE_KEY>"); // https://key.tokenpocket.pro/#/?network=ETH
    final InfuraERC20ProviderAdapter adapter = new InfuraERC20ProviderAdapter(properties);
    final ContractAddress USDC = Ethereum.contractAddress("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB49");

    //    @Test
    void should_get_erc20_info_from_smart_contract() {
        final var token = adapter.get(USDC).orElseThrow();
        assertThat(token.address()).isEqualTo(USDC);
        assertThat(token.name()).isEqualTo("USD Coin");
        assertThat(token.symbol()).isEqualTo("USDC");
        assertThat(token.decimals()).isEqualTo(6);
        assertThat(token.totalSupply()).isEqualTo(new BigInteger("22548692993579353"));
    }
}
