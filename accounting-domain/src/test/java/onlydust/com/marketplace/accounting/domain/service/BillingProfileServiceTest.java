package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoicePreviewStoragePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final InvoicePreviewStoragePort invoicePreviewStoragePort = mock(InvoicePreviewStoragePort.class);
    final AccountingBillingProfileStorage billingProfileStorage = mock(AccountingBillingProfileStorage.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoicePreviewStoragePort, billingProfileStorage);
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final List<RewardId> rewardIds = List.of(RewardId.random(), RewardId.random(), RewardId.random());
    final InvoicePreview invoicePreview = InvoicePreview.of(12,
            new InvoicePreview.PersonalInfo("John", "Doe", "12 rue de la paix, Paris"));

    @BeforeEach
    void setUp() {
        reset(invoicePreviewStoragePort, billingProfileStorage);
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

        verify(invoicePreviewStoragePort, never()).generate(any(), any());
    }

    @Test
    void should_generate_invoice_if_user_is_billing_profile_admin() {
        // Given
        when(billingProfileStorage.isAdmin(userId, billingProfileId)).thenReturn(true);
        when(invoicePreviewStoragePort.generate(billingProfileId, rewardIds)).thenReturn(invoicePreview);

        // When
        final var preview = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds);

        // Then
        assertThat(preview).isEqualTo(invoicePreview);
    }

}