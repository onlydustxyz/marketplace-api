package onlydust.com.marketplace.api.domain.view.backoffice;

import com.github.javafaker.Faker;
import nl.garvelink.iban.IBAN;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Company;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Location;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.Person;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.SepaAccount;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView.Identity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentViewTest {
    private final Faker faker = new Faker();
    private final Location validLocation =
            Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).country(faker.address().country()).postalCode(faker.address().zipCode()).build();
    private final Identity validPerson = new Identity(null,
            Person.builder().firstName(faker.name().firstName()).lastName(faker.name().lastName()).build());
    private final Identity validCompany =
            new Identity(Company.builder().name(faker.company().name()).owner(validPerson.person()).identificationNumber(faker.idNumber().ssnValid()).build(), null);
    private final SepaAccount validSepaAccount =
            SepaAccount.builder().iban(IBAN.valueOf("FR1014508000702139488771C56")).bic(faker.finance().bic()).build();

    @Test
    void should_consider_payout_info_invalid_when_identity_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientSepaAccount(validSepaAccount);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(null, null)).build().recipientPayoutInfoValid());

        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().owner(validPerson.person()).identificationNumber(faker.idNumber().ssnValid()).build(), null)).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().name(faker.company().name()).identificationNumber(faker.idNumber().ssnValid()).build(), null)).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().name(faker.company().name()).owner(validPerson.person()).build(), null)).build().recipientPayoutInfoValid());

        assertFalse(paymentView.recipientIdentity(new Identity(null,
                Person.builder().lastName(faker.name().lastName()).build())).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(null,
                Person.builder().firstName(faker.name().firstName()).build())).build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_invalid_when_location_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientIdentity(validCompany)
                .recipientSepaAccount(validSepaAccount);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientLocation(Location.builder().city(faker.address().city()).country(faker.address().country()).postalCode(faker.address().zipCode()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientLocation(Location.builder().address(faker.address().fullAddress()).country(faker.address().country()).postalCode(faker.address().zipCode()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientLocation(Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).postalCode(faker.address().zipCode()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientLocation(Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).country(faker.address().country()).build()).build().recipientPayoutInfoValid());
    }


    @Test
    void should_consider_payout_info_invalid_when_sepa_account_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validCompany);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().bic(faker.finance().bic()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().iban(IBAN.valueOf(
                "FR1014508000702139488771C56")).build()).build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_invalid_when_wallet_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_valid_when_all_data_are_valid() {
        assertTrue(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validCompany)
                .recipientSepaAccount(validSepaAccount)
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validCompany)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());


        assertTrue(PaymentView.builder()
                .currency(Currency.Eth)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Op)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientOptimismWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Strk)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientStarkWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Apt)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientAptosWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Lords)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());
    }

    @Test
    void should_return_formatted_payout_settings() {
        assertThat(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientSepaAccount(validSepaAccount)
                .build()
                .recipientPayoutSettings()).isEqualTo(validSepaAccount.getIban().toPlainString() + " / " + validSepaAccount.getBic());

        assertThat(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.Eth)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.Lords)
                .recipientEthWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.Op)
                .recipientOptimismWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.Strk)
                .recipientStarkWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");

        assertThat(PaymentView.builder()
                .currency(Currency.Apt)
                .recipientAptosWallet("wallet")
                .build()
                .recipientPayoutSettings()).isEqualTo("wallet");
    }

}