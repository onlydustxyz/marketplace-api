package onlydust.com.marketplace.project.domain.view.backoffice;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Currency;
import onlydust.com.marketplace.project.domain.model.UserPayoutSettings.SepaAccount;
import onlydust.com.marketplace.project.domain.model.bank.AccountNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentViewTest {
    private final Faker faker = new Faker();
    private final SepaAccount validSepaAccount =
            SepaAccount.builder().accountNumber(AccountNumber.of("FR1014508000702139488771C56")).bic(faker.finance().bic()).build();

    @Test
    void should_consider_payout_info_invalid_when_sepa_account_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.USD);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().bic(faker.finance().bic()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().accountNumber(AccountNumber.of(
                "FR1014508000702139488771C56")).build()).build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_invalid_when_wallet_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.USD);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_valid_when_all_data_are_valid() {
        assertTrue(PaymentView.builder()
                .currency(Currency.USD)
                .recipientSepaAccount(validSepaAccount)
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.USDC)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());


        assertTrue(PaymentView.builder()
                .currency(Currency.ETH)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.OP)
                .recipientOptimismWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.STRK)
                .recipientStarkWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.APT)
                .recipientAptosWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.LORDS)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());
    }

    @Test
    void should_return_formatted_payout_settings() {
        assertThat(PaymentView.builder()
                .currency(Currency.USD)
                .recipientSepaAccount(validSepaAccount)
                .build()
                .recipientPayoutSettings()).isEqualTo(validSepaAccount.getAccountNumber().asString() + " / " + validSepaAccount.getBic());

        assertThat(PaymentView.builder()
                .currency(Currency.USDC)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.ETH)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.LORDS)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.OP)
                .recipientOptimismWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.STRK)
                .recipientStarkWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.APT)
                .recipientAptosWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");
    }

}