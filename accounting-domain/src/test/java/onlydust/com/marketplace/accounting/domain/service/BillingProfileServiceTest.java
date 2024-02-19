package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final Faker faker = new Faker();
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    final AccountingBillingProfileStorage billingProfileStorage = mock(AccountingBillingProfileStorage.class);
    final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStorage, pdfStoragePort);
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    final List<Invoice.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());
    final List<RewardId> rewardIds = rewards.stream().map(Invoice.Reward::id).toList();
    final Invoice invoice = Invoice.of(billingProfileId, 12,
            new Invoice.PersonalInfo("John", "Doe", "12 rue de la paix, Paris")).rewards(rewards);
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

    @BeforeEach
    void setUp() {
        reset(invoiceStoragePort, billingProfileStorage, pdfStoragePort);
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
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to upload an invoice for this billing profile");

            verify(invoiceStoragePort, never()).preview(any(), any());
        }

        @Test
        void should_prevent_listing_invoices() {
            // When
            assertThatThrownBy(() -> billingProfileService.invoicesOf(userId, billingProfileId, 1, 10))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to view invoices for this billing profile");
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
            when(invoiceStoragePort.preview(billingProfileId, rewardIds)).thenReturn(invoice);

            // When
            final var preview = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds);

            // Then
            assertThat(preview).isEqualTo(invoice);
            verify(invoiceStoragePort).deleteDraftsOf(billingProfileId);

            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).save(invoiceCaptor.capture());
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.id()).isEqualTo(preview.id());
            assertThat(invoice.billingProfileId()).isEqualTo(billingProfileId);
            assertThat(invoice.number()).isEqualTo(preview.number());
            assertThat(invoice.createdAt()).isEqualTo(preview.createdAt());
            assertThat(invoice.totalAfterTax()).isEqualTo(preview.totalAfterTax());
            assertThat(invoice.status()).isEqualTo(Invoice.Status.DRAFT);
            assertThat(invoice.rewards()).containsExactlyElementsOf(rewards);
        }

        @Test
        void should_prevent_invoice_upload_if_not_found() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStorage.isAdmin(userId, otherBillingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadInvoice(userId, otherBillingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @SneakyThrows
        @Test
        void should_upload_invoice_and_save_url() {
            // Given
            final var url = new URL("https://" + faker.internet().url());
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(pdfStoragePort.upload("OD-DOE-JOHN-012.pdf", pdf)).thenReturn(url);

            // When
            billingProfileService.uploadInvoice(userId, billingProfileId, invoice.id(), pdf);

            // Then
            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).save(invoiceCaptor.capture());
            verify(pdfStoragePort).upload("OD-DOE-JOHN-012.pdf", pdf);
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.url()).isEqualTo(url);
        }

        @SneakyThrows
        @Test
        void should_list_invoices() {
            // Given
            when(invoiceStoragePort.invoicesOf(billingProfileId, 1, 10))
                    .thenReturn(Page.<Invoice>builder().content(List.of(invoice)).totalItemNumber(1).totalPageNumber(1).build());

            // When
            final var invoices = billingProfileService.invoicesOf(userId, billingProfileId, 1, 10);

            // Then
            assertThat(invoices.getTotalItemNumber()).isEqualTo(1);
            assertThat(invoices.getTotalPageNumber()).isEqualTo(1);
            assertThat(invoices.getContent()).hasSize(1);
            assertThat(invoices.getContent().get(0).id()).isEqualTo(invoice.id());
            assertThat(invoices.getContent().get(0).billingProfileId()).isEqualTo(billingProfileId);
        }
    }

    private @NonNull Invoice.Reward fakeReward() {
        return new Invoice.Reward(
                RewardId.random(),
                ZonedDateTime.now(),
                ProjectId.random(),
                faker.lordOfTheRings().character(),
                Money.of(faker.number().randomNumber(1, true), ETH),
                Money.of(faker.number().randomNumber(4, true), USD)
        );
    }
    @Test
    void should_create_individual_billing_profile() {
        // Given
        final var name = "John Doe";

        // When
        final var billingProfile = billingProfileService.createIndividualBillingProfile(userId, name, null);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.INDIVIDUAL);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
    }

    @Test
    void should_create_individual_billing_profile_with_select_for_projects() {
        // Given
        final var name = "John Doe";
        final var selectForProjects = Set.of(ProjectId.random());

        // When
        final var billingProfile = billingProfileService.createIndividualBillingProfile(userId, name, selectForProjects);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.INDIVIDUAL);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
    }

    @Test
    void should_create_self_employed_billing_profile() {
        // Given
        final var name = "John Doe";

        // When
        final var billingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, name, null);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.COMPANY);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
    }

    @Test
    void should_create_self_employed_billing_profile_with_select_for_projects() {
        // Given
        final var name = "John Doe";
        final var selectForProjects = Set.of(ProjectId.random());

        // When
        final var billingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, name, selectForProjects);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.COMPANY);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
    }

    @Test
    void should_create_company_billing_profile() {
        // Given
        final var name = "John Doe";

        // When
        final var billingProfile = billingProfileService.createCompanyBillingProfile(userId, name, null);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.COMPANY);
        assertThat(billingProfile.members()).containsExactlyInAnyOrder(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN));
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
    }

    @Test
    void should_create_company_billing_profile_with_select_for_projects() {
        // Given
        final var name = "John Doe";
        final var selectForProjects = Set.of(ProjectId.random());

        // When
        final var billingProfile = billingProfileService.createCompanyBillingProfile(userId, name, selectForProjects);

        // Then
        assertThat(billingProfile.id()).isNotNull();
        assertThat(billingProfile.name()).isEqualTo(name);
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.COMPANY);
        assertThat(billingProfile.members()).containsExactlyInAnyOrder(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN));
        verify(billingProfileStorage).save(billingProfile);
        verify(billingProfileStorage).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
    }

}