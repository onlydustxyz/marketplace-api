package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
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
import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.model.Invoice.Status.APPROVED;
import static onlydust.com.marketplace.accounting.domain.model.Invoice.Status.TO_REVIEW;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyb;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
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
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final AccountingObserverPort accountingObserverPort = mock(AccountingObserver.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort,
            billingProfileObserver, indexerPort, accountingObserverPort);
    final UserId userId = UserId.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    List<Invoice.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());
    List<RewardId> rewardIds = rewards.stream().map(Invoice.Reward::id).toList();
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());
    Invoice invoice;
    BillingProfile.Id billingProfileId;
    BillingProfileView companyBillingProfile;
    PayoutInfo payoutInfo;

    @BeforeEach
    void setUp() {
        payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        billingProfileId = BillingProfile.Id.random();
        companyBillingProfile = BillingProfileView.builder()
                .id(billingProfileId)
                .type(BillingProfile.Type.COMPANY)
                .payoutInfo(payoutInfo)
                .verificationStatus(VerificationStatus.VERIFIED)
                .name("OnlyDust")
                .kyb(newKyb(billingProfileId, userId))
                .build();
        invoice = Invoice.of(companyBillingProfile, 1, userId)
                .rewards(rewards);
        reset(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObserver);
    }

    @Nested
    class GivenCallerIsNotTheBillingProfileAdmin {
        @BeforeEach
        void setup() {
            when(billingProfileStoragePort.isAdmin(billingProfileId, userId)).thenReturn(false);
        }

        @Test
        void should_prevent_invoice_generation() {
            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to generate invoice for this billing profile");

            verify(invoiceStoragePort, never()).create(any());
        }

        @Test
        void should_prevent_invoice_upload() {
            // Given
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().id(billingProfileId).type(BillingProfile.Type.INDIVIDUAL).build()));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User is not allowed to upload an invoice for this billing profile");

            verify(invoiceStoragePort, never()).create(any());
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
            when(billingProfileStoragePort.isAdmin(billingProfileId, userId)).thenReturn(true);
        }

        @Test
        void should_generate_invoice_preview() {
            // Given
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    companyBillingProfile)
            );
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(1);

            // When
            final var preview = billingProfileService.previewInvoice(userId, billingProfileId, rewardIds);

            // Then
            assertThat(preview).isEqualToIgnoringGivenFields(invoice, "id", "createdAt", "dueAt");
            verify(invoiceStoragePort).deleteDraftsOf(billingProfileId);

            final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceStoragePort).create(invoiceCaptor.capture());
            final var invoice = invoiceCaptor.getValue();
            assertThat(invoice.id()).isEqualTo(preview.id());
            assertThat(invoice.billingProfileSnapshot().id()).isEqualTo(billingProfileId);
            assertThat(invoice.number()).isEqualTo(preview.number());
            assertThat(invoice.createdAt()).isEqualTo(preview.createdAt());
            assertThat(invoice.totalAfterTax()).isEqualTo(preview.totalAfterTax());
            assertThat(invoice.status()).isEqualTo(Invoice.Status.DRAFT);
            assertThat(invoice.rewards()).containsExactlyElementsOf(rewards);
        }

        @Test
        void should_prevent_invoice_preview_if_billing_profile_is_not_verified() {
            // Given
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    BillingProfileView.builder()
                            .id(billingProfileId)
                            .type(BillingProfile.Type.COMPANY)
                            .payoutInfo(payoutInfo)
                            .verificationStatus(VerificationStatus.UNDER_REVIEW)
                            .name("OnlyDust")
                            .kyb(newKyb(billingProfileId, userId))
                            .build())
            );
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(1);

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Billing profile %s is not verified".formatted(billingProfileId));
        }

        @Test
        void should_prevent_invoice_preview_given_a_disabled_billing_profile() {
            // When
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(false);

            // Then
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot generate invoice on a disabled billing profile");
        }


        @Test
        void should_prevent_invoice_preview_if_rewards_are_already_invoiced() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            rewards = List.of(fakeReward(), fakeReward(), reward, fakeReward());
            rewardIds = rewards.stream().map(Invoice.Reward::id).toList();

            invoice = Invoice.of(companyBillingProfile, 1, userId)
                    .rewards(rewards)
                    .status(Invoice.Status.APPROVED);
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));

            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    BillingProfileView.builder()
                            .id(billingProfileId)
                            .type(BillingProfile.Type.INDIVIDUAL)
                            .kyc(newKyc(billingProfileId, userId))
                            .payoutInfo(payoutInfo)
                            .build())
            );
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(42);

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are already invoiced");
        }

        @Test
        void should_prevent_invoice_upload_if_not_found() {
            // Given
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .id(billingProfileId).type(BillingProfile.Type.INDIVIDUAL).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_given_a_disabled_billing_profile() {
            // Given
            when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(false);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .id(billingProfileId).type(BillingProfile.Type.INDIVIDUAL).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot upload an invoice on a disabled billing profile %s".formatted(billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.isAdmin(otherBillingProfileId, userId)).thenReturn(true);
            when(billingProfileStoragePort.isEnabled(otherBillingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.findById(otherBillingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().id(otherBillingProfileId)
                    .type(BillingProfile.Type.INDIVIDUAL).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, otherBillingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }


        @Test
        void should_prevent_external_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.isAdmin(otherBillingProfileId, userId)).thenReturn(true);
            when(billingProfileStoragePort.isEnabled(otherBillingProfileId)).thenReturn(true);
            when(billingProfileStoragePort.findById(otherBillingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .id(otherBillingProfileId).type(BillingProfile.Type.COMPANY).build()));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, otherBillingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @Test
        void should_prevent_external_invoice_upload_given_a_disabled_billing_profile() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.isAdmin(otherBillingProfileId, userId)).thenReturn(true);
            when(billingProfileStoragePort.isEnabled(otherBillingProfileId)).thenReturn(false);
            when(billingProfileStoragePort.findById(otherBillingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .id(otherBillingProfileId).type(BillingProfile.Type.COMPANY).build()));

            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, otherBillingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot upload an invoice on a disabled billing profile %s".formatted(otherBillingProfileId));
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
            assertThat(invoices.getContent().get(0).billingProfileSnapshot().id()).isEqualTo(billingProfileId);
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
            when(billingProfileStoragePort.isAdmin(otherBillingProfileId, userId)).thenReturn(true);
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

        @Nested
        class GivenTheMandateIsAccepted {
            @BeforeEach
            void setup() {
                when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().id(billingProfileId).type(BillingProfile.Type.INDIVIDUAL).build()));
                when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            }

            @Test
            void should_prevent_external_invoice_upload() {
                // When
                when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
                assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                        // Then
                        .isInstanceOf(OnlyDustException.class)
                        .hasMessage("External invoice upload is forbidden when mandate has been accepted (billing profile %s)".formatted(billingProfileId));
            }

            @SneakyThrows
            @Test
            void should_upload_generated_invoice_and_save_url() {
                // Given
                final var url = new URL("https://" + faker.internet().url());
                when(pdfStoragePort.upload(invoice.id() + ".pdf", pdf)).thenReturn(url);
                when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);

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
        }

        @Nested
        class GivenTheMandateIsNotAccepted {
            @BeforeEach
            void setup() {
                when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().id(billingProfileId).type(BillingProfile.Type.COMPANY).build()));
                when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            }

            @Test
            void should_prevent_generated_invoice_upload() {
                // When
                when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
                assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                        // Then
                        .isInstanceOf(OnlyDustException.class)
                        .hasMessage("Invoice mandate has not been accepted for billing profile %s".formatted(billingProfileId));
            }

            @SneakyThrows
            @Test
            void should_upload_external_invoice_and_save_url() {
                // Given
                final var url = new URL("https://" + faker.internet().url());
                when(pdfStoragePort.upload(invoice.id() + ".pdf", pdf)).thenReturn(url);
                when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);

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

            @Test
            void should_prevent_external_invoice_upload_if_not_found() {
                // Given
                when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(true);
                when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

                // When
                assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                        // Then
                        .isInstanceOf(OnlyDustException.class)
                        .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
            }
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
    void should_throw_internal_error_given_a_billing_profile_for_a_user_without_rights_found() {
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdMember = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdMember)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().build()));
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdMember))
                .thenReturn(Optional.empty());
        Exception exception = null;
        try {
            billingProfileService.getBillingProfile(billingProfileId, userIdMember);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(500, ((OnlyDustException) exception).getStatus());
        assertEquals("User %s rights on billing profile %s were not found".formatted(userIdMember.value(), billingProfileId.value()),
                exception.getMessage());
    }

    @Test
    void should_get_billing_profile_given_a_user_member_of_it() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdMember = UserId.of(UUID.randomUUID());
        final BillingProfileView billingProfileView = BillingProfileView.builder()
                .kyb(Kyb.builder().id(UUID.randomUUID())
                        .ownerId(UserId.random())
                        .status(VerificationStatus.NOT_STARTED)
                        .billingProfileId(billingProfileId)
                        .build())
                .kyc(Kyc.builder()
                        .id(UUID.randomUUID())
                        .status(VerificationStatus.NOT_STARTED)
                        .ownerId(UserId.random()).build())
                .payoutInfo(PayoutInfo.builder()
                        .aptosAddress(new AptosAccountAddress("0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5"))
                        .build())
                .build();

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdMember)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(billingProfileView));
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdMember))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .hasBillingProfileSomeInvoices(false)
                        .hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(false)
                        .role(BillingProfile.User.Role.MEMBER)
                        .build()));
        final BillingProfileView billingProfile = billingProfileService.getBillingProfile(billingProfileId, userIdMember);

        // Then
        verify(billingProfileStoragePort).findById(billingProfileId);
        assertNotNull(billingProfile.getMe());
        assertNotNull(billingProfile.getKyb());
        assertNotNull(billingProfile.getKyc());
        assertNull(billingProfile.getPayoutInfo());
    }

    @Test
    void should_get_billing_profile_given_a_user_admin_of_it() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIdMember = UserId.of(UUID.randomUUID());
        final BillingProfileView billingProfileView = BillingProfileView.builder()
                .kyb(Kyb.builder().id(UUID.randomUUID())
                        .ownerId(UserId.random())
                        .status(VerificationStatus.NOT_STARTED)
                        .billingProfileId(billingProfileId)
                        .build())
                .kyc(Kyc.builder()
                        .id(UUID.randomUUID())
                        .status(VerificationStatus.NOT_STARTED)
                        .ownerId(UserId.random()).build())
                .payoutInfo(PayoutInfo.builder()
                        .aptosAddress(new AptosAccountAddress("0xa645c3bdd0dfd0c3628803075b3b133e8426061dc915ef996cc5ed4cece6d4e5"))
                        .build())
                .build();

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userIdMember)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(billingProfileView));
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdMember))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .hasBillingProfileSomeInvoices(false)
                        .hasUserSomeRewardsIncludedInInvoicesOnBillingProfile(false)
                        .role(BillingProfile.User.Role.ADMIN)
                        .build()));
        final BillingProfileView billingProfile = billingProfileService.getBillingProfile(billingProfileId, userIdMember);

        // Then
        verify(billingProfileStoragePort).findById(billingProfileId);
        assertNotNull(billingProfile.getMe());
        assertNotNull(billingProfile.getKyb());
        assertNotNull(billingProfile.getKyc());
        assertNotNull(billingProfile.getPayoutInfo());
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
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
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
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
                                .rewardCount(0)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
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
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
                                .rewardCount(0)
                                .billingProfileAdminCount(1)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.MEMBER)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
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
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
                                .rewardCount(0)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
                                .rewardCount(1)
                                .billingProfileAdminCount(2)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.MEMBER)
                                .joinedAt(ZonedDateTime.now())
                                .invitedAt(null)
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

    @Test
    void should_get_invited_coworkers() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), 0, 10))
                .thenReturn(Page.<BillingProfileCoworkerView>builder().content(List.of(
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.ADMIN)
                                .joinedAt(null)
                                .invitedAt(ZonedDateTime.now())
                                .rewardCount(null)
                                .billingProfileAdminCount(null)
                                .build(),
                        BillingProfileCoworkerView.builder()
                                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                                .role(BillingProfile.User.Role.MEMBER)
                                .joinedAt(null)
                                .invitedAt(ZonedDateTime.now())
                                .rewardCount(null)
                                .billingProfileAdminCount(null)
                                .build()
                )).build());

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userIAdmin, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_invite_coworker() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                .type(BillingProfile.Type.COMPANY)
                .build()
        ));

        // When
        billingProfileService.inviteCoworker(billingProfileId, userIAdmin, githubUserId, BillingProfile.User.Role.MEMBER);

        // Then
        verify(indexerPort).indexUser(githubUserId.value());
        verify(billingProfileStoragePort).saveCoworkerInvitation(eq(billingProfileId), eq(userIAdmin), eq(githubUserId), eq(BillingProfile.User.Role.MEMBER),
                any());
    }

    @Test
    void should_prevent_non_admin_to_invite_coworker() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(false);
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder().build()));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfileId, userIAdmin, githubUserId, BillingProfile.User.Role.MEMBER))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to invite coworker to billing profile %s".formatted(userIAdmin.value(), billingProfileId.value()));

        // Then
        verify(indexerPort, never()).indexUser(any());
        verify(billingProfileStoragePort, never()).saveCoworkerInvitation(any(), any(), any(), any(), any());
    }

    @Test
    void should_prevent_to_invite_coworker_on_individual_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId))
                .thenReturn(Optional.of(BillingProfileView.builder().id(billingProfileId).type(BillingProfile.Type.INDIVIDUAL)
                        .verificationStatus(VerificationStatus.NOT_STARTED).build()));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfileId, userIAdmin, githubUserId, BillingProfile.User.Role.ADMIN))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot invite coworker on individual or self employed billing profile %s".formatted(billingProfileId));

        // Then
        verify(indexerPort, never()).indexUser(any());
        verify(billingProfileStoragePort, never()).saveCoworkerInvitation(any(), any(), any(), any(), any());

    }

    @Test
    void should_prevent_to_invite_coworker_on_self_employed_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.findById(billingProfileId))
                .thenReturn(Optional.of(BillingProfileView.builder().id(billingProfileId).type(BillingProfile.Type.SELF_EMPLOYED)
                        .verificationStatus(VerificationStatus.NOT_STARTED).build()));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfileId, userIAdmin, githubUserId, BillingProfile.User.Role.ADMIN))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot invite coworker on individual or self employed billing profile %s".formatted(billingProfileId));

        // Then
        verify(indexerPort, never()).indexUser(any());
        verify(billingProfileStoragePort, never()).saveCoworkerInvitation(any(), any(), any(), any(), any());

    }


    @Test
    void should_accept_invitation() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId invitedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(invitedUserId)
                        .githubUserId(invitedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .invitedAt(ZonedDateTime.now())
                        .build()
        ));

        // When
        billingProfileService.acceptCoworkerInvitation(billingProfileId, invitedGithubUserId);

        // Then
        verify(billingProfileStoragePort).saveCoworker(eq(billingProfileId), eq(invitedUserId), eq(BillingProfile.User.Role.MEMBER), any());
        verify(billingProfileStoragePort).acceptCoworkerInvitation(eq(billingProfileId), eq(invitedGithubUserId));
    }

    @Test
    void should_not_accept_invitation_when_not_found() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId invitedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> billingProfileService.acceptCoworkerInvitation(billingProfileId, invitedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Invitation not found for billing profile %s and user %s".formatted(billingProfileId.value(), invitedGithubUserId.value()));

        // Then
        verify(billingProfileStoragePort, never()).saveCoworker(eq(billingProfileId), eq(invitedUserId), eq(BillingProfile.User.Role.MEMBER), any());
        verify(billingProfileStoragePort, never()).deleteCoworkerInvitation(eq(billingProfileId), eq(invitedGithubUserId));
    }

    @Test
    void should_reject_invitation() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final GithubUserId invitedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .githubUserId(invitedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .invitedAt(ZonedDateTime.now())
                        .build()
        ));

        // When
        billingProfileService.rejectCoworkerInvitation(billingProfileId, invitedGithubUserId);

        // Then
        verify(billingProfileStoragePort).deleteCoworkerInvitation(eq(billingProfileId), eq(invitedGithubUserId));
    }

    @Test
    void should_not_reject_invitation_when_not_found() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final GithubUserId invitedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> billingProfileService.rejectCoworkerInvitation(billingProfileId, invitedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Invitation not found for billing profile %s and user %s".formatted(billingProfileId.value(), invitedGithubUserId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworkerInvitation(eq(billingProfileId), eq(invitedGithubUserId));
    }

    @Test
    void should_remove_user() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId removedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(removedUserId)
                        .githubUserId(removedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.removeCoworker(billingProfileId, userIAdmin, callerGithubUserId, removedGithubUserId);

        // Then
        verify(billingProfileStoragePort).deleteCoworker(eq(billingProfileId), eq(removedUserId));
    }

    @Test
    void should_remove_user_when_caller_is_user() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(false);
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(userIAdmin)
                        .githubUserId(removedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.removeCoworker(billingProfileId, userIAdmin, removedGithubUserId, removedGithubUserId);

        // Then
        verify(billingProfileStoragePort).deleteCoworker(eq(billingProfileId), eq(userIAdmin));
        verify(billingProfileStoragePort).deleteCoworkerInvitation(eq(billingProfileId), eq(removedGithubUserId));
    }

    @Test
    void should_not_remove_user_when_not_found() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, userIAdmin, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Coworker %d not found for billing profile %s".formatted(removedGithubUserId.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }

    @Test
    void should_not_remove_user_when_not_removable() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(true);
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(invitedUserId)
                        .githubUserId(removedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(10)
                        .build()
        ));

        // When
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, userIAdmin, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Coworker %d cannot be removed from billing profile %s".formatted(removedGithubUserId.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }

    @Test
    void should_not_remove_user_when_caller_is_not_admin_and_not_removed_user() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());
        final UserId userIAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.isAdmin(billingProfileId, userIAdmin)).thenReturn(false);
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(invitedUserId)
                        .githubUserId(removedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(10)
                        .build()
        ));

        // When
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, userIAdmin, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to remove coworker %d from billing profile %s".formatted(userIAdmin, removedGithubUserId.value(),
                        billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }


    @Test
    void should_not_delete_billing_profile_given_a_user_not_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId))
                .thenReturn(false);

        // Then
        assertThatThrownBy(() -> billingProfileService.deleteBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to delete billing profile %s".formatted(userId.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).doesBillingProfileHaveSomeInvoices(billingProfileId);
        verify(billingProfileStoragePort, never()).deleteBillingProfile(billingProfileId);
    }

    @Test
    void should_not_delete_billing_profile_given_linked_rewards() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId))
                .thenReturn(true);
        when(billingProfileStoragePort.doesBillingProfileHaveSomeInvoices(billingProfileId))
                .thenReturn(true);

        // Then
        assertThatThrownBy(() -> billingProfileService.deleteBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot delete billing profile %s with invoice(s)".formatted(billingProfileId.value()));
        verify(billingProfileStoragePort, never()).deleteBillingProfile(billingProfileId);
    }

    @Test
    void should_delete_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId))
                .thenReturn(true);
        when(billingProfileStoragePort.doesBillingProfileHaveSomeInvoices(billingProfileId))
                .thenReturn(false);
        billingProfileService.deleteBillingProfile(userId, billingProfileId);

        // Then
        verify(billingProfileStoragePort).deleteBillingProfile(billingProfileId);
    }

    @Test
    void should_not_enable_billing_profile_given_a_user_not_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId))
                .thenReturn(false);

        // Then
        assertThatThrownBy(() -> billingProfileService.enableBillingProfile(userId, billingProfileId, false))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to enable billing profile %s".formatted(userId.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).enableBillingProfile(billingProfileId, false);
    }

    @Test
    void should_enable_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId))
                .thenReturn(true);
        billingProfileService.enableBillingProfile(userId, billingProfileId, true);

        // Then
        verify(billingProfileStoragePort).enableBillingProfile(billingProfileId, true);
        verify(accountingObserverPort).onBillingProfileEnabled(billingProfileId, true);
    }

    @Test
    void should_prevent_update_mandate_acceptance_date_given_a_user_not_admin() {
        // Given
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> billingProfileService.updateInvoiceMandateAcceptanceDate(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s is not allowed to accept invoice mandate for billing profile %s".formatted(userId, billingProfileId));
    }

    @Test
    void should_prevent_update_mandate_acceptance_date_given_a_disabled_billing_profile() {
        // Given
        when(billingProfileStoragePort.isAdmin(billingProfileId, userId)).thenReturn(true);
        when(billingProfileStoragePort.isEnabled(billingProfileId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> billingProfileService.updateInvoiceMandateAcceptanceDate(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot update mandateAcceptanceDate on a disabled billing profile %s".formatted(billingProfileId));
    }


    @Nested
    class UpdateBillingProfileType {

        @Test
        void should_prevent_given_a_user_not_admin() {
            // Given
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final UserId userIdNotAdmin = UserId.random();
            final BillingProfile.Type type = BillingProfile.Type.COMPANY;

            // When
            assertThatThrownBy(() -> billingProfileService.updateBillingProfileType(billingProfileId, userIdNotAdmin, type))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s must be admin to modify billing profile %s type to %s".formatted(userIdNotAdmin.value(), billingProfileId.value(),
                            type));
        }

        @Test
        void should_prevent_given_type_equals_to_individual() {
            // Given
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final UserId userIdAdmin = UserId.random();
            final BillingProfile.Type type = BillingProfile.Type.INDIVIDUAL;
            when(billingProfileStoragePort.isAdmin(billingProfileId, userIdAdmin)).thenReturn(true);

            // When
            assertThatThrownBy(() -> billingProfileService.updateBillingProfileType(billingProfileId, userIdAdmin, type))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s cannot update billing profile %s to type INDIVIDUAL".formatted(userIdAdmin, billingProfileId));
        }

        @Test
        void given_a_self_employed_billing_profile_to_update_to_company() {
            // Given
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final UserId userIdAdmin = UserId.random();
            final BillingProfile.Type type = BillingProfile.Type.COMPANY;
            when(billingProfileStoragePort.isAdmin(billingProfileId, userIdAdmin)).thenReturn(true);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .type(BillingProfile.Type.SELF_EMPLOYED).build()));
            when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdAdmin))
                    .thenReturn(Optional.of(BillingProfileUserRightsView.builder().build()));

            // When
            billingProfileService.updateBillingProfileType(billingProfileId, userIdAdmin, type);

            // Then
            verify(billingProfileStoragePort).updateBillingProfileType(billingProfileId, type);
        }

        @Test
        void given_a_company_billing_profile_to_update_to_self_employed() {
            // Given
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final UserId userIdAdmin = UserId.random();
            final BillingProfile.Type type = BillingProfile.Type.SELF_EMPLOYED;
            when(billingProfileStoragePort.isAdmin(billingProfileId, userIdAdmin)).thenReturn(true);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .type(BillingProfile.Type.COMPANY).build()));
            when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdAdmin))
                    .thenReturn(Optional.of(BillingProfileUserRightsView.builder().hasMoreThanOneCoworkers(false).build()));

            // When
            billingProfileService.updateBillingProfileType(billingProfileId, userIdAdmin, type);

            // Then
            verify(billingProfileStoragePort).updateBillingProfileType(billingProfileId, type);
        }

        @Test
        void prevent_given_a_company_billing_profile_to_update_to_self_employed_with_some_coworkers() {
            // Given
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final UserId userIdAdmin = UserId.random();
            final BillingProfile.Type type = BillingProfile.Type.SELF_EMPLOYED;
            when(billingProfileStoragePort.isAdmin(billingProfileId, userIdAdmin)).thenReturn(true);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(BillingProfileView.builder()
                    .type(BillingProfile.Type.COMPANY).build()));
            when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userIdAdmin))
                    .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                            .hasMoreThanOneCoworkers(true).build()));

            // When
            assertThatThrownBy(() -> billingProfileService.updateBillingProfileType(billingProfileId, userIdAdmin, type))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s cannot update billing profile %s of type %s to type %s".formatted(
                            userIdAdmin, billingProfileId, BillingProfile.Type.COMPANY, type));

        }


    }
}