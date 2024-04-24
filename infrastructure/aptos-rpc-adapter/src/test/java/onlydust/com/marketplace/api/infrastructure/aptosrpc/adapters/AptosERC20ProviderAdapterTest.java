package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;

import static org.assertj.core.api.Assertions.assertThat;

class AptosERC20ProviderAdapterTest {
    final RpcClient.Properties properties = new RpcClient.Properties("https://api.mainnet.aptoslabs.com/v1");
    final RpcClient client = new RpcClient(properties);
    final AptosERC20ProviderAdapter aptosERC20ProviderAdapter = new AptosERC20ProviderAdapter(client);

    //    @Test
    void get_coin_info() {
        final var coin = aptosERC20ProviderAdapter.get(Aptos.coinType("0xf22bede237a07e121b56d91a491eb7bcdfd1f5907926a9e58338f964a01b17fa::asset::USDT"));
        assertThat(coin).isPresent();
        assertThat(coin.get().getName()).isEqualTo("Tether USD");
        assertThat(coin.get().getSymbol()).isEqualTo("USDT");
        assertThat(coin.get().getDecimals()).isEqualTo(6);
    }
}