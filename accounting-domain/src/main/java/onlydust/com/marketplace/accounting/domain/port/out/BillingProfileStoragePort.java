package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface BillingProfileStoragePort {
    boolean oldIsAdmin(UserId userId, BillingProfile.Id billingProfileId);

    void updateInvoiceMandateAcceptanceDate(BillingProfile.Id billingProfileId, ZonedDateTime now);

    void save(IndividualBillingProfile billingProfile);

    void save(SelfEmployedBillingProfile billingProfile);

    void save(CompanyBillingProfile billingProfile);

    void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId);

    boolean isMandateAccepted(BillingProfile.Id billingProfileId);

    Optional<ShortBillingProfileView> findIndividualBillingProfileForUser(UserId ownerId);

    List<ShortBillingProfileView> findAllBillingProfilesForUser(UserId userId);

    boolean isUserMemberOf(BillingProfile.Id billingProfileId, UserId userId);

    boolean isAdmin(BillingProfile.Id billingProfileId, UserId userId);

    Optional<BillingProfileView> findById(BillingProfile.Id billingProfileId);

    Optional<PayoutInfo> findPayoutInfoByBillingProfile(BillingProfile.Id billingProfileId);

    void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId);

    Page<BillingProfileCoworkerView> findCoworkersByBillingProfile(BillingProfile.Id billingProfileId, int pageIndex, int pageSize);

    void saveCoworkerInvitation(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedUser, BillingProfile.User.Role role,
                                ZonedDateTime invitedAt);
}
