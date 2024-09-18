package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.notification.CompleteYourBillingProfile;
import onlydust.com.marketplace.accounting.domain.notification.dto.NotificationBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.accounting.domain.view.RewardAssociations;
import onlydust.com.marketplace.accounting.domain.view.UserView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.accounting.domain.model.Invoice.Status.*;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyb;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingProfileServiceTest {
    final Faker faker = new Faker();
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    final BillingProfileStoragePort billingProfileStoragePort = mock(BillingProfileStoragePort.class);
    final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);
    final BillingProfileObserverPort billingProfileObserver = mock(BillingProfileObserverPort.class);
    final IndexerPort indexerPort = mock(IndexerPort.class);
    final AccountingObserverPort accountingObserverPort = mock(AccountingObserverPort.class);
    final AccountingFacadePort accountingFacadePort = mock(AccountingFacadePort.class);
    final PayoutInfoValidator payoutInfoValidator = mock(PayoutInfoValidator.class);
    final NotificationPort notificationPort = mock(NotificationPort.class);
    final BillingProfileService billingProfileService = new BillingProfileService(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort,
            billingProfileObserver, indexerPort, accountingObserverPort, accountingFacadePort, payoutInfoValidator, notificationPort);
    UserId userId = UserId.random();
    final Currency ETH = Currencies.ETH;
    final Currency USD = Currencies.USD;
    List<Invoice.Reward> rewards = List.of(fakeReward(), fakeReward(), fakeReward());
    List<RewardId> rewardIds = rewards.stream().map(Invoice.Reward::id).toList();
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());
    Invoice invoice;
    BillingProfile.Id billingProfileId;
    CompanyBillingProfile companyBillingProfile;
    PayoutInfo payoutInfo;

    @BeforeEach
    void setUp() {
        payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        billingProfileId = BillingProfile.Id.random();
        companyBillingProfile = CompanyBillingProfile.builder()
                .id(billingProfileId)
                .status(VerificationStatus.VERIFIED)
                .name("OnlyDust")
                .kyb(newKyb(billingProfileId, userId))
                .enabled(true)
                .members(new HashSet<>())
                .invoiceMandateAcceptedAt(ZonedDateTime.now())
                .invoiceMandateAcceptanceOutdated(false)
                .build();
        companyBillingProfile.addMember(userId, BillingProfile.User.Role.ADMIN);
        invoice = Invoice.of(companyBillingProfile, 1, userId, payoutInfo)
                .rewards(rewards);
        reset(invoiceStoragePort, billingProfileStoragePort, pdfStoragePort, billingProfileObserver, accountingFacadePort);

        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(companyBillingProfile));
        when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));
    }

    @Nested
    class GivenCallerIsNotTheBillingProfileAdmin {
        @BeforeEach
        void setup() {
            userId = UserId.random();
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
        @Test
        void should_generate_invoice_preview() {
            // Given
            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), null, null,
                            billingProfileId)).toList());
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(1);
            when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));

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
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().status(VerificationStatus.NOT_STARTED).build()));
            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(1);
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), null, null,
                            billingProfileId)).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Billing profile %s is not verified".formatted(billingProfileId));
        }

        @Test
        void should_prevent_invoice_preview_given_a_disabled_billing_profile() {
            // When
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(companyBillingProfile.toBuilder().enabled(false).build()));

            // Then
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot generate invoice on a disabled billing profile");
        }

        @Test
        void should_prevent_invoice_preview_if_some_rewards_have_been_cancelled() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            rewards = List.of(fakeReward(), fakeReward(), reward, fakeReward());
            rewardIds = rewards.stream().map(Invoice.Reward::id).toList();

            invoice = Invoice.of(companyBillingProfile, 1, userId, payoutInfo)
                    .rewards(rewards)
                    .status(Invoice.Status.APPROVED);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));

            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .kyc(newKyc(billingProfileId, userId))
                            .status(VerificationStatus.VERIFIED)
                            .enabled(true)
                            .name("John")
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .build())
            );
            when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(42);
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(List.of(new RewardAssociations(rewardIds.get(0),
                    RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), null, null, billingProfileId)));

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Some invoice's rewards were not found");
        }

        @Test
        void should_prevent_invoice_preview_if_rewards_are_already_invoiced() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            rewards = List.of(fakeReward(), fakeReward(), reward, fakeReward());
            rewardIds = rewards.stream().map(Invoice.Reward::id).toList();

            invoice = Invoice.of(companyBillingProfile, 1, userId, payoutInfo)
                    .rewards(rewards)
                    .status(Invoice.Status.APPROVED);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));

            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .kyc(newKyc(billingProfileId, userId))
                            .status(VerificationStatus.VERIFIED)
                            .enabled(true)
                            .name("John")
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .build())
            );
            when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(42);
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), Invoice.Id.random(),
                            TO_REVIEW, BillingProfile.Id.random())).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are already invoiced");


            // Should not reject when rewards are associated with an invoice in DRAFT
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), Invoice.Id.random(),
                            DRAFT, BillingProfile.Id.random())).toList());

            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Some rewards are not associated with billing profile");

            // Should not reject when rewards are associated with an invoice in REJECTED
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), Invoice.Id.random(),
                            REJECTED, BillingProfile.Id.random())).toList());

            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Some rewards are not associated with billing profile");
        }

        @Test
        void should_prevent_invoice_preview_if_rewards_have_wrong_status() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            rewards = List.of(fakeReward(), fakeReward(), reward, fakeReward());
            rewardIds = rewards.stream().map(Invoice.Reward::id).toList();

            invoice = Invoice.of(companyBillingProfile, 1, userId, payoutInfo)
                    .rewards(rewards)
                    .status(Invoice.Status.APPROVED);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));

            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));

            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(42);
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(), RewardStatus.builder()
                            .projectId(ProjectId.random().value())
                            .recipientId(faker.number().randomNumber(4, true))
                            .status(RewardStatus.Input.INDIVIDUAL_LIMIT_REACHED).build(),
                            Invoice.Id.random(), Invoice.Status.DRAFT,
                            billingProfileId)).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards don't have the PENDING_REQUEST status");
        }

        @Test
        void should_prevent_invoice_preview_if_rewards_have_wrong_billing_profile() {
            // Given
            final var reward = fakeReward(Invoice.Id.random());
            rewards = List.of(fakeReward(), fakeReward(), reward, fakeReward());
            rewardIds = rewards.stream().map(Invoice.Reward::id).toList();

            invoice = Invoice.of(companyBillingProfile, 1, userId, payoutInfo)
                    .rewards(rewards)
                    .status(Invoice.Status.APPROVED);
            when(invoiceStoragePort.get(reward.invoiceId())).thenReturn(Optional.of(invoice));
            when(billingProfileStoragePort.getPayoutInfo(billingProfileId)).thenReturn(Optional.of(payoutInfo));

            when(invoiceStoragePort.findRewards(rewardIds)).thenReturn(rewards);
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .name("John")
                            .kyc(newKyc(billingProfileId, userId))
                            .status(VerificationStatus.VERIFIED)
                            .enabled(true)
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .build())
            );
            when(invoiceStoragePort.getNextSequenceNumber(billingProfileId)).thenReturn(42);

            final var billingProfileId = BillingProfile.Id.random();
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(), RewardStatus.builder()
                            .projectId(ProjectId.random().value())
                            .recipientId(faker.number().randomNumber(4, true))
                            .billingProfileId(billingProfileId.value())
                            .status(RewardStatus.Input.PENDING_REQUEST)
                            .build(), null, null, billingProfileId)).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.previewInvoice(userId, BillingProfileServiceTest.this.billingProfileId, rewardIds))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are not associated with billing profile %s".formatted(BillingProfileServiceTest.this.billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_not_found() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), billingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_if_not_a_draft() {
            // Given
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice.status(REJECTED)));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s is not in DRAFT status".formatted(invoice.id()));
        }

        @Test
        void should_prevent_invoice_upload_given_a_disabled_billing_profile() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(companyBillingProfile.toBuilder().enabled(false).build()));
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
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(billingProfileStoragePort.findById(otherBillingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().id(otherBillingProfileId).build()));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, otherBillingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s not found for billing profile %s".formatted(invoice.id(), otherBillingProfileId));
        }

        @Test
        void should_prevent_invoice_upload_when_a_reward_was_cancelled() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .name("name")
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .enabled(true)
                            .status(VerificationStatus.NOT_STARTED)
                            .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                            .invoiceMandateAcceptedAt(ZonedDateTime.now())
                            .invoiceMandateAcceptanceOutdated(false)
                            .build()
            ));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(List.of(new RewardAssociations(rewardIds.get(0),
                    RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(), Invoice.Status.DRAFT, billingProfileId)));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some invoice's rewards were not found (invoice %s). This may happen if a reward was cancelled in the meantime.".formatted(invoice.id()));
        }

        @Test
        void should_prevent_invoice_upload_when_reward_invoice_id_does_not_match() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .name("name")
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .enabled(true)
                            .status(VerificationStatus.NOT_STARTED)
                            .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                            .invoiceMandateAcceptedAt(ZonedDateTime.now())
                            .invoiceMandateAcceptanceOutdated(false)
                            .build()
                    ));

            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), Invoice.Id.random(),
                            Invoice.Status.DRAFT, billingProfileId)).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are not associated with invoice %s".formatted(invoice.id()));
        }

        @Test
        void should_prevent_invoice_upload_when_reward_billing_profile_id_does_not_match() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(
                    IndividualBillingProfile.builder()
                            .id(billingProfileId)
                            .name("name")
                            .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .enabled(true)
                            .status(VerificationStatus.NOT_STARTED)
                            .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                            .invoiceMandateAcceptedAt(ZonedDateTime.now())
                            .invoiceMandateAcceptanceOutdated(false)
                            .build()
            ));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(),
                            Invoice.Status.DRAFT, BillingProfile.Id.random())).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadGeneratedInvoice(userId, billingProfileId, invoice.id(), pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are not associated with billing profile %s".formatted(billingProfileId));
        }


        @Test
        void should_prevent_external_invoice_upload_if_not_a_draft() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().invoiceMandateAcceptanceOutdated(true).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice.status(REJECTED)));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Invoice %s is not in DRAFT status".formatted(invoice.id()));
        }

        @Test
        void should_prevent_external_invoice_upload_if_billing_profile_does_not_match() {
            // Given
            final var otherBillingProfileId = BillingProfile.Id.random();
            when(billingProfileStoragePort.findById(otherBillingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder()
                            .id(otherBillingProfileId)
                            .invoiceMandateAcceptanceOutdated(true)
                            .build()));

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
            when(billingProfileStoragePort.findById(otherBillingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder()
                            .id(otherBillingProfileId)
                            .invoiceMandateAcceptedAt(null)
                            .enabled(false)
                            .build()));

            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, otherBillingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Cannot upload an invoice on a disabled billing profile %s".formatted(otherBillingProfileId));
        }


        @Test
        void should_prevent_external_invoice_upload_when_a_reward_was_cancelled() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().invoiceMandateAcceptanceOutdated(true).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(List.of(new RewardAssociations(rewardIds.get(0),
                    RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(), Invoice.Status.DRAFT, billingProfileId)));

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some invoice's rewards were not found (invoice %s). This may happen if a reward was cancelled in the meantime.".formatted(invoice.id()));
        }

        @Test
        void should_prevent_external_invoice_upload_when_reward_invoice_id_does_not_match() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().invoiceMandateAcceptanceOutdated(true).build()));

            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), Invoice.Id.random(),
                            Invoice.Status.DRAFT, billingProfileId)).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are not associated with invoice %s".formatted(invoice.id()));
        }

        @Test
        void should_prevent_external_invoice_upload_when_reward_billing_profile_id_does_not_match() {
            // Given
            when(billingProfileStoragePort.findById(billingProfileId))
                    .thenReturn(Optional.of(companyBillingProfile.toBuilder().invoiceMandateAcceptanceOutdated(true).build()));
            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                    .map(r -> new RewardAssociations(r.id(),
                            RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(),
                            Invoice.Status.DRAFT, BillingProfile.Id.random())).toList());

            // When
            assertThatThrownBy(() -> billingProfileService.uploadExternalInvoice(userId, billingProfileId, invoice.id(), "foo.pdf", pdf))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Some rewards are not associated with billing profile %s".formatted(billingProfileId));
        }

        @SneakyThrows
        @Test
        void should_list_invoices() {
            // Given
            final var invoiceView = new InvoiceView(
                    invoice.id(),
                    invoice.billingProfileSnapshot(),
                    new UserView(faker.number().randomNumber(), faker.name().username(), URI.create(faker.internet().avatar()),
                            faker.internet().emailAddress(), UserId.random(), faker.name().firstName()),
                    invoice.createdAt(),
                    Money.of(faker.number().randomNumber(1, true), USD),
                    invoice.createdAt().plusDays(30),
                    invoice.number(),
                    invoice.status(),
                    List.of(),
                    invoice.url(),
                    invoice.originalFileName(),
                    null
            );

            when(invoiceStoragePort.invoicesOf(billingProfileId, 1, 10, Invoice.Sort.STATUS, SortDirection.asc))
                    .thenReturn(Page.<InvoiceView>builder().content(List.of(invoiceView)).totalItemNumber(1).totalPageNumber(1).build());

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
            when(billingProfileStoragePort.findById(otherBillingProfileId)).thenReturn(Optional.of(companyBillingProfile.toBuilder().id(otherBillingProfileId).build()));
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
                when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            }

            @Test
            void should_prevent_external_invoice_upload() {
                // When
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
                when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                        .map(r -> new RewardAssociations(r.id(),
                                RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(),
                                Invoice.Status.DRAFT, billingProfileId)).toList());

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
                when(billingProfileStoragePort.findById(billingProfileId))
                        .thenReturn(Optional.of(companyBillingProfile.toBuilder().invoiceMandateAcceptanceOutdated(true).build()));
                when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
            }

            @Test
            void should_prevent_generated_invoice_upload() {
                // When
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
                when(invoiceStoragePort.getRewardAssociations(rewardIds)).thenReturn(rewards.stream()
                        .map(r -> new RewardAssociations(r.id(),
                                RewardStatus.builder().projectId(ProjectId.random().value()).recipientId(faker.number().randomNumber(4, true)).status(RewardStatus.Input.PENDING_REQUEST).build(), invoice.id(),
                                Invoice.Status.DRAFT, billingProfileId)).toList());

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
                invoiceId,
                List.of()
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
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());
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
        verify(accountingObserverPort).onPayoutPreferenceChanged(billingProfile.id(), userId, selectForProjects.iterator().next());
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
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());

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
        verify(accountingObserverPort).onPayoutPreferenceChanged(billingProfile.id(), userId, selectForProjects.iterator().next());
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
        assertThat(billingProfile.members()).hasSize(1);
        assertThat(billingProfile.members()).allMatch(u -> u.id().equals(userId) && u.role().equals(BillingProfile.User.Role.ADMIN));
        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort, never()).savePayoutPreference(eq(billingProfile.id()), eq(userId), any());
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());
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
        assertThat(billingProfile.members()).hasSize(1);
        assertThat(billingProfile.members()).allMatch(u -> u.id().equals(userId) && u.role().equals(BillingProfile.User.Role.ADMIN));

        verify(billingProfileStoragePort).save(billingProfile);
        verify(billingProfileStoragePort).savePayoutPreference(billingProfile.id(), userId, selectForProjects.iterator().next());
        verify(accountingObserverPort).onPayoutPreferenceChanged(billingProfile.id(), userId, selectForProjects.iterator().next());
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
            assertThat(billingProfile.members()).hasSize(3);
            assertThat(billingProfile.members()).usingElementComparatorIgnoringFields("joinedAt").containsExactlyInAnyOrder(
                    new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()),
                    new BillingProfile.User(memberId, BillingProfile.User.Role.MEMBER, ZonedDateTime.now()),
                    new BillingProfile.User(adminId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now())
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
            assertThat(billingProfile.members()).hasSize(1);
            assertThat(billingProfile.members()).allMatch(u -> u.id().equals(userId) && u.role().equals(BillingProfile.User.Role.ADMIN));
        }

        @Test
        void should_remove_last_member() {
            // Given
            final var memberId = UserId.random();
            billingProfile.addMember(memberId, BillingProfile.User.Role.MEMBER);

            // When
            billingProfile.removeMember(memberId);

            // Then
            assertThat(billingProfile.members())
                    .usingElementComparatorIgnoringFields("joinedAt")
                    .containsExactlyInAnyOrder(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()));
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
    void should_not_authorized_to_modify_payout_info_given_a_user_not_admin_of_linked_billing_profile() {
        // Given
        final UserId userIdNotAdmin = UserId.of(UUID.randomUUID());

        // When
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
    void should_update_payout_info_given_a_user_admin() {
        // When
        billingProfileService.updatePayoutInfo(billingProfileId, userId, payoutInfo);

        // Then
        verify(billingProfileStoragePort).savePayoutInfoForBillingProfile(payoutInfo, billingProfileId);
        verify(payoutInfoValidator).validate(payoutInfo);
    }

    @Test
    void should_not_update_payout_info_given_invalid_payout_info() {
        // Given
        doThrow(badRequest("Invalid payout info")).when(payoutInfoValidator).validate(payoutInfo);

        // When
        assertThatThrownBy(() -> billingProfileService.updatePayoutInfo(billingProfileId, userId, payoutInfo))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Invalid payout info");
    }

    @Test
    void should_get_coworkers_last_admin() {
        // When
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

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userId, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(1);
        assertThat(coworkers.getContent().get(0).removable()).isFalse(); // Cannot remove the last admin
    }

    @Test
    void should_get_coworkers_non_last_admin() {
        // When
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

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userId, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_get_coworkers_member() {
        // When
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

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userId, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isFalse();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_get_coworkers_member_with_rewards() {
        // When
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

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userId, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(3);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isFalse();
        assertThat(coworkers.getContent().get(2).removable()).isFalse();
    }

    @Test
    void should_get_invited_coworkers() {
        // When
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

        final var coworkers = billingProfileService.getCoworkers(billingProfileId, userId, 0, 10);

        // Then
        assertThat(coworkers.getContent()).hasSize(2);
        assertThat(coworkers.getContent().get(0).removable()).isTrue();
        assertThat(coworkers.getContent().get(1).removable()).isTrue();
    }

    @Test
    void should_invite_coworker() {
        // Given
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        // When
        billingProfileService.inviteCoworker(billingProfileId, userId, githubUserId, BillingProfile.User.Role.MEMBER);

        // Then
        verify(indexerPort).indexUser(githubUserId.value());
        verify(billingProfileStoragePort).saveCoworkerInvitation(eq(billingProfileId), eq(userId), eq(githubUserId), eq(BillingProfile.User.Role.MEMBER),
                any());
    }

    @Test
    void should_prevent_non_admin_to_invite_coworker() {
        // Given
        final UserId userIdNotAdmin = UserId.of(UUID.randomUUID());
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfileId, userIdNotAdmin, githubUserId, BillingProfile.User.Role.MEMBER))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to invite coworker to billing profile %s".formatted(userIdNotAdmin, billingProfileId));

        // Then
        verify(indexerPort, never()).indexUser(any());
        verify(billingProfileStoragePort, never()).saveCoworkerInvitation(any(), any(), any(), any(), any());
    }

    @Test
    void should_prevent_to_invite_coworker_on_individual_billing_profile() {
        // Given
        final var billingProfile = billingProfileService.createIndividualBillingProfile(userId, "name", null);
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.findById(billingProfile.id())).thenReturn(Optional.of(billingProfile));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfile.id(), userId, githubUserId, BillingProfile.User.Role.ADMIN))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot invite coworker on individual or self employed billing profile %s".formatted(billingProfile.id()));

        // Then
        verify(indexerPort, never()).indexUser(any());
        verify(billingProfileStoragePort, never()).saveCoworkerInvitation(any(), any(), any(), any(), any());

    }

    @Test
    void should_prevent_to_invite_coworker_on_self_employed_billing_profile() {
        // Given
        final var billingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, "name", null);
        final GithubUserId githubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.findById(billingProfile.id()))
                .thenReturn(Optional.of(billingProfile));

        // When
        assertThatThrownBy(() -> billingProfileService.inviteCoworker(billingProfile.id(), userId, githubUserId, BillingProfile.User.Role.ADMIN))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot invite coworker on individual or self employed billing profile %s".formatted(billingProfile.id()));

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
        final GithubUserId invitedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
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
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId removedUserId = UserId.of(UUID.randomUUID());

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
        billingProfileService.removeCoworker(billingProfileId, userId, callerGithubUserId, removedGithubUserId);

        // Then
        verify(billingProfileStoragePort).deleteCoworker(eq(billingProfileId), eq(removedUserId));
    }

    @Test
    void should_downgrade_user() {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final var coworkerUserId = UserId.of(UUID.randomUUID());

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(coworkerUserId)
                        .githubUserId(coworkerGithubUserId)
                        .role(BillingProfile.User.Role.ADMIN)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, BillingProfile.User.Role.MEMBER);

        // Then
        verify(billingProfileStoragePort).updateCoworkerRole(billingProfileId, coworkerUserId, BillingProfile.User.Role.MEMBER);
    }

    @ParameterizedTest
    @EnumSource(value = BillingProfile.User.Role.class)
    void should_update_invited_user_role(BillingProfile.User.Role role) {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final var coworkerUserId = UserId.of(UUID.randomUUID());

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(coworkerUserId)
                        .githubUserId(coworkerGithubUserId)
                        .role(role == BillingProfile.User.Role.ADMIN ? BillingProfile.User.Role.MEMBER : BillingProfile.User.Role.ADMIN)
                        .joinedAt(null)
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, role);

        // Then
        verify(billingProfileStoragePort, never()).updateCoworkerRole(any(), any(), any());
        verify(billingProfileStoragePort).updateCoworkerInvitationRole(billingProfileId, coworkerGithubUserId, role);
    }


    @Test
    void should_upgrade_user() {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final var coworkerUserId = UserId.of(UUID.randomUUID());

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(coworkerUserId)
                        .githubUserId(coworkerGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, BillingProfile.User.Role.ADMIN);

        // Then
        verify(billingProfileStoragePort).updateCoworkerRole(billingProfileId, coworkerUserId, BillingProfile.User.Role.ADMIN);
    }

    @Test
    void should_remove_user_when_caller_is_user() {
        // Given
        final UserId otherUser = UserId.of(UUID.randomUUID());
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(otherUser)
                        .githubUserId(removedGithubUserId)
                        .role(BillingProfile.User.Role.MEMBER)
                        .joinedAt(ZonedDateTime.now())
                        .invitedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.removeCoworker(billingProfileId, otherUser, removedGithubUserId, removedGithubUserId);

        // Then
        verify(billingProfileStoragePort).deleteCoworker(eq(billingProfileId), eq(otherUser));
        verify(billingProfileStoragePort).deleteCoworkerInvitation(eq(billingProfileId), eq(removedGithubUserId));
    }

    @Test
    void should_downgrade_user_when_caller_is_user() {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(userId)
                        .githubUserId(coworkerGithubUserId)
                        .role(BillingProfile.User.Role.ADMIN)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .build()
        ));

        // When
        billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, BillingProfile.User.Role.MEMBER);

        // Then
        verify(billingProfileStoragePort).updateCoworkerRole(billingProfileId, userId, BillingProfile.User.Role.MEMBER);
    }

    @Test
    void should_not_remove_user_when_not_found() {
        // Given
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
        when(billingProfileStoragePort.getCoworker(billingProfileId, removedGithubUserId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, userId, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Coworker %d not found for billing profile %s".formatted(removedGithubUserId.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }

    @Test
    void should_not_downgrade_user_when_not_found() {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.of(
                BillingProfileCoworkerView.builder()
                        .userId(userId)
                        .githubUserId(coworkerGithubUserId)
                        .role(BillingProfile.User.Role.ADMIN)
                        .joinedAt(ZonedDateTime.now())
                        .rewardCount(0)
                        .billingProfileAdminCount(1)
                        .build()
        ));

        // When
        assertThatThrownBy(() -> billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, BillingProfile.User.Role.MEMBER))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot downgrade user %s of billing profile %s".formatted(userId, billingProfileId));

        // Then
        verify(billingProfileStoragePort, never()).updateCoworkerRole(any(), any(), any());
    }

    @Test
    void should_not_remove_user_when_not_removable() {
        // Given
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
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
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, userId, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Coworker %d cannot be removed from billing profile %s".formatted(removedGithubUserId.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }

    @Test
    void should_not_downgrade_user_when_last_admin() {
        // Given
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        when(billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> billingProfileService.updateCoworkerRole(billingProfileId, userId, coworkerGithubUserId, BillingProfile.User.Role.MEMBER))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Coworker %d not found for billing profile %s".formatted(coworkerGithubUserId.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).updateCoworkerRole(any(), any(), any());
    }

    @Test
    void should_not_remove_user_when_caller_is_not_admin_and_not_removed_user() {
        // Given
        final UserId otherUser = UserId.of(UUID.randomUUID());
        final GithubUserId callerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final GithubUserId removedGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));
        final UserId invitedUserId = UserId.of(UUID.randomUUID());
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
        assertThatThrownBy(() -> billingProfileService.removeCoworker(billingProfileId, otherUser, callerGithubUserId, removedGithubUserId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to remove coworker %d from billing profile %s".formatted(otherUser, removedGithubUserId.value(),
                        billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).deleteCoworker(eq(billingProfileId), eq(invitedUserId));
    }

    @ParameterizedTest
    @EnumSource(value = BillingProfile.User.Role.class)
    void should_not_modify_user_role_when_caller_is_not_admin(BillingProfile.User.Role role) {
        // Given
        final var otherUser = UserId.random();
        final var coworkerGithubUserId = GithubUserId.of(faker.number().randomNumber(10, true));

        // When
        assertThatThrownBy(() -> billingProfileService.updateCoworkerRole(billingProfileId, otherUser, coworkerGithubUserId, role))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to manage billing profile %s coworkers".formatted(otherUser.value(), billingProfileId.value()));

        // Then
        verify(billingProfileStoragePort, never()).updateCoworkerRole(any(), any(), any());
    }

    @Test
    void should_not_delete_billing_profile_given_a_user_not_admin() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .role(BillingProfile.User.Role.MEMBER)
                        .billingProfileProcessingRewardsCount(0L)
                        .build()));


        // Then
        assertThatThrownBy(() -> billingProfileService.deleteBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s cannot delete billing profile %s".formatted(userId.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).deleteBillingProfile(billingProfileId);
    }


    @Test
    void should_not_delete_individual_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .role(BillingProfile.User.Role.ADMIN)
                        .billingProfileType(BillingProfile.Type.INDIVIDUAL)
                        .billingProfileProcessingRewardsCount(0L)
                        .build()));


        // Then
        assertThatThrownBy(() -> billingProfileService.deleteBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s cannot delete billing profile %s".formatted(userId.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).deleteBillingProfile(billingProfileId);
    }

    @Test
    void should_not_delete_billing_profile_given_linked_rewards() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .role(BillingProfile.User.Role.ADMIN)
                        .billingProfileProcessingRewardsCount(1L)
                        .build()));

        // Then
        assertThatThrownBy(() -> billingProfileService.deleteBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s cannot delete billing profile %s".formatted(userId, billingProfileId));
        verify(billingProfileStoragePort, never()).deleteBillingProfile(billingProfileId);
    }

    @Test
    void should_delete_billing_profile() {
        // Given
        final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
        final UserId userId = UserId.random();

        // When
        when(billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId))
                .thenReturn(Optional.of(BillingProfileUserRightsView.builder()
                        .role(BillingProfile.User.Role.ADMIN)
                        .billingProfileProcessingRewardsCount(0L)
                        .build()));

        billingProfileService.deleteBillingProfile(userId, billingProfileId);

        // Then
        verify(billingProfileStoragePort).deleteBillingProfile(billingProfileId);
        verify(accountingObserverPort).onBillingProfileDeleted(billingProfileId);
    }

    @Test
    void should_not_enable_billing_profile_given_a_user_not_admin() {
        // Given
        final UserId otherUser = UserId.random();

        // Then
        assertThatThrownBy(() -> billingProfileService.enableBillingProfile(otherUser, billingProfileId, false))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to enable billing profile %s".formatted(otherUser.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).updateEnableBillingProfile(billingProfileId, false);
    }

    @Test
    void should_enable_billing_profile() {
        // When
        billingProfileService.enableBillingProfile(userId, billingProfileId, true);

        // Then
        verify(billingProfileStoragePort).updateEnableBillingProfile(billingProfileId, true);
        verify(accountingObserverPort).onBillingProfileEnableChanged(billingProfileId, true);
    }

    @Test
    void should_prevent_update_mandate_acceptance_date_given_a_user_not_admin() {
        // Given
        when(billingProfileStoragePort.findById(billingProfileId))
                .thenReturn(Optional.of(IndividualBillingProfile.builder()
                        .id(billingProfileId)
                        .name("name")
                        .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                        .enabled(false)
                        .status(VerificationStatus.NOT_STARTED)
                        .kyc(Kyc.initForUserAndBillingProfile(UserId.random(), billingProfileId))
                        .build()
                ));

        // When
        assertThatThrownBy(() -> billingProfileService.acceptInvoiceMandate(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s is not allowed to accept invoice mandate for billing profile %s".formatted(userId, billingProfileId));
    }

    @Test
    void should_prevent_update_mandate_acceptance_date_given_a_disabled_billing_profile() {
        // Given
        when(billingProfileStoragePort.findById(billingProfileId))
                .thenReturn(Optional.of(IndividualBillingProfile.builder()
                        .id(billingProfileId)
                        .name("name")
                        .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                        .enabled(false)
                        .status(VerificationStatus.NOT_STARTED)
                        .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                        .build()
                ));

        // When
        assertThatThrownBy(() -> billingProfileService.acceptInvoiceMandate(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot update mandateAcceptanceDate on a disabled billing profile %s".formatted(billingProfileId));
    }

    @Nested
    class UpdateBillingProfileType {

        @Test
        void should_prevent_given_a_user_not_admin() {
            // Given
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
            final BillingProfile.Type type = BillingProfile.Type.INDIVIDUAL;

            // When
            assertThatThrownBy(() -> billingProfileService.updateBillingProfileType(billingProfileId, userId, type))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s cannot update billing profile %s to type INDIVIDUAL".formatted(userId, billingProfileId));
        }

        @Test
        void given_a_self_employed_billing_profile_to_update_to_company() {
            // Given
            final BillingProfile.Type type = BillingProfile.Type.COMPANY;
            final var selfEmployedBillingProfile = billingProfileService.createSelfEmployedBillingProfile(userId, "name", null);
            when(billingProfileStoragePort.findById(selfEmployedBillingProfile.id())).thenReturn(Optional.of(selfEmployedBillingProfile));

            // When
            billingProfileService.updateBillingProfileType(selfEmployedBillingProfile.id(), userId, type);

            // Then
            verify(billingProfileStoragePort).updateBillingProfileType(selfEmployedBillingProfile.id(), type);
        }

        @Test
        void given_a_company_billing_profile_to_update_to_self_employed() {
            // Given
            final BillingProfile.Type type = BillingProfile.Type.SELF_EMPLOYED;

            // When
            billingProfileService.updateBillingProfileType(billingProfileId, userId, type);

            // Then
            verify(billingProfileStoragePort).updateBillingProfileType(billingProfileId, type);
        }

        @Test
        void prevent_given_a_company_billing_profile_to_update_to_self_employed_with_some_coworkers() {
            // Given
            final BillingProfile.Type type = BillingProfile.Type.SELF_EMPLOYED;
            companyBillingProfile.addMember(UserId.random(), BillingProfile.User.Role.MEMBER);

            // When
            assertThatThrownBy(() -> billingProfileService.updateBillingProfileType(billingProfileId, userId, type))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s cannot update billing profile %s of type %s to type %s".formatted(
                            userId, billingProfileId, BillingProfile.Type.COMPANY, type));

        }
    }

    @Test
    void should_not_return_invoiceable_rewards_given_a_user_not_admin() {
        // Given
        final UserId userId = UserId.random();

        // Then
        assertThatThrownBy(() -> billingProfileService.getInvoiceableRewardsForBillingProfile(userId, billingProfileId))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %s must be admin to get invoiceable rewards of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        verify(billingProfileStoragePort, never()).findInvoiceableRewardsForBillingProfile(any());
    }

    @Test
    void should_return_invoiceable_rewards() {
        // When
        billingProfileService.getInvoiceableRewardsForBillingProfile(userId, billingProfileId);

        // Then
        verify(billingProfileStoragePort).findInvoiceableRewardsForBillingProfile(billingProfileId);
    }

    @Test
    void should_remind_billing_profile_admins_to_complete_their_billing_profiles() {
        // Given
        final BillingProfile.User bpCompanyAdmin1 = new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now());
        final BillingProfile.User bpCompanyAdmin2 = new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now());
        final BillingProfile.User bpCompanyMember1 = new BillingProfile.User(UserId.random(), BillingProfile.User.Role.MEMBER, ZonedDateTime.now());
        final BillingProfile.Id companyId1 = BillingProfile.Id.random();
        final CompanyBillingProfile company1 = CompanyBillingProfile.builder()
                .name(faker.rickAndMorty().character())
                .status(VerificationStatus.STARTED)
                .members(Set.of(bpCompanyAdmin1, bpCompanyAdmin2, bpCompanyMember1))
                .id(companyId1)
                .enabled(true)
                .kyb(Kyb.builder().ownerId(bpCompanyAdmin1.id()).id(UUID.randomUUID()).billingProfileId(companyId1).status(VerificationStatus.STARTED).build())
                .build();
        final BillingProfile.Id companyId2 = BillingProfile.Id.random();
        final CompanyBillingProfile company2 = CompanyBillingProfile.builder()
                .name(faker.rickAndMorty().character())
                .status(VerificationStatus.UNDER_REVIEW)
                .members(Set.of(bpCompanyAdmin2))
                .id(companyId1)
                .enabled(true)
                .kyb(Kyb.builder().ownerId(bpCompanyAdmin2.id()).id(UUID.randomUUID()).billingProfileId(companyId2).status(VerificationStatus.STARTED).build())
                .build();
        final BillingProfile.Id individualBpId = BillingProfile.Id.random();
        final BillingProfile.User individualAdmin = new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now());
        final IndividualBillingProfile individualBillingProfile = IndividualBillingProfile.builder()
                .id(individualBpId)
                .name(faker.rickAndMorty().character())
                .status(VerificationStatus.REJECTED)
                .kyc(Kyc.builder().status(VerificationStatus.REJECTED).ownerId(individualAdmin.id()).billingProfileId(individualBpId).id(UUID.randomUUID()).build())
                .enabled(true)
                .owner(individualAdmin)
                .build();
        final BillingProfile.Id selfEmployedBpId = BillingProfile.Id.random();
        final BillingProfile.User selfEmployedAdmin = new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now());
        final SelfEmployedBillingProfile selfEmployedBillingProfile = SelfEmployedBillingProfile.builder()
                .owner(selfEmployedAdmin)
                .status(VerificationStatus.NOT_STARTED)
                .enabled(true)
                .id(selfEmployedBpId)
                .kyb(Kyb.builder().id(UUID.randomUUID()).status(VerificationStatus.NOT_STARTED).billingProfileId(selfEmployedBpId).ownerId(selfEmployedAdmin.id()).build())
                .name(faker.name().username())
                .build();

        // When
        when(billingProfileStoragePort.findAllByCreationDate(any()))
                .thenReturn(List.of(company1, company2), List.of(individualBillingProfile, selfEmployedBillingProfile));

        billingProfileService.remindUsersToCompleteTheirBillingProfiles();

        // Then
        final var userIdCaptor = ArgumentCaptor.forClass(UserId.class);
        final var completeYourBillingProfileCaptor = ArgumentCaptor.forClass(CompleteYourBillingProfile.class);
        verify(notificationPort, times(4)).push(userIdCaptor.capture(), completeYourBillingProfileCaptor.capture());
        assertTrue(userIdCaptor.getAllValues().contains(bpCompanyAdmin1.id()));
        assertTrue(userIdCaptor.getAllValues().contains(bpCompanyAdmin2.id()));
        assertTrue(userIdCaptor.getAllValues().contains(individualAdmin.id()));
        assertTrue(userIdCaptor.getAllValues().contains(selfEmployedAdmin.id()));
        assertTrue(completeYourBillingProfileCaptor.getAllValues()
                .contains(new CompleteYourBillingProfile(new NotificationBillingProfile(company1.id(), company1.name(), company1.status()))));
        assertFalse(completeYourBillingProfileCaptor.getAllValues()
                .contains(new CompleteYourBillingProfile(new NotificationBillingProfile(company2.id(), company2.name(), company2.status()))));
        assertTrue(completeYourBillingProfileCaptor.getAllValues()
                .contains(new CompleteYourBillingProfile(new NotificationBillingProfile(individualBillingProfile.id(),
                        individualBillingProfile.name(), individualBillingProfile.status()))));
        assertTrue(completeYourBillingProfileCaptor.getAllValues()
                .contains(new CompleteYourBillingProfile(new NotificationBillingProfile(selfEmployedBillingProfile.id(),
                        selfEmployedBillingProfile.name(), selfEmployedBillingProfile.status()))));
    }
}