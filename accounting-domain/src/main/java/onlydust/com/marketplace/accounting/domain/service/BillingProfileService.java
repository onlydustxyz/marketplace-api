package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
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
    public InvoicePreview previewInvoice(UserId userId, BillingProfile.Id billingProfileId, List<RewardId> rewardIds) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to generate invoice for this billing profile");

        final var preview = invoiceStoragePort.preview(billingProfileId, rewardIds);
        invoiceStoragePort.deleteDraftsOf(billingProfileId);
        invoiceStoragePort.save(Invoice.of(billingProfileId, preview));

        return preview;
    }

    @Override
    public Page<Invoice> getInvoicesForBillingProfile(UserId userId, BillingProfile.Id billingProfileId) {
        return Page.<Invoice>builder()
                .content(List.of())
                .totalPageNumber(0)
                .totalItemNumber(0)
                .build();
    }

    @Override
    public void uploadInvoice(UserId userId, BillingProfile.Id billingProfileId, Invoice.Id invoiceId, InputStream data) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to upload an invoice for this billing profile");

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound("Invoice %s not found for billing profile %s".formatted(invoiceId, billingProfileId)));

        final var url = pdfStoragePort.upload(invoice.name() + ".pdf", data);
        invoiceStoragePort.save(invoice.url(url));
    }
}
