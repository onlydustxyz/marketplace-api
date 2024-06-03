package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.kernel.port.output.IndexerPort;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class BillingProfileService implements BillingProfileFacadePort {
    private final @NonNull InvoiceStoragePort invoiceStoragePort;
    private final @NonNull BillingProfileStoragePort billingProfileStoragePort;
    private final @NonNull PdfStoragePort pdfStoragePort;
    private final @NonNull BillingProfileObserverPort billingProfileObserver;
    private final @NonNull IndexerPort indexerPort;
    private final @NonNull AccountingObserverPort accountingObserverPort;
    private final @NonNull AccountingFacadePort accountingFacadePort;
    private final @NonNull PayoutInfoValidator payoutInfoValidator;


    @Override
    public IndividualBillingProfile createIndividualBillingProfile(@NonNull UserId ownerId, @NonNull String name, Set<ProjectId> selectForProjects) {
        billingProfileStoragePort.findIndividualBillingProfileForUser(ownerId).ifPresent(billingProfile -> {
            throw OnlyDustException.forbidden("Individual billing profile already existing for user %s".formatted(ownerId.value()));
        });
        final var billingProfile = new IndividualBillingProfile(name, ownerId);
        billingProfileStoragePort.save(billingProfile);
        selectBillingProfileForUserAndProjects(billingProfile.id(), ownerId, selectForProjects);
        return billingProfile;
    }

    @Override
    public SelfEmployedBillingProfile createSelfEmployedBillingProfile(@NonNull UserId ownerId, @NonNull String name, Set<ProjectId> selectForProjects) {
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

        final var rewards = invoiceStoragePort.findRewards(rewardIds)
                .stream().map(r -> r.withNetworks(accountingFacadePort.networksOf(r.amount().getCurrency().id(), r.id())))
                .toList();

        final var billingProfile = billingProfileStoragePort.findById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        if (!billingProfile.isVerified())
            throw badRequest("Billing profile %s is not verified".formatted(billingProfileId));

        final var payoutInfo = billingProfileStoragePort.getPayoutInfo(billingProfileId)
                .orElseThrow(() -> internalServerError("An invoice can only be created on a billing profile with payout info (billing profile %s)".formatted(billingProfileId)));

        final int sequenceNumber = invoiceStoragePort.getNextSequenceNumber(billingProfileId);
        final var invoice = (switch (billingProfile.type()) {
            case INDIVIDUAL -> Invoice.of((IndividualBillingProfile) billingProfile, sequenceNumber, userId, payoutInfo);
            case COMPANY -> Invoice.of((CompanyBillingProfile) billingProfile, sequenceNumber, userId, payoutInfo);
            case SELF_EMPLOYED -> Invoice.of((SelfEmployedBillingProfile) billingProfile, sequenceNumber, userId, payoutInfo);
        }).rewards(rewards);

        checkInvoicePreviewRewards(invoice);

        invoiceStoragePort.deleteDraftsOf(billingProfileId);
        invoiceStoragePort.create(invoice);
        return invoice;
    }

    private void checkInvoicePreviewRewards(Invoice invoice) {
        final var rewards = invoiceStoragePort.getRewardAssociations(invoice.rewards().stream().map(Invoice.Reward::id).toList());
        if (rewards.size() != invoice.rewards().size()) {
            throw notFound("Some invoice's rewards were not found (invoice %s). This may happen if a reward was cancelled in the meantime.".formatted(invoice.id()));
        }
        if (rewards.stream().anyMatch(r -> !r.status().isPendingRequest())) {
            throw badRequest("Some rewards don't have the PENDING_REQUEST status");
        }
        if (rewards.stream().anyMatch(r -> nonNull(r.invoiceId()) && r.invoiceStatus() != Invoice.Status.DRAFT && r.invoiceStatus() != Invoice.Status.REJECTED)) {
            throw badRequest("Some rewards are already invoiced");
        }
        if (rewards.stream().anyMatch(r -> isNull(r.billingProfileId()) || !r.billingProfileId().equals(invoice.billingProfileSnapshot().id()))) {
            throw badRequest("Some rewards are not associated with billing profile %s".formatted(invoice.billingProfileSnapshot().id()));
        }
    }

    @Override
    public Page<InvoiceView> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                                        final @NonNull Integer pageSize, final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User is not allowed to view invoices for this billing profile");

        return invoiceStoragePort.invoicesOf(billingProfileId, pageNumber, pageSize, sort, direction);
    }

    @Override
    @Transactional
    public void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                       final @NonNull InputStream data) {
        final var billingProfile = billingProfileStoragePort.findViewById(billingProfileId)
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
        final var billingProfile = billingProfileStoragePort.findViewById(billingProfileId)
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
        if (invoice.status() != Invoice.Status.DRAFT)
            throw badRequest("Invoice %s is not in DRAFT status".formatted(invoiceId));

        checkInvoiceRewards(invoice);

        final var url = pdfStoragePort.upload(invoice.internalFileName(), data);
        invoiceStoragePort.update(invoice
                .status(status)
                .url(url)
                .originalFileName(fileName));

        billingProfileObserver.onInvoiceUploaded(billingProfileId, invoiceId, nonNull(fileName));
    }

    private void checkInvoiceRewards(Invoice invoice) {
        final var rewards = invoiceStoragePort.getRewardAssociations(invoice.rewards().stream().map(Invoice.Reward::id).toList());
        if (rewards.size() != invoice.rewards().size()) {
            throw notFound("Some invoice's rewards were not found (invoice %s). This may happen if a reward was cancelled in the meantime.".formatted(invoice.id()));
        }
        if (rewards.stream().anyMatch(r -> isNull(r.invoiceId()) || !r.invoiceId().equals(invoice.id()))) {
            throw badRequest("Some rewards are not associated with invoice %s".formatted(invoice.id()));
        }
        if (rewards.stream().anyMatch(r -> isNull(r.billingProfileId()) || !r.billingProfileId().equals(invoice.billingProfileSnapshot().id()))) {
            throw badRequest("Some rewards are not associated with billing profile %s".formatted(invoice.billingProfileSnapshot().id()));
        }
    }

    private void selectBillingProfileForUserAndProjects(@NonNull BillingProfile.Id billingProfileId, @NonNull UserId userId, Set<ProjectId> projectIds) {
        if (projectIds != null)
            projectIds.forEach(projectId -> {
                billingProfileStoragePort.savePayoutPreference(billingProfileId, userId, projectId);
                accountingObserverPort.onPayoutPreferenceChanged(billingProfileId, userId, projectId);
            });
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
    public BillingProfileView getBillingProfile(BillingProfile.Id billingProfileId, UserId userId, GithubUserId githubUserId) {
        if (!billingProfileStoragePort.isUserMemberOf(billingProfileId, userId) &&
            !billingProfileStoragePort.isUserInvitedTo(billingProfileId, githubUserId)) {
            throw unauthorized("User %s is not a member of billing profile %s".formatted(userId, billingProfileId));
        }
        return getBillingProfileViewWithUserRights(billingProfileId, userId);
    }

    private BillingProfileView getBillingProfileViewWithUserRights(BillingProfile.Id billingProfileId, UserId userId) {
        final BillingProfileView billingProfileView = billingProfileStoragePort.findViewById(billingProfileId)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        final BillingProfileUserRightsView billingProfileUserRightsView = billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId)
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
    public PayoutInfoView getPayoutInfo(BillingProfile.Id billingProfileId, UserId userId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to read payout info of billing profile %s".formatted(userId, billingProfileId));
        return billingProfileStoragePort.findPayoutInfoByBillingProfile(billingProfileId).orElseGet(() -> PayoutInfoView.builder().build());
    }

    @Override
    public void updatePayoutInfo(BillingProfile.Id billingProfileId, UserId userId, PayoutInfo payoutInfo) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to edit payout info of billing profile %s".formatted(userId, billingProfileId));
        payoutInfoValidator.validate(payoutInfo);
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
        final var billingProfile = billingProfileStoragePort.findViewById(billingProfileId)
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
        if (coworker.hasBeenInvited()) {
            billingProfileStoragePort.deleteCoworkerInvitation(billingProfileId, coworker.githubUserId());
        }
    }

    @Override
    public void updateCoworkerRole(BillingProfile.Id billingProfileId, UserId updatedBy, GithubUserId coworkerGithubUserId, BillingProfile.User.Role role) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, updatedBy))
            throw unauthorized("User %s must be admin to manage billing profile %s coworkers".formatted(updatedBy, billingProfileId));

        final var coworker = billingProfileStoragePort.getCoworker(billingProfileId, coworkerGithubUserId)
                .orElseThrow(() -> notFound("Coworker %s not found for billing profile %s"
                        .formatted(coworkerGithubUserId, billingProfileId)));

        if (role == BillingProfile.User.Role.MEMBER && !coworker.downgradable())
            throw badRequest("Cannot downgrade user %s of billing profile %s".formatted(coworker.userId(), billingProfileId));

        if (coworker.joinedAt() == null)
            billingProfileStoragePort.updateCoworkerInvitationRole(billingProfileId, coworkerGithubUserId, role);
        else
            billingProfileStoragePort.updateCoworkerRole(billingProfileId, coworker.userId(), role);
    }

    @Override
    @Transactional
    public void deleteBillingProfile(UserId userId, BillingProfile.Id billingProfileId) {
        final var userRights = billingProfileStoragePort.getUserRightsForBillingProfile(billingProfileId, userId)
                .orElseThrow(() -> internalServerError("User %s rights on billing profile %s were not found".formatted(userId, billingProfileId)));

        if (!userRights.canDelete())
            throw unauthorized("User %s cannot delete billing profile %s".formatted(userId.value(), billingProfileId.value()));

        accountingObserverPort.onBillingProfileDeleted(billingProfileId);
        billingProfileStoragePort.deleteBillingProfile(billingProfileId);
    }

    @Override
    public void enableBillingProfile(UserId userId, BillingProfile.Id billingProfileId, Boolean enabled) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to enable billing profile %s".formatted(userId.value(), billingProfileId.value()));
        billingProfileStoragePort.updateEnableBillingProfile(billingProfileId, enabled);
        accountingObserverPort.onBillingProfileEnableChanged(billingProfileId, enabled);
    }

    @Override
    public void updateBillingProfileType(BillingProfile.Id billingProfileId, UserId userId, BillingProfile.Type type) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to modify billing profile %s type to %s".formatted(userId.value(), billingProfileId.value(), type));
        if (type == BillingProfile.Type.INDIVIDUAL) {
            throw unauthorized("User %s cannot update billing profile %s to type INDIVIDUAL".formatted(userId, billingProfileId));
        }
        final BillingProfileView billingProfileViewWithUserRights = getBillingProfileViewWithUserRights(billingProfileId, userId);
        if (type == BillingProfile.Type.COMPANY && billingProfileViewWithUserRights.getType() == BillingProfile.Type.SELF_EMPLOYED) {
            billingProfileStoragePort.updateBillingProfileType(billingProfileId, type);
        } else if (type == BillingProfile.Type.SELF_EMPLOYED && billingProfileViewWithUserRights.isSwitchableToSelfEmployed()) {
            billingProfileStoragePort.updateBillingProfileType(billingProfileId, type);
        } else {
            throw internalServerError("User %s cannot update billing profile %s of type %s to type %s".formatted(
                    userId, billingProfileId, billingProfileViewWithUserRights.getType(), type
            ));
        }
    }

    @Override
    public List<BillingProfileRewardView> getInvoiceableRewardsForBillingProfile(UserId userId, BillingProfile.Id billingProfileId) {
        if (!billingProfileStoragePort.isAdmin(billingProfileId, userId))
            throw unauthorized("User %s must be admin to get invoiceable rewards of billing profile %s".formatted(userId.value(), billingProfileId.value()));
        return billingProfileStoragePort.findInvoiceableRewardsForBillingProfile(billingProfileId);
    }

    @Override
    public BillingProfileView getById(BillingProfile.Id id) {
        return billingProfileStoragePort.findViewById(id)
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(id)));
    }
}
