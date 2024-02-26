package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.accounting.domain.model.Invoice.Status.APPROVED;
import static onlydust.com.marketplace.accounting.domain.model.Invoice.Status.TO_REVIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final Faker faker = new Faker();
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    final BillingProfileStoragePort billingProfileStoragePort = mock(BillingProfileStoragePort.class);
    final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);
    final BillingProfileObserver billingProfileObserver = mock(BillingProfileObserver.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort,
            billingProfileObserver);
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    final List<Invoice.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());
    final List<RewardId> rewardIds = rewards.stream().map(Invoice.Reward::id).toList();
    final Invoice invoice = Invoice.of(billingProfileId, 12,
            new Invoice.PersonalInfo("John", "Doe", "12 rue de la paix, Paris")).rewards(new ArrayList<>(rewards));
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

    @BeforeEach
    void setUp() {
        reset(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObserver);
    }

    @Nested
    class GivenCallerIsNotTheBillingProfileAdmin {
        @BeforeEach
        void setup() {
            when(billingProfileStoragePort.oldIsAdmin(userId, billingProfileId)).thenReturn(false);
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
            when(billingProfileStoragePort.isMandateAccepted(billingProfileId)).thenReturn(true);
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to upload an invoice for this billing profile");

            verify(invoiceStoragePort, never()).preview(any(), any());
        }

        @Test
        void should_prevent_listing_invoices() {
            // When
            assertThatThrownBy(() -> billingProfileService.invoicesOf(userId, billingProfileId, 1, 10, Invoice.Sort.STATUS, SortDirection.asc))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to view invoices for this billing profile");
        }

        @Test
        void should_prevent_invoice_download() {
            // When
            assertThatThrownBy(() -> billingProfileService.downloadInvoice(userId, billingProfileId, invoice.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s is not allowed to download invoice %s of billing profile %s".formatted(userId, invoice.id(), billingProfileId));
        }

    }

    @Nested
    class GivenUserIsBillingProfileAdmin {
        @BeforeEach
        void setup() {
            when(billingProfileStoragePort.oldIsAdmin(userId, billingProfileId)).thenReturn(true);
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
            verify(invoiceStoragePort).create(invoiceCaptor.capture());
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
        void should_prevent_invoice_preview_if_rewards_are_already_invoiced() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            invoice.rewards().add(reward);
            invoice.status(Invoice.Status.APPROVED);
            when(invoiceStoragePort.preview(billingProfileId, rewardIds)).thenReturn(invoice);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are already invoiced");
        }

        @Test
        void should_prevent_invoice_upload_if_not_found() {
            // Given
            when(billingProfileStoragePort.isMandateAccepted(billingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.oldIsAdmin(userId, otherBillingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.isMandateAccepted(otherBillingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, otherBillingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_mandate_is_not_accepted() {
            // Given
            when(billingProfileStoragePort.oldIsAdmin(userId, billingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.isMandateAccepted(billingProfileId)).thenReturn(false);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice mandate has not been accepted for billing profile %s".formatted(billingProfileId));
        }

        @SneakyThrows
        @Test
        void should_upload_invoice_and_save_url() {
            // Given
            when(billingProfileStoragePort.isMandateAccepted(billingProfileId)).thenReturn(true);
            final var url = new URL("https://" + faker.internet().url());
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(pdfStoragePort.upload(invoice.id() + ".pdf", pdf)).thenReturn(url);

            // When
            billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf);

            // Then
            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).update(invoiceCaptor.capture());
            verify(pdfStoragePort).upload(invoice.id() + ".pdf", pdf);
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.url()).isEqualTo(url);
            verify(billingProfileObserver).onInvoiceUploaded(billingProfileId, invoice.id(), false);
            assertThat(invoice.originalFileName()).isNull();
            assertThat(invoice.status()).isEqualTo(APPROVED);
        }

        @Test
        void should_prevent_external_invoice_upload_if_not_found() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_external_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.oldIsAdmin(userId, otherBillingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, otherBillingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @Test
        void should_prevent_external_invoice_upload_if_mandate_is_accepted() {
            // Given
            when(billingProfileStoragePort.oldIsAdmin(userId, billingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.isMandateAccepted(billingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("External invoice upload is forbidden when mandate has been accepted (billing profile %s)".formatted(billingProfileId));
        }

        @SneakyThrows
        @Test
        void should_upload_external_invoice_and_save_url() {
            // Given
            final var url = new URL("https://" + faker.internet().url());
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(pdfStoragePort.upload(invoice.id() + ".pdf", pdf)).thenReturn(url);

            // When
            billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf);

            // Then
            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).update(invoiceCaptor.capture());
            verify(pdfStoragePort).upload(invoice.id() + ".pdf", pdf);
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.url()).isEqualTo(url);
            verify(billingProfileObserver).onInvoiceUploaded(billingProfileId, invoice.id(), true);
            assertThat(invoice.originalFileName()).isEqualTo("foo.pdf");
            assertThat(invoice.status()).isEqualTo(TO_REVIEW);
        }

        @SneakyThrows
        @Test
        void should_list_invoices() {
            // Given
            when(invoiceStoragePort.invoicesOf(billingProfileId, 1, 10, Invoice.Sort.STATUS, SortDirection.asc))
                    .thenReturn(Page.<Invoice>builder().content(List.of(invoice)).totalItemNumber(1).totalPageNumber(1).build());

            // When
            final var invoices = billingProfileService.invoicesOf(userId, billingProfileId, 1, 10, Invoice.Sort.STATUS, SortDirection.asc);

            // Then
            assertThat(invoices.getTotalItemNumber()).isEqualTo(1);
            assertThat(invoices.getTotalPageNumber()).isEqualTo(1);
            assertThat(invoices.getContent()).hasSize(1);
            assertThat(invoices.getContent().get(0).id()).isEqualTo(invoice.id());
            assertThat(invoices.getContent().get(0).billingProfileId()).isEqualTo(billingProfileId);
        }

        @Test
        void should_prevent_invoice_download_if_not_found() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.downloadInvoice(userId, billingProfileId, invoice.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_download_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.oldIsAdmin(userId, otherBillingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.downloadInvoice(userId, otherBillingProfileId, invoice.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @SneakyThrows
        @Test
        void should_download_invoice() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(pdfStoragePort.download(invoice.id() + ".pdf")).thenReturn(pdf);

            // When
            final var invoiceDownload = billingProfileService.downloadInvoice(userId, billingProfileId, invoice.id());

            // Then
            assertThat(invoiceDownload.fileName()).isEqualTo(invoice.number() + ".pdf");
            assertThat(invoiceDownload.data()).isEqualTo(pdf);
        }
    }

    private @NonNull Invoice.Reward fakeReward() {
        return fakeReward(null);
    }

    private @NonNull Invoice.Reward fakeReward(Invoice.Id invoiceId) {
        return new Invoice.Reward(
                RewardId.random(),
                ZonedDateTime.now(),
                faker.lordOfTheRings().character(),
                Money.of(faker.number().randomNumber(1, true), ETH),
                Money.of(faker.number().randomNumber(4, true), USD),
                invoiceId
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
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
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
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
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
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.SELF_EMPLOYED);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
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
        assertThat(billingProfile.type()).isEqualTo(BillingProfile.Type.SELF_EMPLOYED);
        assertThat(billingProfile.owner().id()).isEqualTo(userId);
        assertThat(billingProfile.owner().role()).isEqualTo(BillingProfile.User.Role.ADMIN);
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
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
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
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
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
    }

    @Nested
    class GivenIndividualBillingProfile {

        final UserId userId = UserId.random();
        IndividualBillingProfile billingProfile;

        @BeforeEach
        void setUp() {
            billingProfile = billingProfileService.createIndividualBillingProfile(userId, "my profile", null);
        }

        @Test
        void should_return_current_year_payment_limit_and_amount() {
            // When
            final var limit = billingProfile.currentYearPaymentLimit();
            final var amount = billingProfile.currentYearPaymentAmount();

            // Then
            assertThat(limit).isEqualTo(PositiveAmount.of(5000L));
            assertThat(amount).isEqualTo(PositiveAmount.ZERO);

            //TODO: pay some rewards linked to the billing profile, and check the currentYearPaymentAmount is updated
        }
    }

    @Nested
    class GivenSelfEmployedBillingProfile {

        final UserId userId = UserId.random();
        SelfEmployedBillingProfile billingProfile;

        @BeforeEach
        void setUp() {
            billingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, "my profile", null);
        }

        @Test
        void should_be_switchable_to_company() {
            // When
            final var isSwitchableToSelfEmployed = billingProfile.isSwitchableToCompany();

            // Then
            assertThat(isSwitchableToSelfEmployed).isTrue();
        }
    }

    @Nested
    class GivenCompanyBillingProfile {

        final UserId userId = UserId.random();
        CompanyBillingProfile billingProfile;

        @BeforeEach
        void setUp() {
            billingProfile = billingProfileService.createCompanyBillingProfile(userId, "my profile", null);
        }

        @Test
        void should_add_members() {
            // Given
            final var memberId = UserId.random();
            final var adminId = UserId.random();

            // When
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);
            billingProfile.addMember(adminId, BillingProfile.User.Role.ADMIN);

            // Then
            assertThat(billingProfile.members()).containsExactlyInAnyOrder(
                    new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN),
                    new BillingProfile.User(memberId, BillingProfile.User.Role.MEMBER),
                    new BillingProfile.User(adminId, BillingProfile.User.Role.ADMIN)
            );
        }

        @Test
        void should_remove_members() {
            // Given
            final var memberId = UserId.random();
            final var adminId = UserId.random();
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);
            billingProfile.addMember(adminId, BillingProfile.User.Role.ADMIN);

            // When
            billingProfile.removeMember(memberId);
            billingProfile.removeMember(adminId);

            // Then
            assertThat(billingProfile.members()).containsExactlyInAnyOrder(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN));
        }

        @Test
        void should_remove_last_member() {
            // Given
            final var memberId = UserId.random();
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);

            // When
            billingProfile.removeMember(memberId);

            // Then
            assertThat(billingProfile.members()).containsExactlyInAnyOrder(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN));
        }


        @Test
        void should_not_remove_last_admin() {
            // Given
            final var memberId = UserId.random();
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);

            // When
            assertThatThrownBy(() -> billingProfile.removeMember(userId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot remove last admin %s from company billing profile".formatted(userId));
        }

        @Test
        void should_be_switchable_to_self_employed_as_long_as_there_is_only_one_user() {
            assertThat(billingProfile.isSwitchableToSelfEmployed()).isTrue();

            final var memberId = UserId.random();
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);
            assertThat(billingProfile.isSwitchableToSelfEmployed()).isFalse();

            billingProfile.removeMember(memberId);
            assertThat(billingProfile.isSwitchableToSelfEmployed()).isTrue();

            final var adminId = UserId.random();
            billingProfile.addMember(adminId, BillingProfile.User.Role.ADMIN);
            assertThat(billingProfile.isSwitchableToSelfEmployed()).isFalse();

            billingProfile.removeMember(adminId);
            assertThat(billingProfile.isSwitchableToSelfEmployed()).isTrue();
        }
    }


    @Test
    void should_unauthorized_to_get_billing_profile_given_a_user_not_member_of_it() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdNotMember = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdNotMember)).thenReturn(false);
        Exception exception = null;
        try {
            billingProfileService.getBillingProfile(billingProfileId, userIdNotMember);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(401, ((OnlyDustException) exception).getStatus());
        assertEquals("User %s is not a member of billing profile %s".formatted(userIdNotMember.value(), billingProfileId.value()),
                exception.getMessage());
    }

    @Test
    void should_get_billing_profile_given_a_user_member_of_it() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdMember = UserId.of(UUID.randomUUID());
        final BillingProfileView billingProfileView = BillingProfileView.builder().build();

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdMember)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(billingProfileView));
        final BillingProfileView billingProfile = billingProfileService.getBillingProfile(billingProfileId, userIdMember);

        // Then
        verify(billingProfileStoragePort).findById(billingProfileId);
        assertEquals(billingProfileView, billingProfile);
    }

    @Test
    void should_get_billing_profile_by_id_throw_not_found_given_a_user_member_of_it() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdNotMember = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdNotMember)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.empty());
        Exception exception = null;
        try {
            billingProfileService.getBillingProfile(billingProfileId, userIdNotMember);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(404, ((OnlyDustException) exception).getStatus());
        assertEquals("Billing profile %s not found".formatted(billingProfileId.value()),
                exception.getMessage());
    }


    @Test
    void should_not_authorized_to_modify_payout_info_given_a_user_not_admin_of_linked_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdNotAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIdNotAdmin)).thenReturn(false);
        Exception exception = null;
        try {
            billingProfileService.updatePayoutInfo(billingProfileId, userIdNotAdmin, PayoutInfo.builder().build());
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(401, ((OnlyDustException) exception).getStatus());
        assertEquals("User %s must be admin to edit payout info of billing profile %s".formatted(userIdNotAdmin.value(), billingProfileId.value()),
                exception.getMessage());
    }

    @Test
    void should_not_authorized_to_read_payout_info_given_a_user_not_admin_of_linked_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdNotAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIdNotAdmin)).thenReturn(false);
        Exception exception = null;
        try {
            billingProfileService.getPayoutInfo(billingProfileId, userIdNotAdmin);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(401, ((OnlyDustException) exception).getStatus());
        assertEquals("User %s must be admin to read payout info of billing profile %s".formatted(userIdNotAdmin.value(), billingProfileId.value()),
                exception.getMessage());
    }

    @Test
    void should_update_payout_info_given_a_user_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final PayoutInfo payoutInfo = PayoutInfo.builder().build();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        billingProfileService.updatePayoutInfo(billingProfileId, userIAdmin, payoutInfo);

        // Then
        verify(billingProfileStoragePort).savePayoutInfoForBillingProfile(payoutInfo, billingProfileId);
    }

    @Test
    void should_get_payout_info_given_a_user_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final PayoutInfo expectedPayoutInfo = PayoutInfo.builder().build();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findPayoutInfoByBillingProfile(billingProfileId))
                .thenReturn(Optional.of(expectedPayoutInfo));
        final PayoutInfo payoutInfo = billingProfileService.getPayoutInfo(billingProfileId, userIAdmin);

        // Then
        assertEquals(expectedPayoutInfo, payoutInfo);
        verify(billingProfileStoragePort).findPayoutInfoByBillingProfile(billingProfileId);
    }

    @Test
    void should_get_empty_payout_info_given_a_user_admin_and_no_payout_info() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findPayoutInfoByBillingProfile(billingProfileId))
                .thenReturn(Optional.empty());
        final PayoutInfo payoutInfo = billingProfileService.getPayoutInfo(billingProfileId, userIAdmin);

        // Then
        assertNotNull(payoutInfo);
        verify(billingProfileStoragePort).findPayoutInfoByBillingProfile(billingProfileId);
    }

    @Test
    void should_get_coworkers_last_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(0)
                                .billingProfileAdminCount(1)
                                .build()
                )).build());

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userIAdmin, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(1);
        assertThat(coworkers.getContent().get(0).removable()).isFalse(); // Cannot remove the last admin
    }

    @Test
    void should_get_coworkers_non_last_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(0)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(0)
                                .billingProfileAdminCount(2)
                                .build()
                )).build());

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userIAdmin, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_get_coworkers_member() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(0)
                                .billingProfileAdminCount(1)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.MEMBER)
                                .rewardCount(0)
                                .billingProfileAdminCount(1)
                                .build()
                )).build());

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userIAdmin, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isFalse();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_get_coworkers_member_with_rewards() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(0)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.ADMIN)
                                .rewardCount(1)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(faker.number().randomNumber(10, true))
                                .role(BillingProfile.User.Role.MEMBER)
                                .rewardCount(1)
                                .billingProfileAdminCount(2)
                                .build()
                )).build());

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userIAdmin, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(3);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isFalse();
        assertThat(coworkers.getContent().get(2).removable()).isFalse();
    }


}