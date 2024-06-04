package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
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
    private final BillingProfileObserverPort billingProfileObserver = mock(BillingProfileObserverPort.class);
    private final InvoiceService invoiceService = new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileStoragePort, billingProfileObserver);
    private final Faker faker = new Faker();
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());
    Invoice invoice;

    @BeforeEach
    void setUp() {
        final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        final var billingProfileId = BillingProfile.Id.random();
        final var individualBillingProfile = IndividualBillingProfile.builder()
                .id(billingProfileId)
                .status(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId, UserId.random()))
                .enabled(true)
                .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build();
        invoice = Invoice.of(individualBillingProfile, 1, UserId.random(), payoutInfo);
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
    void should_update_if_rejected_status() {
        // Given
        final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
        final var billingProfileId = BillingProfile.Id.random();
        final var individualBillingProfile = IndividualBillingProfile.builder()
                .id(billingProfileId)
                .status(VerificationStatus.VERIFIED)
                .name("John")
                .kyc(newKyc(billingProfileId, UserId.random()))
                .enabled(true)
                .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build();

        final var invoice = Invoice.of(individualBillingProfile, 1, UserId.random(), payoutInfo);
        invoice.rewards(List.of(
                new Invoice.Reward(RewardId.random(), ZonedDateTime.now(), faker.rickAndMorty().character(),
                        Money.of(BigDecimal.TEN, Currency.crypto("dustyCrypto", Currency.Code.of("DSTC"), 10)),
                        Money.of(BigDecimal.TEN, Currency.crypto("dustyCrypto", Currency.Code.of("DSTC"), 10)), invoice.id(), List.of())));
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        Invoice.Status status = Invoice.Status.REJECTED;
        final String rejectionReason = faker.rickAndMorty().character();

        // When
        invoiceService.update(invoice.id(), status, rejectionReason);

        // Then
        final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceStoragePort).update(invoiceCaptor.capture());
        final var updatedInvoice = invoiceCaptor.getValue();
        assertThat(updatedInvoice.status()).isEqualTo(status);
        verify(billingProfileObserver).onInvoiceRejected(invoice.id(), rejectionReason);
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