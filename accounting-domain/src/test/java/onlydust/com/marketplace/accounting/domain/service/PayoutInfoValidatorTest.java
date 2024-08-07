package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.out.WalletValidator;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.Stellar;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PayoutInfoValidatorTest {
    private final WalletValidator<Name> ensValidator = mock(WalletValidator.class);
    private final WalletValidator<StarknetAccountAddress> starknetAccountAddressWalletValidator = mock(WalletValidator.class);
    private final WalletValidator<EvmAccountAddress> evmAccountAddressWalletValidator = mock(WalletValidator.class);
    private final WalletValidator<AptosAccountAddress> aptosAccountAddressWalletValidator = mock(WalletValidator.class);
    private final WalletValidator<StellarAccountId> stellarAccountIdValidator = mock(WalletValidator.class);
    private final PayoutInfoValidator validator = new PayoutInfoValidator(ensValidator, starknetAccountAddressWalletValidator,
            evmAccountAddressWalletValidator, aptosAccountAddressWalletValidator, stellarAccountIdValidator);
    private final Name ens = Ethereum.name("toto.eth");
    final EvmAccountAddress evmAccountAddress = Ethereum.accountAddress("0xdB3A62c5eE9886EdebFDe29D42731F59c8B30686");
    private final StarknetAccountAddress starknetAccountAddress = StarkNet.accountAddress("0x0788b45a11Ee333293a1d4389430009529bC97D814233C2A5137c4F5Ff949905");
    private final AptosAccountAddress aptosAccountAddress = Aptos.accountAddress("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73");
    private final StellarAccountId stellarAccountId = Stellar.accountId("GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5");
    private final PayoutInfo payoutInfo = PayoutInfo.builder()
            .ethWallet(new WalletLocator(ens))
            .optimismAddress(evmAccountAddress)
            .starknetAddress(starknetAccountAddress)
            .aptosAddress(aptosAccountAddress)
            .stellarAccountId(stellarAccountId)
            .build();

    @BeforeEach
    void resetAll() {
        reset(ensValidator,
                starknetAccountAddressWalletValidator,
                evmAccountAddressWalletValidator,
                aptosAccountAddressWalletValidator,
                stellarAccountIdValidator);

        when(ensValidator.isValid(any())).thenReturn(true);
        when(starknetAccountAddressWalletValidator.isValid(any())).thenReturn(true);
        when(evmAccountAddressWalletValidator.isValid(any())).thenReturn(true);
        when(aptosAccountAddressWalletValidator.isValid(any())).thenReturn(true);
        when(stellarAccountIdValidator.isValid(any())).thenReturn(true);
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

    @Test
    void should_reject_invalid_aptos_account_address() {
        // Given
        when(aptosAccountAddressWalletValidator.isValid(aptosAccountAddress)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("0xa35864ccdb3abcb64c144da4511c66457f743ee0ddf95c1b5bbfabaf67e6ac73 is not a valid Aptos account address");
    }

    @Test
    void should_reject_invalid_evm_account_address() {
        // Given
        final var payoutInfo = PayoutInfo.builder()
                .ethWallet(new WalletLocator(evmAccountAddress))
                .build();

        when(evmAccountAddressWalletValidator.isValid(evmAccountAddress)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("0xdB3A62c5eE9886EdebFDe29D42731F59c8B30686 is not a valid EVM account address");
    }

    @Test
    void should_reject_invalid_optimism_account_address() {
        // Given
        when(evmAccountAddressWalletValidator.isValid(evmAccountAddress)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("0xdB3A62c5eE9886EdebFDe29D42731F59c8B30686 is not a valid EVM account address");
    }


    @Test
    void should_reject_invalid_stellar_account_id() {
        // Given
        when(stellarAccountIdValidator.isValid(stellarAccountId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> validator.validate(payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5 is not a valid Stellar account id");
    }
}