package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileObserver;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import org.jetbrains.annotations.NotNull;

import javax.transaction.Transactional;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class BillingProfileService implements BillingProfileFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull BillingProfileStoragePort billingProfileStoragePort;
    private final @NonNull PdfStoragePort pdfStoragePort;
    private final @NonNull BillingProfileObserver billingProfileObserver;
    private final @NonNull IndexerPort indexerPort;


    @Override
    public IndividualBillingProfile createIndividualBillingProfile(@NotNull UserId ownerId, @NotNull String name, Set<ProjectId> selectForProjects) {
        billingProfileStoragePort.findIndividualBillingProfileForUser(ownerId).ifPresent(billingProfile -> {
            throw OnlyDustException.forbidden("Individual billing profile already existing for user %s".formatted(ownerId.value()));
        });
        final var billingProfile = new IndividualBillingProfile(name, ownerId);
        billingProfileStoragePort.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), ownerId, selectForProjects);
        return billingProfile;
    }

    @Override
    public SelfEmployedBillingProfile createSelfEmployedBillingProfile(@NotNull UserId ownerId, @NotNull String name, Set<ProjectId> selectForProjects) {
        final var billingProfile = new SelfEmployedBillingProfile(name, ownerId);
        billingProfileStoragePort.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), ownerId, selectForProjects);
        return billingProfile;
    }

    @Override
    public CompanyBillingProfile createCompanyBillingProfile(@NonNull UserId firstAdminId, @NonNull String name, Set<ProjectId> selectForProjects) {
        final var billingProfile = new CompanyBillingProfile(name, firstAdminId);
        billingProfileStoragePort.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), firstAdminId, selectForProjects);
        return billingProfile;
    }

    @Override
    @Transactional
    public Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull List<RewardId> rewardIds) {
        if (!billingProfileStoragePort.oldIsAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to generate invoice for this billing profile");

        invoiceStoragePort.deleteDraftsOf(billingProfileId);

        final var invoice = invoiceStoragePort.preview(billingProfileId, rewardIds);

        if (invoice.rewards().stream().map(Invoice.Reward::invoiceId).filter(Objects::nonNull)
                .anyMatch(i -> invoiceStoragePort.get(i).orElseThrow().status().isActive()))
            throw badRequest("Some rewards are already invoiced");

        invoiceStoragePort.create(invoice);

        return invoice;
    }

    @Override
    public Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                                    final @NonNull Integer pageSize, final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction) {
        if (!billingProfileStoragePort.oldIsAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to view invoices for this billing profile");

        return invoiceStoragePort.invoicesOf(billingProfileId, pageNumber, pageSize, sort, direction);
    }

    @Override
    @Transactional
    public void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                       final @NonNull InputStream data) {
        if (!billingProfileStoragePort.isMandateAccepted(billingProfileId))
            throw forbidden("Invoice mandate has not been accepted for billing profile %s".formatted(billingProfileId));

        uploadInvoice(userId, billingProfileId, invoiceId, null, Invoice.Status.APPROVED, data);
    }

    @Override
    @Transactional
    public void uploadExternalInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId, String fileName,
                                      @NonNull InputStream data) {
        if (billingProfileStoragePort.isMandateAccepted(billingProfileId))
            throw forbidden("External invoice upload is forbidden when mandate has been accepted (billing profile %s)".formatted(billingProfileId));

        uploadInvoice(userId, billingProfileId, invoiceId, fileName, Invoice.Status.TO_REVIEW, data);
    }

    @Transactional
    private void uploadInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId, String fileName,
                               @NonNull Invoice.Status status,
                               @NonNull InputStream data) {
        if (!billingProfileStoragePort.oldIsAdmin(userId, billingProfileId))
            throw unauthorized("User is not allowed to upload an invoice for this billing profile");

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound("Invoice %s not found for billing profile %s".formatted(invoiceId, billingProfileId)));

        final var url = pdfStoragePort.upload(invoice.internalFileName(), data);
        invoiceStoragePort.update(invoice
                .status(status)
                .url(url)
                .originalFileName(fileName));

        billingProfileObserver.onInvoiceUploaded(billingProfileId, invoiceId, nonNull(fileName));
    }

    private void selectBillingProfileForUserAndProjects(@NonNull BillingProfile.Id billingProfileId, @NonNull UserId userId, Set<ProjectId> projectIds) {
        if (projectIds != null) projectIds.forEach(projectId -> billingProfileStoragePort.savePayoutPreference(billingProfileId, userId, projectId));
    }

    @Override
    public @NonNull InvoiceDownload downloadInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId) {
        if (!billingProfileStoragePort.oldIsAdmin(userId, billingProfileId))
            throw unauthorized("User %s is not allowed to download invoice %s of billing profile %s".formatted(userId, invoiceId, billingProfileId));

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileId().equals(billingProfileId))
                .orElseThrow(() -> notFound(("Invoice %s not found for billing profile %s").formatted(invoiceId, billingProfileId)));

        final var pdf = pdfStoragePort.download(invoice.internalFileName());
        return new InvoiceDownload(pdf, invoice.externalFileName());
    }

    @Override
    public void updateInvoiceMandateAcceptanceDate(UserId userId, BillingProfile.Id billingProfileId) {
        if (!billingProfileStoragePort.oldIsAdmin(userId, billingProfileId))
            throw unauthorized("User %s is not allowed to accept invoice mandate for billing profile %s".formatted(userId, billingProfileId));

        billingProfileStoragePort.updateInvoiceMandateAcceptanceDate(billingProfileId, ZonedDateTime.now());
    }

    @Override
    public List<ShortBillingProfileView> getBillingProfilesForUser(UserId userId) {
        return billingProfileStoragePort.findAllBillingProfilesForUser(userId);
    }

    @Override
    public BillingProfileView getBillingProfile(BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)) {
            throw unauthorized("User %s is not a member of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        }
        return billingProfileStoragePort.findById(billingProfileId).orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId.value())));
    }

    @Override
    public PayoutInfo getPayoutInfo(BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to read payout info of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        return billingProfileStoragePort.findPayoutInfoByBillingProfile(billingProfileId).orElseGet(() -> PayoutInfo.builder().build());
    }

    @Override
    public void updatePayoutInfo(BillingProfile.Id billingProfileId, UserId userId, PayoutInfo payoutInfo) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to edit payout info of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        billingProfileStoragePort.savePayoutInfoForBillingProfile(payoutInfo, billingProfileId);
    }

    @Override
    public List<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, Set<BillingProfile.User.Role> roles) {
        return billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, roles, 0, 1_000_000).getContent();
    }

    @Override
    public Page<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, UserId userId, int pageIndex, int pageSize) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to list coworkers of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        return billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), pageIndex, pageSize);
    }

    @Override
    public void inviteCoworker(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedGithubUserId, BillingProfile.User.Role role) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, invitedBy))
            throw unauthorized("User %s must be admin to list coworkers of billing profile %s".formatted(invitedBy.value(), billingProfileId.value()));

        indexerPort.indexUser(invitedGithubUserId.value());
        billingProfileStoragePort.saveCoworkerInvitation(billingProfileId, invitedBy, invitedGithubUserId, role, ZonedDateTime.now());
    }
}
