package onlydust.com.marketplace.accounting.domain.view;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicePreviewTest {
    private final Faker faker = new Faker();
    private final Currency ETH = Currencies.ETH;
    private final Currency USD = Currencies.USD;

    @Nested
    class GivenAnIndividual {
        final InvoicePreview preview = InvoicePreview.of(1, new InvoicePreview.PersonalInfo("John", "Doe", "123 Main St"))
                .rewards(List.of(
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

        @Test
        void should_compute_id() {
            assertThat(preview.id().value()).isEqualTo("OD-DOE-JOHN-001");
        }

        @Test
        void should_compute_due_date() {
            assertThat(preview.dueAt()).isAfter(preview.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(preview.billingProfileType()).isEqualTo(BillingProfile.Type.INDIVIDUAL);
        }

        @Test
        void should_compute_totals() {
            assertThat(preview.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(preview.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(preview.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(preview.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }

    @Nested
    class GivenACompanyNotApplicableToVatRegulation {
        final InvoicePreview preview = InvoicePreview.of(1,
                        new InvoicePreview.CompanyInfo("0123456789", "OnlyDust", "123 Main St", InvoicePreview.VatRegulationState.NOT_APPLICABLE_NON_UE, "666"))
                .rewards(List.of(
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

        @Test
        void should_compute_id() {
            assertThat(preview.id().value()).isEqualTo("OD-ONLYDUST-001");
        }
        
        @Test
        void should_compute_due_date() {
            assertThat(preview.dueAt()).isAfter(preview.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(preview.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_totals() {
            assertThat(preview.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(preview.taxRate()).isEqualTo(BigDecimal.ZERO);
            assertThat(preview.totalTax()).isEqualTo(Money.of(BigDecimal.ZERO, USD));
            assertThat(preview.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
        }
    }

    @Nested
    class GivenAVatRegulatedCompany {
        final InvoicePreview preview = InvoicePreview.of(1,
                        new InvoicePreview.CompanyInfo("0123456789", "OnlyDust", "123 Main St", InvoicePreview.VatRegulationState.APPLICABLE, "666"))
                .rewards(List.of(
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD)),
                        new InvoicePreview.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                                Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD))
                ));

        @Test
        void should_compute_id() {
            assertThat(preview.id().value()).isEqualTo("OD-ONLYDUST-001");
        }


        @Test
        void should_compute_due_date() {
            assertThat(preview.dueAt()).isAfter(preview.createdAt());
        }

        @Test
        void should_compute_billing_profile_type() {
            assertThat(preview.billingProfileType()).isEqualTo(BillingProfile.Type.COMPANY);
        }

        @Test
        void should_compute_totals() {
            assertThat(preview.totalBeforeTax()).isEqualTo(Money.of(BigDecimal.valueOf(5400), USD));
            assertThat(preview.taxRate()).isEqualTo(BigDecimal.valueOf(0.2));
            assertThat(preview.totalTax()).isEqualTo(Money.of(BigDecimal.valueOf(1080.0), USD));
            assertThat(preview.totalAfterTax()).isEqualTo(Money.of(BigDecimal.valueOf(6480.0), USD));
        }
    }
}