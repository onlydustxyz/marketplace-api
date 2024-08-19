package onlydust.com.marketplace.api.infura.adapters;

import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;

import static org.assertj.core.api.Assertions.assertThat;

class EthInfuraEnsValidatorAdapterTest {

    final Web3Client.Properties properties =
            new Web3Client.Properties("https://mainnet.infura.io/v3/<API_KEY>", "<PRIVATE_KEY>", Blockchain.ETHEREUM); // https://key.tokenpocket.pro/#/?network=ETH
    final EthWeb3EnsValidatorAdapter adapter = new EthWeb3EnsValidatorAdapter(properties);

    //    @Test
    void should_get_erc20_info_from_smart_contract() {
        assertThat(adapter.isValid(Ethereum.name("dfdufgdyfdytf.eth"))).isFalse();
        assertThat(adapter.isValid(Ethereum.name("vitalik.eth"))).isTrue();
    }
}