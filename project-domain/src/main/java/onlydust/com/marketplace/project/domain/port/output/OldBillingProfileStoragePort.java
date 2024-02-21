package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OldBillingProfileStoragePort {
    Optional<OldCompanyBillingProfile> findCompanyProfileForUser(UUID userId);

    void saveCompanyProfileForUser(OldCompanyBillingProfile companyBillingProfile);

    Optional<OldIndividualBillingProfile> findIndividualBillingProfile(UUID userId);

    void saveIndividualProfileForUser(OldIndividualBillingProfile individualBillingProfile);

    void saveProfileTypeForUser(OldBillingProfileType oldBillingProfileType, UUID userId);

    Optional<OldBillingProfileType> getBillingProfileTypeForUser(UUID userId);

    void updateBillingProfileType(UUID userId, OldBillingProfileType oldBillingProfileType);

    Optional<OldCompanyBillingProfile> findCompanyProfileById(UUID billingProfileId);

    Optional<OldIndividualBillingProfile> findIndividualProfileById(UUID billingProfileId);

    OldCompanyBillingProfile saveCompanyProfile(OldCompanyBillingProfile companyBillingProfile);

    OldIndividualBillingProfile saveIndividualProfile(OldIndividualBillingProfile individualBillingProfile);

    List<OldBillingProfile> all(UUID userId, Long githubUserId);

    Boolean hasValidBillingProfileForUserAndType(UUID userId, OldBillingProfileType oldBillingProfileType);

    Optional<OldCompanyBillingProfile> findCompanyByExternalVerificationId(String billingProfileExternalVerificationId);

    List<OldVerificationStatus> findKycStatusesFromParentKybExternalVerificationId(String billingProfileExternalVerificationId);

    void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, OldVerificationStatus oldVerificationStatus);
}
