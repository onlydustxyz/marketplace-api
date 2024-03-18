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
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
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
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User is not allowed to generate invoice for this billing profile");

        if (!billingProfileStoragePort.isEnabled(billingProfileId))
            throw unauthorized("Cannot generate invoice on a disabled billing profile");

        final var rewards = invoiceStoragePort.findRewards(rewardIds);
        if (rewards.stream().map(Invoice.Reward::invoiceId).filter(Objects::nonNull)
                .anyMatch(i -> invoiceStoragePort.get(i).orElseThrow().status().isActive())) {
            throw badRequest("Some rewards are already invoiced");
        }

        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        if (!billingProfile.isVerified()) {
            throw badRequest("Billing profile %s is not verified".formatted(billingProfileId));
        }

        final int sequenceNumber = invoiceStoragePort.getNextSequenceNumber(billingProfileId);
        final var invoice = Invoice.of(billingProfile, sequenceNumber, userId).rewards(rewards);

        invoiceStoragePort.deleteDraftsOf(billingProfileId);
        invoiceStoragePort.create(invoice);
        return invoice;
    }

    @Override
    public Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                                    final @NonNull Integer pageSize, final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User is not allowed to view invoices for this billing profile");

        return invoiceStoragePort.invoicesOf(billingProfileId, pageNumber, pageSize, sort, direction);
    }

    @Override
    @Transactional
    public void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                       final @NonNull InputStream data) {
        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        if (!billingProfileStoragePort.isEnabled(billingProfileId))
            throw unauthorized("Cannot upload an invoice on a disabled billing profile %s".formatted(billingProfileId.value()));

        if (!billingProfile.isInvoiceMandateAccepted())
            throw forbidden("Invoice mandate has not been accepted for billing profile %s".formatted(billingProfileId));

        uploadInvoice(userId, billingProfileId, invoiceId, null, Invoice.Status.APPROVED, data);
    }

    @Override
    @Transactional
    public void uploadExternalInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId, String fileName,
                                      @NonNull InputStream data) {
        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        if (!billingProfileStoragePort.isEnabled(billingProfileId))
            throw unauthorized("Cannot upload an invoice on a disabled billing profile %s".formatted(billingProfileId.value()));

        if (billingProfile.isInvoiceMandateAccepted())
            throw forbidden("External invoice upload is forbidden when mandate has been accepted (billing profile %s)".formatted(billingProfileId));

        uploadInvoice(userId, billingProfileId, invoiceId, fileName, Invoice.Status.TO_REVIEW, data);
    }

    @Transactional
    private void uploadInvoice(@NonNull UserId userId, BillingProfile.@NonNull Id billingProfileId, Invoice.@NonNull Id invoiceId, String fileName,
                               @NonNull Invoice.Status status,
                               @NonNull InputStream data) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User is not allowed to upload an invoice for this billing profile");

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileSnapshot().id().equals(billingProfileId))
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
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s is not allowed to download invoice %s of billing profile %s".formatted(userId, invoiceId, billingProfileId));

        final var invoice = invoiceStoragePort.get(invoiceId)
                .filter(i -> i.billingProfileSnapshot().id().equals(billingProfileId))
                .orElseThrow(() -> notFound(("Invoice %s not found for billing profile %s").formatted(invoiceId, billingProfileId)));

        final var pdf = pdfStoragePort.download(invoice.internalFileName());
        return new InvoiceDownload(pdf, invoice.externalFileName());
    }

    @Override
    public void updateInvoiceMandateAcceptanceDate(UserId userId, BillingProfile.Id billingProfileId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s is not allowed to accept invoice mandate for billing profile %s".formatted(userId, billingProfileId));
        if (!billingProfileStoragePort.isEnabled(billingProfileId))
            throw unauthorized("Cannot update mandateAcceptanceDate on a disabled billing profile %s".formatted(billingProfileId));
        billingProfileStoragePort.updateInvoiceMandateAcceptanceDate(billingProfileId, ZonedDateTime.now());
    }

    @Override
    public List<ShortBillingProfileView> getBillingProfilesForUser(UserId userId) {
        return billingProfileStoragePort.findAllBillingProfilesForUser(userId);
    }

    @Override
    public BillingProfileView getBillingProfile(BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)) {
            throw unauthorized("User %s is not a member of billing profile %s".formatted(userId, billingProfileId));
        }
        final BillingProfileView billingProfileView = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        final BillingProfileUserRightsView billingProfileUserRightsView = billingProfileStoragePort.getUserRightsOnBillingProfile(billingProfileId, userId)
                .orElseThrow(() -> internalServerError("User %s rights on billing profile %s were not found".formatted(userId, billingProfileId)));
        if (billingProfileUserRightsView.role() == BillingProfile.User.Role.MEMBER) {
            return billingProfileView.toBuilder()
                    .me(billingProfileUserRightsView)
                    .payoutInfo(null)
                    .build();
        }
        return billingProfileView.toBuilder()
                .me(billingProfileUserRightsView)
                .build();
    }

    @Override
    public PayoutInfo getPayoutInfo(BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to read payout info of billing profile %s".formatted(userId, billingProfileId));
        return billingProfileStoragePort.findPayoutInfoByBillingProfile(billingProfileId).orElseGet(() -> PayoutInfo.builder().build());
    }

    @Override
    public void updatePayoutInfo(BillingProfile.Id billingProfileId, UserId userId, PayoutInfo payoutInfo) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to edit payout info of billing profile %s".formatted(userId, billingProfileId));
        billingProfileStoragePort.savePayoutInfoForBillingProfile(payoutInfo, billingProfileId);
    }

    @Override
    public List<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, Set<BillingProfile.User.Role> roles) {
        return billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, roles, 0, 1_000_000).getContent();
    }

    @Override
    public Page<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, UserId userId, int pageIndex, int pageSize) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to list coworkers of billing profile %s".formatted(userId, billingProfileId));
        return billingProfileStoragePort.findCoworkersByBillingProfile(billingProfileId, BillingProfile.User.Role.all(), pageIndex, pageSize);
    }

    @Override
    @Transactional
    public void inviteCoworker(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedGithubUserId, BillingProfile.User.Role role) {
        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        if (!billingProfileStoragePort.isAdmin(billingProfileId, invitedBy)) {
            throw unauthorized("User %s must be admin to invite coworker to billing profile %s".formatted(invitedBy, billingProfileId));
        }
        if (List.of(BillingProfile.Type.INDIVIDUAL, BillingProfile.Type.SELF_EMPLOYED).contains(billingProfile.getType())) {
            throw unauthorized("Cannot invite coworker on individual or self employed billing profile %s".formatted(billingProfile.getId()));
        }
        indexerPort.indexUser(invitedGithubUserId.value());
        billingProfileStoragePort.saveCoworkerInvitation(billingProfileId, invitedBy, invitedGithubUserId, role, ZonedDateTime.now());
    }

    @Override
    @Transactional
    public void acceptCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        final BillingProfileCoworkerView invited = billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)
                .orElseThrow(() -> notFound("Invitation not found for billing profile %s and user %s"
                        .formatted(billingProfileId, invitedGithubUserId)));
        billingProfileStoragePort.saveCoworker(billingProfileId, invited.userId(), invited.role(), ZonedDateTime.now());
        billingProfileStoragePort.acceptCoworkerInvitation(billingProfileId, invitedGithubUserId);
    }

    @Override
    @Transactional
    public void rejectCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        billingProfileStoragePort.getInvitedCoworker(billingProfileId, invitedGithubUserId)
                .orElseThrow(() -> notFound("Invitation not found for billing profile %s and user %s"
                        .formatted(billingProfileId, invitedGithubUserId)));

        billingProfileStoragePort.deleteCoworkerInvitation(billingProfileId, invitedGithubUserId);
    }

    @Override
    @Transactional
    public void removeCoworker(BillingProfile.Id billingProfileId, UserId removeByUserId, GithubUserId removeByGithubUserId, GithubUserId githubUserId) {
        if (!removeByGithubUserId.equals(githubUserId) && !billingProfileStoragePort.isAdmin(billingProfileId, removeByUserId))
            throw unauthorized("User %s must be admin to remove coworker %s from billing profile %s".formatted(removeByUserId, githubUserId, billingProfileId));

        final var coworker = billingProfileStoragePort.getCoworker(billingProfileId, githubUserId)
                .orElseThrow(() -> notFound("Coworker %s not found for billing profile %s"
                        .formatted(githubUserId, billingProfileId)));

        if (!coworker.removable())
            throw forbidden("Coworker %s cannot be removed from billing profile %s".formatted(githubUserId, billingProfileId));

        if (coworker.hasJoined()) {
            billingProfileStoragePort.deleteCoworker(billingProfileId, coworker.userId());
        }
        billingProfileStoragePort.deleteCoworkerInvitation(billingProfileId, coworker.githubUserId());
    }

    @Override
    public void deleteBillingProfile(UserId userId, BillingProfile.Id billingProfileId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to delete billing profile %s".formatted(userId.value(), billingProfileId.value()));
        if (billingProfileStoragePort.doesBillingProfileHaveSomeInvoices(billingProfileId))
            throw unauthorized("Cannot delete billing profile %s with invoice(s)".formatted(billingProfileId.value()));
        billingProfileStoragePort.deleteBillingProfile(billingProfileId);
    }

    @Override
    public void enableBillingProfile(UserId userId, BillingProfile.Id billingProfileId, Boolean enabled) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to enable billing profile %s".formatted(userId.value(), billingProfileId.value()));
        billingProfileStoragePort.enableBillingProfile(billingProfileId, enabled);
    }
}
