package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {
    private final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    private final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);
    private final BillingProfileStoragePort billingProfileStoragePort = mock(BillingProfileStoragePort.class);
    private final BillingProfileObserver billingProfileObserver = mock(BillingProfileObserver.class);
    private final InvoiceService invoiceService = new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileStoragePort, billingProfileObserver);
    private final Faker faker = new Faker();
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());
    Invoice invoice;

    @BeforeEach
    void setUp() {
        final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        final var billingProfileId = BillingProfile.Id.random();
        final var individualBillingProfile = BillingProfileView.builder()
                .id(billingProfileId)
                .type(BillingProfile.Type.INDIVIDUAL)
                .payoutInfo(payoutInfo)
                .verificationStatus(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId, UserId.random()))
                .build();
        invoice = Invoice.of(individualBillingProfile, 1, UserId.random());
        reset(invoiceStoragePort, pdfStoragePort, billingProfileObserver);
    }

    @Test
    void should_reject_if_invoice_not_found() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> invoiceService.update(invoice.id(), Invoice.Status.APPROVED, null))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Invoice %s not found".formatted(invoice.id()));
    }

    @Test
    void should_reject_if_rejection_reason_on_approved_status() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.update(invoice.id(), Invoice.Status.APPROVED, faker.rickAndMorty().character()))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Only rejected invoice can have a rejection reason");
    }

    @ParameterizedTest
    @EnumSource(value = Invoice.Status.class, names = {"DRAFT", "TO_REVIEW"})
    void should_reject_if_invalid_status(Invoice.Status status) {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        // When
        assertThatThrownBy(() -> invoiceService.update(invoice.id(), status, null))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot update invoice to status %s".formatted(status));
    }

    @Test
    void should_update_if_approved_status() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
        final var status = Invoice.Status.APPROVED;

        // When
        invoiceService.update(invoice.id(), status, null);

        // Then
        final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceStoragePort).update(invoiceCaptor.capture());
        final var updatedInvoice = invoiceCaptor.getValue();
        assertThat(updatedInvoice.status()).isEqualTo(status);
    }

    @Test
    void should_update_if_rejected_status_and_not_send_notification_given_a_billing_profile_admin_not_found() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
        Invoice.Status status = Invoice.Status.REJECTED;
        final String rejectionReason = faker.rickAndMorty().character();

        // When
        assertThatThrownBy(() -> invoiceService.update(invoice.id(), status, rejectionReason))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Billing profile admin not found for billing profile %s".formatted(invoice.billingProfileSnapshot().id()));
    }

    @Test
    void should_update_if_rejected_status() {
        // Given
        final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        final var billingProfileId = BillingProfile.Id.random();
        final var individualBillingProfile = BillingProfileView.builder()
                .id(billingProfileId)
                .type(BillingProfile.Type.INDIVIDUAL)
                .payoutInfo(payoutInfo)
                .verificationStatus(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId, UserId.random()))
                .build();

        final var invoiceCreatorId = UserId.random();
        final var invoiceCreator = BillingProfileCoworkerView.builder()
                .login(faker.name().username())
                .email(faker.internet().emailAddress())
                .firstName(faker.name().firstName())
                .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                .role(BillingProfile.User.Role.ADMIN)
                .joinedAt(ZonedDateTime.now())
                .invitedAt(null)
                .rewardCount(0)
                .billingProfileAdminCount(1)
                .build();
        final var invoice = Invoice.of(individualBillingProfile, 1, invoiceCreatorId);
        invoice.rewards(List.of(
                new Invoice.Reward(RewardId.random(), ZonedDateTime.now(), faker.rickAndMorty().character(),
                        Money.of(BigDecimal.TEN, Currency.crypto("dustyCrypto", Currency.Code.of("DSTC"), 10)),
                        Money.of(BigDecimal.TEN, Currency.crypto("dustyCrypto", Currency.Code.of("DSTC"), 10)), invoice.id(), List.of())));
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
        Invoice.Status status = Invoice.Status.REJECTED;
        final String rejectionReason = faker.rickAndMorty().character();

        // When
        when(billingProfileStoragePort.findBillingProfileAdmin(invoiceCreatorId, invoice.billingProfileSnapshot().id()))
                .thenReturn(Optional.of(invoiceCreator));
        invoiceService.update(invoice.id(), status, rejectionReason);

        // Then
        final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceStoragePort).update(invoiceCaptor.capture());
        final var updatedInvoice = invoiceCaptor.getValue();
        assertThat(updatedInvoice.status()).isEqualTo(status);
        final ArgumentCaptor<InvoiceRejected> rejectedArgumentCaptor = ArgumentCaptor.forClass(InvoiceRejected.class);
        verify(billingProfileObserver).onInvoiceRejected(rejectedArgumentCaptor.capture());
        assertThat(rejectedArgumentCaptor.getValue().rejectionReason()).isEqualTo(rejectionReason);
        assertThat(rejectedArgumentCaptor.getValue().invoiceName()).isEqualTo(invoice.number().value());
        assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminEmail()).isEqualTo(invoiceCreator.email());
        assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminFirstName()).isEqualTo(invoiceCreator.firstName());
        assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminGithubLogin()).isEqualTo(invoiceCreator.login());
        assertThat(rejectedArgumentCaptor.getValue().rewardCount()).isEqualTo(invoice.rewards().size());
        assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).projectName()).isEqualTo(invoice.rewards().get(0).projectName());
        assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).amount()).isEqualTo(invoice.rewards().get(0).amount().getValue());
        assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).currencyCode()).isEqualTo(invoice.rewards().get(0).amount().getCurrency().code().toString());
    }


    @SneakyThrows
    @Test
    void should_download_invoice() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
        when(pdfStoragePort.download(invoice.id() + ".pdf")).thenReturn(pdf);

        // When
        final var invoiceDownload = invoiceService.download(invoice.id());

        // Then
        assertThat(invoiceDownload.fileName()).isEqualTo(invoice.number() + ".pdf");
        assertThat(invoiceDownload.data()).isEqualTo(pdf);
    }
}