package onlydust.com.marketplace.accounting.domain.model;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.TotalMoneyView;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
import static org.assertj.core.api.Assertions.assertThat;

class InvoiceTest {
    private final Faker faker = new Faker();
    private final Currency ETH = Currencies.ETH;
    private final Currency STRK = Currencies.STRK;
    private final Currency OP = Currencies.OP;
    private final Currency USD = Currencies.USD;

    @Test
    void should_compute_id() {
        assertThat(Invoice.Number.of(1, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-001");
        assertThat(Invoice.Number.of(1, "Doe", null).value()).isEqualTo("OD-DOE-001");
        assertThat(Invoice.Number.of(1, null, "Doe").value()).isEqualTo("OD-DOE-001");
        assertThat(Invoice.Number.of(2, "Doe", "John").value()).isEqualTo("OD-DOE-JOHN-002");
        assertThat(Invoice.Number.of(1, "A peu près", "Jean-Michel").value()).isEqualTo("OD-A-PEU-PRÈS-JEAN-MICHEL-001");
        assertThat(Invoice.Number.of(1, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-001");
        assertThat(Invoice.Number.of(123456, "OnlyDust").value()).isEqualTo("OD-ONLYDUST-123456");
        assertThat(Invoice.Number.of(1, "Caisse d'Épargne").value()).isEqualTo("OD-CAISSE-D-ÉPARGNE-001");
        assertThat(Invoice.Number.of(2, "Doe", "Köseoğlu").value()).isEqualTo("OD-DOE-KÖSEOĞLU-002");
        assertThat(Invoice.Number.of(1, "婷", "陈").value()).isEqualTo("OD-婷-陈-001");
        assertThat(Invoice.Number.of(1, "Ömer", "Aydın").value()).isEqualTo("OD-ÖMER-AYDIN-001");
    }

    @Nested
    class GivenAnIndividual {
        IndividualBillingProfile individualBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            individualBillingProfile = IndividualBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("John")
                    .kyc(newKyc(billingProfileId, UserId.random()))
                    .enabled(true)
                    .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                    .build();

            invoice = Invoice.of(individualBillingProfile, 1, UserId.random(), payoutInfo)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.TEN, ETH), Money.of(27012L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.valueOf(100), STRK), Money.of(1900L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.valueOf(10), OP), Money.of(BigDecimal.valueOf(31.5), USD), null, List.of())
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
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(31643.5), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualToComparingFieldByField(Money.of(BigDecimal.valueOf(0.0), USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(31643.5), USD));

            assertThat(invoice.totals()).containsExactly(
                    new TotalMoneyView(BigDecimal.valueOf(11), ETH.toView(), BigDecimal.valueOf(29712L)),
                    new TotalMoneyView(BigDecimal.valueOf(10), OP.toView(), BigDecimal.valueOf(31.5)),
                    new TotalMoneyView(BigDecimal.valueOf(100), STRK.toView(), BigDecimal.valueOf(1900L))
            );
        }
    }

    @Nested
    class GivenACompanyOutsideOfEurope {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var billingProfileId = BillingProfile.Id.random();
            final var kyb = Kyb.builder()
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .billingProfileId(billingProfileId)
                    .country(Country.fromIso3("USA"))
                    .registrationNumber("123456789")
                    .euVATNumber(null)
                    .registrationDate(new Date())
                    .address("1 rue de la paix")
                    .name("OnlyDust SAS")
                    .subjectToEuropeVAT(false)
                    .usEntity(true)
                    .status(VerificationStatus.VERIFIED)
                    .build();
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(kyb)
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of())
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-SAS-001");
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
            assertThat(invoice.billingProfileSnapshot().kyb().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.NOT_APPLICABLE_NON_UE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));

            assertThat(invoice.totals()).containsExactly(
                    new TotalMoneyView(BigDecimal.valueOf(2), ETH.toView(), BigDecimal.valueOf(5400))
            );
        }
    }

    @Nested
    class GivenAFrenchCompanySubjectToVAT {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var billingProfileId = BillingProfile.Id.random();
            final var kyb = Kyb.builder()
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .billingProfileId(billingProfileId)
                    .country(Country.fromIso3("FRA"))
                    .registrationNumber("123456789")
                    .euVATNumber(null)
                    .registrationDate(new Date())
                    .address("1 rue de la paix")
                    .name("OnlyDust SAS")
                    .subjectToEuropeVAT(true)
                    .usEntity(false)
                    .status(VerificationStatus.VERIFIED)
                    .build();
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(kyb)
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of())
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-SAS-001");
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
            assertThat(invoice.billingProfileSnapshot().kyb().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.APPLICABLE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.valueOf(0.2));
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.valueOf(1080.0), USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(6480.0), USD));

            assertThat(invoice.totals()).containsExactly(
                    new TotalMoneyView(BigDecimal.valueOf(2), ETH.toView(), BigDecimal.valueOf(5400))
            );
        }
    }

    @Nested
    class GivenAFrenchCompanyNonSubjectToVAT {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var billingProfileId = BillingProfile.Id.random();
            final var kyb = Kyb.builder()
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .billingProfileId(billingProfileId)
                    .country(Country.fromIso3("FRA"))
                    .registrationNumber("123456789")
                    .euVATNumber(null)
                    .registrationDate(new Date())
                    .address("1 rue de la paix")
                    .name("OnlyDust SAS")
                    .subjectToEuropeVAT(false)
                    .usEntity(false)
                    .status(VerificationStatus.VERIFIED)
                    .build();
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(kyb)
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of())
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-SAS-001");
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
            assertThat(invoice.billingProfileSnapshot().kyb().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.valueOf(0));
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));

            assertThat(invoice.totals()).containsExactly(
                    new TotalMoneyView(BigDecimal.valueOf(2), ETH.toView(), BigDecimal.valueOf(5400))
            );
        }
    }

    @Nested
    class GivenANonFrenchEuropeanCompany {
        CompanyBillingProfile companyBillingProfile;
        PayoutInfo payoutInfo;
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var billingProfileId = BillingProfile.Id.random();
            final var kyb = Kyb.builder()
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .billingProfileId(billingProfileId)
                    .country(Country.fromIso3("DEU"))
                    .registrationNumber("123456789")
                    .euVATNumber("029834980")
                    .registrationDate(new Date())
                    .address("1 rue de la paix")
                    .name("OnlyDust SAS")
                    .subjectToEuropeVAT(true)
                    .usEntity(false)
                    .status(VerificationStatus.VERIFIED)
                    .build();
            payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(kyb)
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo)
                    .rewards(List.of(
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of()),
                            new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                    Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), null, List.of())
                    ));
        }

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-SAS-001");
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
            assertThat(invoice.billingProfileSnapshot().kyb().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.REVERSE_CHARGE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));

            assertThat(invoice.totals()).containsExactly(
                    new TotalMoneyView(BigDecimal.valueOf(2), ETH.toView(), BigDecimal.valueOf(5400))
            );
        }
    }
}