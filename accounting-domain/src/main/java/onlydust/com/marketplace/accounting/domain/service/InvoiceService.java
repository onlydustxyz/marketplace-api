package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class InvoiceService implements InvoiceFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull PdfStoragePort pdfStoragePort;
    private final @NonNull BillingProfileStoragePort billingProfileStoragePort;
    private final @NonNull BillingProfileObserver billingProfileObserver;

    @Override
    public Optional<InvoiceView> find(Invoice.@NonNull Id id) {
        return invoiceStoragePort.getView(id);
    }

    @Override
    public Page<Invoice> findAll(@NonNull List<Invoice.Id> ids, @NonNull List<Invoice.Status> statuses, @NonNull List<Currency.Id> currencyIds,
                                 @NonNull List<BillingProfile.Type> billingProfileTypes, String search, @NonNull Integer pageIndex, @NonNull Integer pageSize) {
        return invoiceStoragePort.findAll(ids, statuses, currencyIds, billingProfileTypes, search, pageIndex, pageSize);
    }

    @Override
    public void update(Invoice.@NonNull Id id, Invoice.@NonNull Status status, String rejectionReason) {
        final var invoice = invoiceStoragePort.get(id).orElseThrow(() -> OnlyDustException.notFound("Invoice %s not found".formatted(id)));
        if (status != Invoice.Status.APPROVED && status != Invoice.Status.REJECTED)
            throw forbidden("Cannot update invoice to status %s".formatted(status));
        if (nonNull(rejectionReason) && status != Invoice.Status.REJECTED) {
            throw forbidden("Only rejected invoice can have a rejection reason");
        }
        invoiceStoragePort.update(invoice.status(status).rejectionReason(rejectionReason));
        if (status == Invoice.Status.REJECTED) {
            final var billingProfileAdmin = billingProfileStoragePort.findBillingProfileAdmin(invoice.createdBy(), invoice.billingProfileSnapshot().id())
                    .orElseThrow(() -> notFound("Billing profile admin not found for billing profile %s".formatted(invoice.billingProfileSnapshot().id())));

            billingProfileObserver.onInvoiceRejected(new InvoiceRejected(billingProfileAdmin.email(),
                    (long) invoice.rewards().size(), billingProfileAdmin.login(),
                    billingProfileAdmin.firstName(),
                    invoice.number().value(),
                    invoice.rewards().stream()
                            .map(reward -> InvoiceRejected.ShortReward.builder()
                                    .id(reward.id())
                                    .amount(reward.amount().getValue())
                                    .currencyCode(reward.amount().getCurrency().code().toString())
                                    .projectName(reward.projectName())
                                    .dollarsEquivalent(reward.target().getValue())
                                    .build()
                            ).toList(),
                    rejectionReason));
        }
    }

    @Override
    public @NonNull InvoiceDownload download(Invoice.@NonNull Id invoiceId) {
        final var invoice = invoiceStoragePort.get(invoiceId)
                .orElseThrow(() -> notFound("Invoice %s not found".formatted(invoiceId)));

        final var pdf = pdfStoragePort.download(invoice.internalFileName());
        return new InvoiceDownload(pdf, invoice.externalFileName());
    }
}
