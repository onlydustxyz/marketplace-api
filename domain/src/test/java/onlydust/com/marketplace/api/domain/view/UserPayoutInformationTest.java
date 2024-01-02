package onlydust.com.marketplace.api.domain.view;

import nl.garvelink.iban.IBAN;
import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPayoutInformationTest {

    public static UserPayoutInformation fakeValidUserPayoutInformation() {
        return fakeUserPayoutInformation(false, false, true, true, null, List.of());
    }

    public static UserPayoutInformation fakeUserPayoutInformation(
            boolean isACompany,
            boolean hasValidCompany,
            boolean hasValidPerson,
            boolean hasValidLocation,
            UserPayoutInformation.PayoutSettings payoutSettings,
            List<Currency> pendingPaymentsCurrencies
    ) {
        final var builder = UserPayoutInformation.builder()
                .isACompany(isACompany)
                .payoutSettings(payoutSettings)
                .pendingPaymentsCurrencies(pendingPaymentsCurrencies);
        if (hasValidCompany) {
            builder.company(UserPayoutInformation.Company.builder()
                    .name("OnlyDust")
                    .identificationNumber("12345678A")
                    .owner(UserPayoutInformation.Person.builder()
                            .firstName("John")
                            .lastName("Doe")
                            .build())
                    .build());
        }
        if (hasValidPerson) {
            builder.person(UserPayoutInformation.Person.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build());
        }
        if (hasValidLocation) {
            builder.location(UserPayoutInformation.Location.builder()
                    .country("Spain")
                    .address("Calle Falsa 123")
                    .city("Madrid")
                    .postalCode("28001")
                    .build());
        }
        return builder.build();
    }

    @Test
    void should_be_valid_when_nothing_is_missing() {
        var userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertIsFullValid(userPayoutInformation);
    }

    @Test
    void should_not_be_valid_when_not_a_valid_company() {
        var userPayoutInformation = fakeUserPayoutInformation(
                true,
                false,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
    }

    @Test
    void should_not_be_valid_when_not_a_valid_person() {
        var userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
    }

    @Test
    void should_not_be_valid_when_not_a_valid_location() {
        var userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                false,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
    }

    @Test
    void should_not_be_valid_when_wallet_is_missing() {
        var userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Eth)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Usd)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingSepaAccount()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Strk)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingStarknetWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Lords)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Op)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingOptimismWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Apt)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingAptosWallet()).isTrue();

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .build(),
                List.of(Currency.Usdc)
        );
        assertThat(userPayoutInformation.isValid()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isTrue();
    }


    @Test
    void should_be_always_valid_when_no_pending_payments() {
        var userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                false,
                false,
                null,
                List.of()
        );
        assertThat(userPayoutInformation.isValid()).isTrue();
        assertThat(userPayoutInformation.hasValidContactInfo()).isFalse();
        assertThat(userPayoutInformation.hasValidPayoutSettings()).isFalse();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingStarknetWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingOptimismWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingAptosWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingSepaAccount()).isFalse();
    }

    @Test
    void should_be_valid() {
        var userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                .iban(IBAN.valueOf("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .build(),
                List.of(Currency.Usd)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .starknetAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Strk)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Eth)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethName("vitalik.eth")
                        .build(),
                List.of(Currency.Eth)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Lords)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethName("vitalik.eth")
                        .build(),
                List.of(Currency.Lords)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Usdc)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .ethName("vitalik.eth")
                        .build(),
                List.of(Currency.Usdc)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                true,
                true,
                false,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                .iban(IBAN.valueOf("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .starknetAddress("0x1234567890123456789012345678901234567890")
                        .aptosAddress("0x1234567890123456789012345678901234567890")
                        .optimismAddress("0x1234567890123456789012345678901234567890")
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Usd, Currency.Strk, Currency.Eth, Currency.Op, Currency.Apt, Currency.Lords,
                        Currency.Usdc)
        );
        assertIsFullValid(userPayoutInformation);

        userPayoutInformation = fakeUserPayoutInformation(
                false,
                false,
                true,
                true,
                UserPayoutInformation.PayoutSettings.builder()
                        .sepaAccount(UserPayoutInformation.SepaAccount.builder()
                                .iban(IBAN.valueOf("ES6621000418401234567891"))
                                .bic("CAIXESBBXXX")
                                .build())
                        .starknetAddress("0x1234567890123456789012345678901234567890")
                        .aptosAddress("0x1234567890123456789012345678901234567890")
                        .optimismAddress("0x1234567890123456789012345678901234567890")
                        .ethAddress("0x1234567890123456789012345678901234567890")
                        .build(),
                List.of(Currency.Usd, Currency.Strk, Currency.Eth, Currency.Op, Currency.Apt, Currency.Lords,
                        Currency.Usdc)
        );
        assertIsFullValid(userPayoutInformation);
    }

    private static void assertIsFullValid(UserPayoutInformation userPayoutInformation) {
        assertThat(userPayoutInformation.isValid()).isTrue();
        assertThat(userPayoutInformation.hasValidContactInfo()).isTrue();
        assertThat(userPayoutInformation.hasValidPayoutSettings()).isTrue();
        assertThat(userPayoutInformation.isMissingEthereumWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingStarknetWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingOptimismWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingAptosWallet()).isFalse();
        assertThat(userPayoutInformation.isMissingSepaAccount()).isFalse();
    }
}
