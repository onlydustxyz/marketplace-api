package onlydust.com.marketplace.accounting.domain.model;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.fillKyc;
import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {
    private final Faker faker = new Faker();
    private final Currency ETH = Currencies.ETH;
    private final Currency USD = Currencies.USD;

    @Test
    void should_compute_id() {
        assertThat(Invoice.Number.of(1, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-001");
        assertThat(Invoice.Number.of(2, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-002");
        assertThat(Invoice.Number.of(1, "A peu près", "Jean-Michel").value()).isEqualTo("OD-APEUPRES-JEANMICHEL-001");
        assertThat(Invoice.Number.of(1, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-001");
        assertThat(Invoice.Number.of(123456, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-123456");
        assertThat(Invoice.Number.of(1, "Caisse d'Épargne").value()).isEqualTo("OD-CAISSEDEPARGNE-001");
    }


    @Nested
    class GivenAnIndividual {

        IndividualBillingProfile individualBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            individualBillingProfile = new IndividualBillingProfile("John Doe", UserId.random());
            fillKyc(individualBillingProfile.kyc());
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            invoice = Invoice.of(individualBillingProfile, payoutInfo, 1)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null)
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-DOE-JOHN-001");
        }

        @Test
        void should_compute_due_date() {
            assertThat(invoice.dueAt()).isAfter(invoice.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(invoice.billingProfileType()).isEqualTo(BillingProfile.Type.INDIVIDUAL);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }

    @Nested
    class GivenACompanyOutsideOfEurope {

        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            companyBillingProfile = new CompanyBillingProfile("Foo Inc.", UserId.random());
            final var kyb = companyBillingProfile.kyb();
            kyb.setCountry(Country.fromIso3("USA"));
            kyb.setRegistrationNumber("123456789");
            kyb.setEuVATNumber(null);
            kyb.setRegistrationDate(new Date());
            kyb.setAddress("1 rue de la paix");
            kyb.setName("OnlyDust SAS");
            kyb.setSubjectToEuropeVAT(false);
            kyb.setStatus(VerificationStatus.VERIFIED);
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            invoice = Invoice.of(companyBillingProfile, payoutInfo, 1)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null)
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUSTSAS-001");
        }

        @Test
        void should_compute_due_date() {
            assertThat(invoice.dueAt()).isAfter(invoice.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(invoice.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_vat_regulation_state() {
            assertThat(invoice.billingProfileSnapshot().kybSnapshot().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.NOT_APPLICABLE_NON_UE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }

    @Nested
    class GivenAFrenchCompanySubjectToVAT {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            companyBillingProfile = new CompanyBillingProfile("OnlyDust", UserId.random());
            final var kyb = companyBillingProfile.kyb();
            kyb.setCountry(Country.fromIso3("FRA"));
            kyb.setRegistrationNumber("123456789");
            kyb.setEuVATNumber(null);
            kyb.setRegistrationDate(new Date());
            kyb.setAddress("1 rue de la paix");
            kyb.setName("OnlyDust SAS");
            kyb.setSubjectToEuropeVAT(true);
            kyb.setStatus(VerificationStatus.VERIFIED);
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            invoice = Invoice.of(companyBillingProfile, payoutInfo, 1)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null)
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUSTSAS-001");
        }


        @Test
        void should_compute_due_date() {
            assertThat(invoice.dueAt()).isAfter(invoice.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(invoice.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_vat_regulation_state() {
            assertThat(invoice.billingProfileSnapshot().kybSnapshot().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.APPLICABLE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.valueOf(0.2));
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.valueOf(1080.0), USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(6480.0), USD));
        }
    }

    @Nested
    class GivenAFrenchCompanyNonSubjectToVAT {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            companyBillingProfile = new CompanyBillingProfile("OnlyDust", UserId.random());
            final var kyb = companyBillingProfile.kyb();
            kyb.setCountry(Country.fromIso3("FRA"));
            kyb.setRegistrationNumber("123456789");
            kyb.setEuVATNumber(null);
            kyb.setRegistrationDate(new Date());
            kyb.setAddress("1 rue de la paix");
            kyb.setName("OnlyDust SAS");
            kyb.setSubjectToEuropeVAT(false);
            kyb.setStatus(VerificationStatus.VERIFIED);
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            invoice = Invoice.of(companyBillingProfile, payoutInfo, 1)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null)
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUSTSAS-001");
        }


        @Test
        void should_compute_due_date() {
            assertThat(invoice.dueAt()).isAfter(invoice.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(invoice.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_vat_regulation_state() {
            assertThat(invoice.billingProfileSnapshot().kybSnapshot().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.valueOf(0));
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }

    @Nested
    class GivenANonFrenchEuropeanCompany {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            companyBillingProfile = new CompanyBillingProfile("OnlyDust", UserId.random());
            final var kyb = companyBillingProfile.kyb();
            kyb.setCountry(Country.fromIso3("DEU"));
            kyb.setRegistrationNumber("123456789");
            kyb.setEuVATNumber("029834980");
            kyb.setRegistrationDate(new Date());
            kyb.setAddress("1 rue de la paix");
            kyb.setName("OnlyDust SAS");
            kyb.setSubjectToEuropeVAT(true);
            kyb.setStatus(VerificationStatus.VERIFIED);
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            invoice = Invoice.of(companyBillingProfile, payoutInfo, 1)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null)
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUSTSAS-001");
        }


        @Test
        void should_compute_due_date() {
            assertThat(invoice.dueAt()).isAfter(invoice.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(invoice.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_vat_regulation_state() {
            assertThat(invoice.billingProfileSnapshot().kybSnapshot().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.REVERSE_CHARGE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }
}