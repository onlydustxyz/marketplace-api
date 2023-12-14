package onlydust.com.marketplace.api.domain.view.backoffice;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation.*;
import onlydust.com.marketplace.api.domain.view.backoffice.PaymentView.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class PaymentViewTest {
    private final Faker faker = new Faker();
    private final Location validLocation =
            Location.builder().address(faker.address().fullAddress()).city(faker.address().city()).country(faker.address().country()).postalCode(faker.address().zipCode()).build();
    private final Identity validPerson = new Identity(null,
            Person.builder().firstName(faker.name().firstName()).lastName(faker.name().lastName()).build());
    private final Identity validCompany =
            new Identity(Company.builder().name(faker.company().name()).owner(validPerson.person()).identificationNumber(faker.idNumber().ssnValid()).build(), null);
    private final SepaAccount validSepaAccount = SepaAccount.builder().iban(faker.finance().iban()).bic(faker.finance().bic()).build();

    @Test
    void should_consider_payout_info_invalid_when_identity_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.FIAT)
                .recipientSepaAccount(validSepaAccount);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(null, null)).build().recipientPayoutInfoValid());

        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().owner(validPerson.person()).identificationNumber(faker.idNumber().ssnValid()).build(), null)).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().name(faker.company().name()).identificationNumber(faker.idNumber().ssnValid()).build(), null)).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(Company.builder().name(faker.company().name()).owner(validPerson.person()).build(), null)).build().recipientPayoutInfoValid());

        assertFalse(paymentView.recipientIdentity(new Identity(null, Person.builder().lastName(faker.name().lastName()).build())).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientIdentity(new Identity(null, Person.builder().firstName(faker.name().firstName()).build())).build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_invalid_when_location_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientIdentity(validCompany)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.FIAT)
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
                .recipientIdentity(validCompany)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.FIAT);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().bic(faker.finance().bic()).build()).build().recipientPayoutInfoValid());
        assertFalse(paymentView.recipientSepaAccount(SepaAccount.builder().iban(faker.finance().iban()).build()).build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_invalid_when_wallet_is_invalid() {
        final var paymentView = PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO);

        assertFalse(paymentView.build().recipientPayoutInfoValid());
    }

    @Test
    void should_consider_payout_info_valid_when_all_data_are_valid() {
        assertTrue(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validCompany)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.FIAT)
                .recipientSepaAccount(validSepaAccount)
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Usd)
                .recipientLocation(validLocation)
                .recipientIdentity(validCompany)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());


        assertTrue(PaymentView.builder()
                .currency(Currency.Eth)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Op)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientOptimismWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Stark)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientStarkWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Apt)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientAptosWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());

        assertTrue(PaymentView.builder()
                .currency(Currency.Lords)
                .recipientLocation(validLocation)
                .recipientIdentity(validPerson)
                .recipientUsdPreferredMethod(UsdPreferredMethodEnum.CRYPTO)
                .recipientEthWallet("vitalik.eth")
                .build()
                .recipientPayoutInfoValid());
    }

}