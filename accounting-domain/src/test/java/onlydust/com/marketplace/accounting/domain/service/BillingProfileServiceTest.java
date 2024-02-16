package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final Faker faker = new Faker();
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    final AccountingBillingProfileStorage billingProfileStorage = mock(AccountingBillingProfileStorage.class);
    final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStorage, imageStoragePort);
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    final List<InvoicePreview.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());
    final List<RewardId> rewardIds = rewards.stream().map(InvoicePreview.Reward::id).toList();
    final InvoicePreview invoicePreview = InvoicePreview.of(12,
            new InvoicePreview.PersonalInfo("John", "Doe", "12 rue de la paix, Paris")).rewards(rewards);
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

    @BeforeEach
    void setUp() {
        reset(invoiceStoragePort, billingProfileStorage, imageStoragePort);
    }

    @Nested
    class GivenCallerIsNotTheBillingProfileAdmin {
        @BeforeEach
        void setup() {
            when(billingProfileStorage.isAdmin(userId, billingProfileId)).thenReturn(false);
        }

        @Test
        void should_prevent_invoice_generation() {
            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to generate invoice for this billing profile");

            verify(invoiceStoragePort, never()).preview(any(), any());
        }

        @Test
        void should_prevent_invoice_upload() {
            // When
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, billingProfileId, invoicePreview.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to upload an invoice for this billing profile");

            verify(invoiceStoragePort, never()).preview(any(), any());
        }
    }

    @Nested
    class GivenUserIsBillingProfileAdmin {
        @BeforeEach
        void setup() {
            when(billingProfileStorage.isAdmin(userId, billingProfileId)).thenReturn(true);
        }

        @Test
        void should_generate_invoice_preview() {
            // Given
            when(invoiceStoragePort.preview(billingProfileId, rewardIds)).thenReturn(invoicePreview);

            // When
            final var preview = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds);

            // Then
            assertThat(preview).isEqualTo(invoicePreview);
            verify(invoiceStoragePort).deleteDraftsOf(billingProfileId);

            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).save(invoiceCaptor.capture());
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.id()).isEqualTo(preview.id());
            assertThat(invoice.billingProfileId()).isEqualTo(billingProfileId);
            assertThat(invoice.name()).isEqualTo(preview.name());
            assertThat(invoice.createdAt()).isEqualTo(preview.createdAt());
            assertThat(invoice.totalAfterTax()).isEqualTo(preview.totalAfterTax());
            assertThat(invoice.status()).isEqualTo(Invoice.Status.DRAFT);
            assertThat(invoice.rewards()).containsOnlyElementsOf(rewardIds);
        }

        @Test
        void should_prevent_invoice_upload_if_not_found() {
            // Given
            when(invoiceStoragePort.get(invoicePreview.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, billingProfileId, invoicePreview.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoicePreview.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            when(invoiceStoragePort.get(invoicePreview.id())).thenReturn(Optional.of(Invoice.of(BillingProfile.Id.random(), invoicePreview)));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, billingProfileId, invoicePreview.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoicePreview.id(), billingProfileId));
        }

        @SneakyThrows
        @Test
        void should_upload_invoice_and_save_url() {
            // Given
            final var url = new URL("https://" + faker.internet().url());
            when(invoiceStoragePort.get(invoicePreview.id())).thenReturn(Optional.of(Invoice.of(billingProfileId, invoicePreview)));
            when(imageStoragePort.storeImage(pdf)).thenReturn(url);

            // When
            billingProfileService.uploadInvoice(userId, billingProfileId, invoicePreview.id(), pdf);

            // Then
            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).save(invoiceCaptor.capture());
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.url()).isEqualTo(url);
        }
    }

    private @NonNull InvoicePreview.Reward fakeReward() {
        return new InvoicePreview.Reward(
                RewardId.random(),
                ZonedDateTime.now(),
                faker.lordOfTheRings().character(),
                Money.of(faker.number().randomNumber(1, true), ETH),
                Money.of(faker.number().randomNumber(4, true), USD));
    }
}