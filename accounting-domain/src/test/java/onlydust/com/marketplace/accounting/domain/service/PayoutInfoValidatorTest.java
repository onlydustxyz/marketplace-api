package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PayoutInfoValidatorTest {
    private final WalletValidator<Name> ensValidator = mock(WalletValidator.class);
    private final WalletValidator<StarknetAccountAddress> starknetAccountAddressWalletValidator = mock(WalletValidator.class);
    private final PayoutInfoValidator validator = new PayoutInfoValidator(ensValidator, starknetAccountAddressWalletValidator);
    private final Name ens = Ethereum.name("toto.eth");
    private final StarknetAccountAddress starknetAccountAddress = StarkNet.accountAddress("0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905");
    private final PayoutInfo payoutInfo = PayoutInfo.builder()
            .ethWallet(new WalletLocator(ens))
            .starknetAddress(starknetAccountAddress)
            .build();

    @BeforeEach
    void resetAll() {
        reset(ensValidator, starknetAccountAddressWalletValidator);
        
        when(ensValidator.isValid(any())).thenReturn(true);
        when(starknetAccountAddressWalletValidator.isValid(any())).thenReturn(true);
    }

    @Test
    void should_accept_valid_payout_info() {
        validator.validate(payoutInfo);
    }

    @Test
    void should_reject_invalid_ens() {
        // Given
        when(ensValidator.isValid(ens)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("toto.eth is not a valid ENS");
    }


    @Test
    void should_reject_invalid_starknet_account_address() {
        // Given
        when(starknetAccountAddressWalletValidator.isValid(starknetAccountAddress)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905 is not a valid StarkNet account address");
    }
}