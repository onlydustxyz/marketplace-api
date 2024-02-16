package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final Faker faker = new Faker();
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    final AccountingBillingProfileStorage billingProfileStorage = mock(AccountingBillingProfileStorage.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStorage);
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    final List<InvoicePreview.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());

    private @NonNull InvoicePreview.Reward fakeReward() {
        return new InvoicePreview.Reward(
                RewardId.random(),
                ZonedDateTime.now(),
                faker.lordOfTheRings().character(),
                Money.of(faker.number().randomNumber(1, true), ETH),
                Money.of(faker.number().randomNumber(4, true), USD));
    }

    final List<RewardId> rewardIds = rewards.stream().map(InvoicePreview.Reward::id).toList();
    final InvoicePreview invoicePreview = InvoicePreview.of(12,
            new InvoicePreview.PersonalInfo("John", "Doe", "12 rue de la paix, Paris")).rewards(rewards);

    @BeforeEach
    void setUp() {
        reset(invoiceStoragePort, billingProfileStorage);
    }

    @Test
    void should_prevent_invoice_generation_if_user_is_not_billing_profile_admin() {
        // Given
        when(billingProfileStorage.isAdmin(userId, billingProfileId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User is not allowed to generate invoice for this billing profile");

        verify(invoiceStoragePort, never()).preview(any(), any());
    }

    @Test
    void should_generate_invoice_if_user_is_billing_profile_admin() {
        // Given
        when(billingProfileStorage.isAdmin(userId, billingProfileId)).thenReturn(true);
        when(invoiceStoragePort.preview(billingProfileId, rewardIds)).thenReturn(invoicePreview);

        // When
        final var preview = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds);

        // Then
        assertThat(preview).isEqualTo(invoicePreview);
        verify(invoiceStoragePort).deleteDraftsOf(billingProfileId);

        final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceStoragePort).save(eq(billingProfileId), invoiceCaptor.capture());
        final var invoice = invoiceCaptor.getValue();
        assertThat(invoice.id()).isEqualTo(preview.id());
        assertThat(invoice.name()).isEqualTo(preview.name());
        assertThat(invoice.createdAt()).isEqualTo(preview.createdAt());
        assertThat(invoice.totalAfterTax()).isEqualTo(preview.totalAfterTax());
        assertThat(invoice.status()).isEqualTo(Invoice.Status.DRAFT);
        assertThat(invoice.rewards()).containsOnlyElementsOf(rewardIds);
    }

}