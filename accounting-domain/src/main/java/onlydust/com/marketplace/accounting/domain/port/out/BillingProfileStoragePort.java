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
import java.util.UUID;

public interface BillingProfileStoragePort {

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

    Optional<Kyc> findKycById(UUID verificationId);

    void saveKyc(Kyc kyc);

    void updateBillingProfileStatus(BillingProfile.Id billingProfileId, VerificationStatus status);

    Optional<Kyb> findKybById(UUID verificationId);

    void saveKyb(Kyb kyb);

    List<VerificationStatus> findAllChildrenKycStatuesFromParentKyb(Kyb parentKyb);

    Optional<Kyb> findKybByParentExternalId(String parentExternalApplicantId);

    void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus);

    void saveCoworkerInvitation(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedUser, BillingProfile.User.Role role,
                                ZonedDateTime invitedAt);

    void deleteCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedUser);

    void saveCoworker(BillingProfile.Id billingProfileId, UserId invitedUser, BillingProfile.User.Role role, ZonedDateTime acceptedAt);

    Optional<BillingProfileCoworkerView> getInvitedCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);
}
