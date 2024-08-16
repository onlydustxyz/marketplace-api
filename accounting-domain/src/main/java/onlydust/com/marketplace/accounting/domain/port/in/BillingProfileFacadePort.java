package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
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

    // TODO: move to read-api
    Page<InvoiceView> invoicesOf(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber,
                                 final @NonNull Integer pageSize, final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction);

    void uploadGeneratedInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                                final @NonNull InputStream inputStream);

    void uploadExternalInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId, final @NonNull Invoice.Id invoiceId,
                               final String fileName, final @NonNull InputStream inputStream);

    @NonNull
    InvoiceDownload downloadInvoice(final @NonNull UserId userId, final @NonNull BillingProfile.Id billingProfileId,
                                    final @NonNull Invoice.Id invoiceId);

    void acceptInvoiceMandate(UserId userId, BillingProfile.Id billingProfileId);

    void updatePayoutInfo(BillingProfile.Id billingProfileId, UserId userId, PayoutInfo payoutInfo);

    Page<BillingProfileCoworkerView> getCoworkers(BillingProfile.Id billingProfileId, UserId userId, int pageIndex, int pageSize);

    void inviteCoworker(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedGithubUserId, BillingProfile.User.Role role);

    void acceptCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void rejectCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void removeCoworker(BillingProfile.Id billingProfileId, UserId removeByUserId, GithubUserId removeByGithubUserId, GithubUserId githubUserId);

    void updateCoworkerRole(BillingProfile.Id billingProfileId, UserId updatedBy, GithubUserId coworkerGithubUserId, BillingProfile.User.Role role);

    void deleteBillingProfile(UserId userId, BillingProfile.Id billingProfileId);

    void enableBillingProfile(UserId userId, BillingProfile.Id billingProfileId, Boolean enable);

    void updateBillingProfileType(BillingProfile.Id billingProfileId, UserId userId, BillingProfile.Type type);

    // TODO: move to read-api
    List<BillingProfileRewardView> getInvoiceableRewardsForBillingProfile(UserId userId, BillingProfile.Id billingProfileId);

    void remindUsersToCompleteTheirBillingProfiles();
}
