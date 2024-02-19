package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.unauthorized;

@AllArgsConstructor
public class BillingProfileService implements BillingProfileFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull AccountingBillingProfileStorage billingProfileStorage;
    private final @NonNull PdfStoragePort pdfStoragePort;

    @Override
    public Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId,
                                  final @NonNull List<RewardId> rewardIds) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to generate invoice for this billing profile");

        final var invoice = invoiceStoragePort.preview(billingProfileId, rewardIds);
        invoiceStoragePort.deleteDraftsOf(billingProfileId);
        invoiceStoragePort.save(invoice);

        return invoice;
    }

    @Override
    public Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                                    final @NonNull Integer pageSize) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to view invoices for this billing profile");

        return invoiceStoragePort.invoicesOf(billingProfileId, pageNumber, pageSize);
    }

    @Override
    public void uploadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                              final @NonNull InputStream data) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to upload an invoice for this billing profile");

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound("Invoice %s not found for billing profile %s".formatted(invoiceId, billingProfileId)));

        final var url = pdfStoragePort.upload(invoice.number() + ".pdf", data);
        invoiceStoragePort.save(invoice.status(Invoice.Status.PROCESSING).url(url));
    }
}
