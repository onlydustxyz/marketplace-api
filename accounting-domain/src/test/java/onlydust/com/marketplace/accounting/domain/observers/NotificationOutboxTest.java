package onlydust.com.marketplace.accounting.domain.observers;

import onlydust.com.marketplace.accounting.domain.events.InvoiceUploaded;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationOutboxTest {
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final NotificationOutbox notificationOutbox = new NotificationOutbox(outbox);

    @BeforeEach
    void setup() {
        reset(outbox);
    }

    @Test
    void should_publish_an_event_upon_invoice_upload() {
        // Given
        final var billingProfileId = BillingProfile.Id.random();
        final var invoiceId = Invoice.Id.random();
        final var isExternal = true;

        // When
        notificationOutbox.onInvoiceUploaded(billingProfileId, invoiceId, isExternal);

        // Then
        final var eventCaptor = ArgumentCaptor.forClass(InvoiceUploaded.class);
        verify(outbox).push(eventCaptor.capture());
        final var event = eventCaptor.getValue();
        assertThat(event.billingProfileId()).isEqualTo(billingProfileId);
        assertThat(event.invoiceId()).isEqualTo(invoiceId);
        assertThat(event.isExternal()).isEqualTo(isExternal);
    }
}