package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {
    private final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    private final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);
    private final BillingProfileObserver billingProfileObserver = mock(BillingProfileObserver.class);
    private final InvoiceService invoiceService = new InvoiceService(invoiceStoragePort, pdfStoragePort, billingProfileObserver);
    private final Faker faker = new Faker();
    final Invoice invoice = Invoice.of(BillingProfile.Id.random(), 1,
            new Invoice.PersonalInfo("John", "Doe", "123 Main St", "FRA"));
    final InputStream pdf = new ByteArrayInputStream(faker.lorem().paragraph().getBytes());

    @BeforeEach
    void setUp() {
        reset(invoiceStoragePort, pdfStoragePort, billingProfileObserver);
    }

    @Test
    void should_reject_if_invoice_not_found() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.empty());

        // When
        assertThatThrownBy(() -> invoiceService.update(invoice.id(), Invoice.Status.APPROVED))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Invoice %s not found".formatted(invoice.id()));
    }

    @ParameterizedTest
    @EnumSource(value = Invoice.Status.class, names = {"DRAFT", "TO_REVIEW"})
    void should_reject_if_invalid_status(Invoice.Status status) {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        // When
        assertThatThrownBy(() -> invoiceService.update(invoice.id(), status))
                // Then
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot update invoice to status %s".formatted(status));
    }

    @ParameterizedTest
    @EnumSource(value = Invoice.Status.class, names = {"APPROVED", "REJECTED"})
    void should_update_if_valid_status(Invoice.Status status) {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        // When
        invoiceService.update(invoice.id(), status);

        // Then
        final var invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceStoragePort).update(invoiceCaptor.capture());
        final var updatedInvoice = invoiceCaptor.getValue();
        assertThat(updatedInvoice.status()).isEqualTo(status);

        if (status == Invoice.Status.REJECTED) {
            verify(billingProfileObserver).onInvoiceRejected(invoice.id());
        }
    }

    @Test
    void should_do_nothing_if_status_is_null() {
        // Given
        when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));

        // When
        invoiceService.update(invoice.id(), null);

        // Then
        verify(invoiceStoragePort, never()).update(any());
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