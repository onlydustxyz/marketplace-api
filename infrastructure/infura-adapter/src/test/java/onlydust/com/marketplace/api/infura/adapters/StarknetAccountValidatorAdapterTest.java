package onlydust.com.marketplace.api.infura.adapters;

import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StarknetAccountValidatorAdapterTest {
    final StarknetAccountValidatorAdapter adapter = new StarknetAccountValidatorAdapter();

    @ParameterizedTest
    @CsvSource({
            "0x048fa41160CF328a766fd6528915aA5836bE1a1548b6Da62201ba1c7175736bA",
            "0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905"
    })
    void should_accept_checksumed_addresses(String address) {
        assertThat(adapter.isValid(StarkNet.accountAddress(address))).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "0x048Fa41160CF328a766fd6528915aA5836bE1a1548b6Da62201ba1c7175736bA",
            "0x0688b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905"
    })
    void should_reject_invalid_checksumed_addresses(String address) {
        assertThat(adapter.isValid(StarkNet.accountAddress(address))).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "0x048fa41160cf328a766fd6528915aa5836be1a1548b6da62201ba1c7175736ba",
            "0x0688b45a11ee333293a1d4389430009529bc97d814233c2a5137c4f5ff949905"
    })
    void should_accept_non_checksumed_addresses(String address) {
        assertThat(adapter.isValid(StarkNet.accountAddress(address))).isTrue();
    }
}