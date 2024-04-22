package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PayoutInfoValidatorTest {
    private final WalletValidator<Name> ensValidator = mock(WalletValidator.class);
    private final PayoutInfoValidator validator = new PayoutInfoValidator(ensValidator);
    private final Name ens = Ethereum.name("toto.eth");
    private final PayoutInfo payoutInfo = PayoutInfo.builder()
            .ethWallet(new WalletLocator(ens))
            .build();

    @BeforeEach
    void resetAll() {
        reset(ensValidator);
    }

    @Test
    void should_accept_valid_payout_info() {
        // Given
        when(ensValidator.isValid(ens)).thenReturn(true);

        // When
        validator.validate(payoutInfo);

        // Then
        // No exception is thrown
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
}