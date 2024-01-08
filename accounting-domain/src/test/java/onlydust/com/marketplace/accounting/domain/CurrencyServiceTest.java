package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.junit.jupiter.api.Test;

public class CurrencyServiceTest {
    final CurrencyService currencyService = new CurrencyService();

    @Test
    void given_a_blockchain_evm_compatible_should_add_erc20_support() {
        // Given
        final var lordsAddress = Ethereum.contractAddress("0x686f2404e77Ab0d9070a46cdfb0B7feCDD2318b0");

        // When
        currencyService.addERC20Support(Blockchain.ETHEREUM, lordsAddress);

        // Then
    }
}
