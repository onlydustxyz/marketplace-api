package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPayoutSettingsTest {

    public static UserPayoutSettings fakeValidUserPayoutInformation() {
        return fakeUserPayoutInformation(null, List.of());
    }

    public static UserPayoutSettings fakeUserPayoutInformation(
            UserPayoutSettings payoutSettings,
            List<Currency> pendingPaymentsCurrencies
    ) {
        final var builder = Objects.isNull(payoutSettings) ? UserPayoutSettings.builder().pendingPaymentsCurrencies(pendingPaymentsCurrencies) :
                payoutSettings.toBuilder()
                        .pendingPaymentsCurrencies(pendingPaymentsCurrencies);
        return builder.build();
    }

    @Test
    void should_be_valid_when_nothing_is_missing() {
        var userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.ETH)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.ETH)
        );
        assertIsFullValid(userPayoutInformation);
    }

    @Test
    void should_not_be_valid_when_wallet_is_missing() {
        var userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.ETH)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.USD)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingSepaAccount()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.STRK)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingStarknetWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.LORDS)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.OP)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingOptimismWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.APT)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingAptosWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .build(),
                List.of(Currency.USDC)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();
    }


    @Test
    void should_be_always_valid_when_no_pending_payments() {
        var userPayoutInformation = fakeUserPayoutInformation(
                null,
                List.of()
        );
        assertThat(userPayoutInformation.isValid()).isTrue();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingStarknetWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingOptimismWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingAptosWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingSepaAccount()).isFalse();
    }

    @Test
    void should_be_valid() {
        var userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                                .accountNumber(AccountNumber.of("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .build(),
                List.of(Currency.USD)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .starknetAddress(StarkNet.accountAddress("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.STRK)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.ETH)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("vitalik.eth"))
                        .build(),
                List.of(Currency.ETH)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.LORDS)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("vitalik.eth"))
                        .build(),
                List.of(Currency.LORDS)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.USDC)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .ethWallet(Ethereum.wallet("vitalik.eth"))
                        .build(),
                List.of(Currency.USDC)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                                .accountNumber(AccountNumber.of("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .starknetAddress(StarkNet.accountAddress("0x1234567890123456789012345678901234567890"))
                        .aptosAddress(Aptos.accountAddress("0x1234567890123456789012345678901234567890"))
                        .optimismAddress(Optimism.accountAddress("0x1234567890123456789012345678901234567890"))
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.USD, Currency.STRK, Currency.ETH, Currency.OP, Currency.APT, Currency.LORDS,
                        Currency.USDC)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                UserPayoutSettings.builder()
                        .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                                .accountNumber(AccountNumber.of("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .starknetAddress(StarkNet.accountAddress("0x1234567890123456789012345678901234567890"))
                        .aptosAddress(Aptos.accountAddress("0x1234567890123456789012345678901234567890"))
                        .optimismAddress(Optimism.accountAddress("0x1234567890123456789012345678901234567890"))
                        .ethWallet(Ethereum.wallet("0x1234567890123456789012345678901234567890"))
                        .build(),
                List.of(Currency.USD, Currency.STRK, Currency.ETH, Currency.OP, Currency.APT, Currency.LORDS,
                        Currency.USDC)
        );
        assertIsFullValid(userPayoutInformation);
    }

    private static void assertIsFullValid(UserPayoutSettings userPayoutSettings) {
        assertThat(userPayoutSettings.isValid()).isTrue();
        assertThat(userPayoutSettings.hasValidPayoutSettings()).isTrue();
        assertThat(userPayoutSettings.isMissingEthereumWallet()).isFalse();
        assertThat(userPayoutSettings.isMissingStarknetWallet()).isFalse();
        assertThat(userPayoutSettings.isMissingOptimismWallet()).isFalse();
        assertThat(userPayoutSettings.isMissingAptosWallet()).isFalse();
        assertThat(userPayoutSettings.isMissingSepaAccount()).isFalse();
    }
}
