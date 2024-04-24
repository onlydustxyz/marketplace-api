package onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters;

import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;

import static org.assertj.core.api.Assertions.assertThat;

class AptosAccountValidatorAdapterTest {
    final RpcClient.Properties properties = new RpcClient.Properties("https://api.mainnet.aptoslabs.com/v1");
    final RpcClient client = new RpcClient(properties);
    final AptosAccountValidatorAdapter aptosAccountValidatorAdapter = new AptosAccountValidatorAdapter(client);

    //    @Test
    void isValid() {
        assertThat(aptosAccountValidatorAdapter.isValid(Aptos.accountAddress("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73"))).isTrue();
        assertThat(aptosAccountValidatorAdapter.isValid(Aptos.accountAddress("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac74"))).isFalse();
    }
}