package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileStoragePort {
    Optional<OldCompanyBillingProfile> findCompanyProfileForUser(UUID userId);

    void saveCompanyProfileForUser(OldCompanyBillingProfile companyBillingProfile);

    Optional<OldIndividualBillingProfile> findIndividualBillingProfile(UUID userId);

    void saveIndividualProfileForUser(OldIndividualBillingProfile individualBillingProfile);

    void saveProfileTypeForUser(BillingProfileType billingProfileType, UUID userId);

    Optional<BillingProfileType> getBillingProfileTypeForUser(UUID userId);

    void updateBillingProfileType(UUID userId, BillingProfileType billingProfileType);

    Optional<OldCompanyBillingProfile> findCompanyProfileById(UUID billingProfileId);

    Optional<OldIndividualBillingProfile> findIndividualProfileById(UUID billingProfileId);

    OldCompanyBillingProfile saveCompanyProfile(OldCompanyBillingProfile companyBillingProfile);

    OldIndividualBillingProfile saveIndividualProfile(OldIndividualBillingProfile individualBillingProfile);

    List<BillingProfile> all(UUID userId, Long githubUserId);

    Boolean hasValidBillingProfileForUserAndType(UUID userId, BillingProfileType billingProfileType);

    Optional<OldCompanyBillingProfile> findCompanyByExternalVerificationId(String billingProfileExternalVerificationId);

    List<VerificationStatus> findKycStatusesFromParentKybExternalVerificationId(String billingProfileExternalVerificationId);

    void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus);
}
