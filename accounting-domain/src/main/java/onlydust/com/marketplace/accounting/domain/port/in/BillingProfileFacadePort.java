package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceDownload;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface BillingProfileFacadePort {

    IndividualBillingProfile createIndividualBillingProfile(@NonNull UserId owner, @NonNull String name, Set<ProjectId> selectForProjects);

    SelfEmployedBillingProfile createSelfEmployedBillingProfile(@NonNull UserId owner, @NonNull String name, Set<ProjectId> selectForProjects);

    CompanyBillingProfile createCompanyBillingProfile(@NonNull UserId firstAdmin, @NonNull String name, Set<ProjectId> selectForProjects);

    Invoice previewInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull List<RewardId> rewardIds);

    Page<Invoice> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                             final @NonNull Integer pageSize, final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction);

    void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                final @NonNull InputStream inputStream);

    void uploadExternalInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                               final String fileName, final @NonNull InputStream inputStream);

    @NonNull
    InvoiceDownload downloadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId,
                                    final @NonNull Invoice.Id invoiceId);

    void updateInvoiceMandateAcceptanceDate(UserId userId, BillingProfile.Id billingProfileId);

    List<ShortBillingProfileView> getBillingProfilesForUser(UserId userId);

    BillingProfileView getBillingProfile(BillingProfile.Id billingProfileId, UserId userId);

    PayoutInfo getPayoutInfo(BillingProfile.Id billingProfileId, UserId userId);

    void updatePayoutInfo(BillingProfile.Id billingProfileId, UserId userId, PayoutInfo payoutInfo);

    List<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, Set<BillingProfile.User.Role> roles);

    Page<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, UserId userId, int pageIndex, int pageSize);

    void inviteCoworker(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedGithubUserId, BillingProfile.User.Role role);

    void acceptCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void rejectCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void removeCoworker(BillingProfile.Id billingProfileId, UserId removeByUserId, GithubUserId removeByGithubUserId, GithubUserId githubUserId);

    void deleteBillingProfile(UserId userId, BillingProfile.Id billingProfileId);
}
