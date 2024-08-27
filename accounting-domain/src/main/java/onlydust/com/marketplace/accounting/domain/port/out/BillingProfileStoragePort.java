package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileRewardView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileUserRightsView;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BillingProfileStoragePort {
    void save(BillingProfile billingProfile);

    void save(IndividualBillingProfile billingProfile);

    void save(SelfEmployedBillingProfile billingProfile);

    void save(CompanyBillingProfile billingProfile);

    void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId);

    boolean individualBillingProfileExistsByUserId(UserId ownerId);

    boolean isUserMemberOf(BillingProfile.Id billingProfileId, UserId userId);

    Optional<BillingProfile> findById(BillingProfile.Id billingProfileId);

    void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId);

    Page<BillingProfileCoworkerView> findCoworkersByBillingProfile(@NonNull BillingProfile.Id billingProfileId, @NonNull Set<BillingProfile.User.Role> roles,
                                                                   int pageIndex,
                                                                   int pageSize);

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

    void updateCoworkerRole(BillingProfile.Id billingProfileId, UserId userId, BillingProfile.User.Role role);

    void updateCoworkerInvitationRole(BillingProfile.Id billingProfileId, GithubUserId invitedUser, BillingProfile.User.Role role);

    Optional<BillingProfileCoworkerView> getInvitedCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    Optional<BillingProfileCoworkerView> getCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void deleteCoworker(BillingProfile.Id billingProfileId, UserId userId);

    Optional<BillingProfileCoworkerView> findBillingProfileAdmin(UserId userId, BillingProfile.Id billingProfileId);

    void deleteBillingProfile(BillingProfile.Id billingProfileId);

    void updateEnableBillingProfile(BillingProfile.Id billingProfileId, Boolean enabled);

    Optional<BillingProfileUserRightsView> getUserRightsForBillingProfile(BillingProfile.Id billingProfileId, UserId userId);

    void acceptCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId);

    void updateBillingProfileType(BillingProfile.Id billingProfileId, BillingProfile.Type type);

    List<BillingProfileRewardView> findInvoiceableRewardsForBillingProfile(BillingProfile.Id billingProfileId);

    Optional<ShortContributorView> getBillingProfileOwnerById(UserId ownerId);

    Optional<PayoutInfo> getPayoutInfo(BillingProfile.Id billingProfileId);

    List<BillingProfile> findAllByCreationDate(ZonedDateTime creationDate);

    Optional<String> findExternalRejectionReason(String groupId, String buttonId, String label);
}
