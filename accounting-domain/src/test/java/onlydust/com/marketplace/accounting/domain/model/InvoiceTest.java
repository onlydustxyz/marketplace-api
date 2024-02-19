package onlydust.com.marketplace.accounting.domain.model;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

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
        final Invoice invoice = Invoice.of(BillingProfile.Id.random(), 1, new Invoice.PersonalInfo("John", "Doe", "123 Main St"))
                .rewards(List.of(
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

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
    class GivenACompanyNotApplicableToVatRegulation {
        final Invoice invoice = Invoice.of(BillingProfile.Id.random(), 1,
                        new Invoice.CompanyInfo("0123456789", "OnlyDust", "123 Main St", false, null))
                .rewards(List.of(
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-001");
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
            assertThat(invoice.companyInfo().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.NOT_APPLICABLE_NON_UE);
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
    class GivenAVatRegulatedCompany {
        final Invoice invoice = Invoice.of(BillingProfile.Id.random(), 1,
                        new Invoice.CompanyInfo("0123456789", "OnlyDust", "123 Main St", true, "666"))
                .rewards(List.of(
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), ProjectId.random(), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

        @Test
        void should_compute_id() {
            assertThat(invoice.number().value()).isEqualTo("OD-ONLYDUST-001");
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
            assertThat(invoice.companyInfo().orElseThrow().vatRegulationState()).isEqualTo(Invoice.VatRegulationState.APPLICABLE);
        }

        @Test
        void should_compute_totals() {
            assertThat(invoice.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(invoice.taxRate()).isEqualTo(BigDecimal.valueOf(0.2));
            assertThat(invoice.totalTax()).isEqualTo(Money.of(BigDecimal.valueOf(1080.0), USD));
            assertThat(invoice.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(6480.0), USD));
        }
    }
}