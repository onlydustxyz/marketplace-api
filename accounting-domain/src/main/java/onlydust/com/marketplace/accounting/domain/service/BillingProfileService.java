package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.jetbrains.annotations.NotNull;

import javax.transaction.Transactional;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class BillingProfileService implements BillingProfileFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull AccountingBillingProfileStorage billingProfileStorage;
    private final @NonNull PdfStoragePort pdfStoragePort;


    @Override
    public IndividualBillingProfile createIndividualBillingProfile(@NotNull UserId ownerId, @NotNull String name, Set<ProjectId> selectForProjects) {
        final var billingProfile = new IndividualBillingProfile(name, ownerId);
        billingProfileStorage.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), ownerId, selectForProjects);
        return billingProfile;
    }

    @Override
    public SelfEmployedBillingProfile createSelfEmployedBillingProfile(@NotNull UserId ownerId, @NotNull String name, Set<ProjectId> selectForProjects) {
        final var billingProfile = new SelfEmployedBillingProfile(name, ownerId);
        billingProfileStorage.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), ownerId, selectForProjects);
        return billingProfile;
    }

    @Override
    public CompanyBillingProfile createCompanyBillingProfile(@NonNull UserId firstAdminId, @NonNull String name, Set<ProjectId> selectForProjects) {
        final var billingProfile = new CompanyBillingProfile(name, firstAdminId);
        billingProfileStorage.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), firstAdminId, selectForProjects);
        return billingProfile;
    }

    @Override
    @Transactional
    public Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId,
                                  final @NonNull List<RewardId> rewardIds) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to generate invoice for this billing profile");

        invoiceStoragePort.deleteDraftsOf(billingProfileId);

        final var invoice = invoiceStoragePort.preview(billingProfileId, rewardIds);

        if (invoice.rewards().stream().map(Invoice.Reward::invoiceId).anyMatch(Objects::nonNull))
            throw badRequest("Some rewards are already invoiced");

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
    @Transactional
    public void uploadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                              final @NonNull InputStream data) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to upload an invoice for this billing profile");

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound("Invoice %s not found for billing profile %s".formatted(invoiceId, billingProfileId)));

        final var url = pdfStoragePort.upload(invoiceInternalFileName(invoice), data);
        invoiceStoragePort.save(invoice.status(Invoice.Status.PROCESSING).url(url));
    }

    private void selectBillingProfileForUserAndProjects(@NonNull BillingProfile.Id billingProfileId, @NonNull UserId userId, Set<ProjectId> projectIds) {
        if (projectIds != null)
            projectIds.forEach(projectId -> billingProfileStorage.savePayoutPreference(billingProfileId, userId, projectId));
    }

    @Override
    public @NonNull InvoiceDownload downloadInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User %s is not allowed to download invoice %s of billing profile %s".formatted(userId, invoiceId, billingProfileId));

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound("Invoice %s not found for billing profile %s".formatted(invoiceId, billingProfileId)));

        final var pdf = pdfStoragePort.download(invoiceInternalFileName(invoice));
        return new InvoiceDownload(pdf, invoiceExternalFileName(invoice));
    }

    @Override
    public void updateInvoiceMandateAcceptanceDate(UserId userId, BillingProfile.Id billingProfileId) {
        if (!billingProfileStorage.isAdmin(userId, billingProfileId))
            throw unauthorized("User %s is not allowed to accept invoice mandate for billing profile %s".formatted(userId, billingProfileId));

        billingProfileStorage.updateInvoiceMandateAcceptanceDate(billingProfileId, ZonedDateTime.now());
    }

    private String invoiceExternalFileName(Invoice invoice) {
        return "%s.pdf".formatted(invoice.number());
    }

    private static String invoiceInternalFileName(Invoice invoice) {
        return "%s.pdf".formatted(invoice.id());
    }
}
