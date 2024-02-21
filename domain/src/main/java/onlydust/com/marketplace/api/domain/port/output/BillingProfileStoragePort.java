package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingProfileStoragePort {
    Optional<CompanyBillingProfile> findCompanyProfileForUser(UUID userId);

    void saveCompanyProfileForUser(CompanyBillingProfile companyBillingProfile);

    Optional<IndividualBillingProfile> findIndividualBillingProfile(UUID userId);

    void saveIndividualProfileForUser(IndividualBillingProfile individualBillingProfile);

    void saveProfileTypeForUser(BillingProfileType billingProfileType, UUID userId);

    Optional<BillingProfileType> getBillingProfileTypeForUser(UUID userId);

    void updateBillingProfileType(UUID userId, BillingProfileType billingProfileType);

    Optional<CompanyBillingProfile> findCompanyProfileById(UUID billingProfileId);

    Optional<IndividualBillingProfile> findIndividualProfileById(UUID billingProfileId);

    CompanyBillingProfile saveCompanyProfile(CompanyBillingProfile companyBillingProfile);

    IndividualBillingProfile saveIndividualProfile(IndividualBillingProfile individualBillingProfile);

    List<BillingProfile> all(UUID userId, Long githubUserId);

    Boolean hasValidBillingProfileForUserAndType(UUID userId, BillingProfileType billingProfileType);

    Optional<CompanyBillingProfile> findCompanyByExternalVerificationId(String billingProfileExternalVerificationId);

    List<VerificationStatus> findKycStatusesFromParentKybExternalVerificationId(String billingProfileExternalVerificationId);

    void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus);
}
