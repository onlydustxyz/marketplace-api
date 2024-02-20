package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {

    private final UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    private final CompanyBillingProfileRepository companyBillingProfileRepository;
    private final IndividualBillingProfileRepository individualBillingProfileRepository;
    private final CustomUserRewardRepository userRewardRepository;
    private final ChildrenKycRepository childrenKycRepository;
    private final GlobalSettingsRepository globalSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyBillingProfile> findCompanyProfileForUser(UUID userId) {
        return companyBillingProfileRepository.findByUserId(userId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public void saveCompanyProfileForUser(CompanyBillingProfile companyBillingProfile) {
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndividualBillingProfile> findIndividualBillingProfile(UUID userId) {
        return individualBillingProfileRepository.findByUserId(userId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public void saveIndividualProfileForUser(IndividualBillingProfile individualBillingProfile) {
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile));
    }

    @Override
    @Transactional
    public void saveProfileTypeForUser(BillingProfileType billingProfileType, UUID userId) {
        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(switch (billingProfileType) {
                    case COMPANY -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY;
                    case INDIVIDUAL -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL;
                })
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileType> getBillingProfileTypeForUser(UUID userId) {
        return userBillingProfileTypeRepository.findById(userId).map(entity -> switch (entity.getBillingProfileType()) {
            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
            case COMPANY -> BillingProfileType.COMPANY;
        });
    }

    @Override
    @Transactional
    public void updateBillingProfileType(UUID userId, BillingProfileType billingProfileType) {
        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(switch (billingProfileType) {
                    case COMPANY -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY;
                    case INDIVIDUAL -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL;
                })
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyBillingProfile> findCompanyProfileById(UUID billingProfileId) {
        return companyBillingProfileRepository.findById(billingProfileId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndividualBillingProfile> findIndividualProfileById(UUID billingProfileId) {
        return individualBillingProfileRepository.findById(billingProfileId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public CompanyBillingProfile saveCompanyProfile(CompanyBillingProfile companyBillingProfile) {
        return companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile))
                .toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate());
    }

    @Override
    @Transactional
    public IndividualBillingProfile saveIndividualProfile(IndividualBillingProfile individualBillingProfile) {
        return individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile))
                .toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingProfile> all(UUID userId, Long githubUserId) {
        final var type = getBillingProfileTypeForUser(userId)
                .orElseThrow(() -> notFound("Billing profile type not found for user " + userId));

        final var billingProfile = (switch (type) {
            case COMPANY -> findCompanyProfileForUser(userId).map(BillingProfile::of);
            case INDIVIDUAL -> findIndividualBillingProfile(userId).map(BillingProfile::of);
        }).orElseThrow(() -> notFound("Billing profile not found for user " + userId));

        final var rewardCount = userRewardRepository.getPendingInvoicesViewEntities(githubUserId).size();

        return List.of(billingProfile.rewardCount(rewardCount));
    }

    @Override
    public Boolean hasValidBillingProfileForUserAndType(UUID userId, BillingProfileType billingProfileType) {
        return switch (billingProfileType) {
            case COMPANY -> companyBillingProfileRepository.findByUserId(userId)
                    .map(e -> e.getVerificationStatus().equals(VerificationStatusEntity.VERIFIED))
                    .orElse(false);
            case INDIVIDUAL -> individualBillingProfileRepository.findByUserId(userId)
                    .map(e -> e.getVerificationStatus().equals(VerificationStatusEntity.VERIFIED))
                    .orElse(false);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyBillingProfile> findCompanyByExternalVerificationId(String billingProfileExternalVerificationId) {
        return companyBillingProfileRepository.findByApplicantId(billingProfileExternalVerificationId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationStatus> findKycStatusesFromParentKybExternalVerificationId(String billingProfileExternalVerificationId) {
        return childrenKycRepository.findAllByParentApplicantId(billingProfileExternalVerificationId)
                .stream()
                .map(ChildrenKycEntity::getVerificationStatus)
                .map(verificationStatusEntity -> switch (verificationStatusEntity) {
                    case REJECTED -> VerificationStatus.REJECTED;
                    case VERIFIED -> VerificationStatus.VERIFIED;
                    case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                    case CLOSED -> VerificationStatus.CLOSED;
                    case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                    case STARTED -> VerificationStatus.STARTED;
                }).toList();
    }

    @Override
    @Transactional
    public void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus) {
        childrenKycRepository.save(ChildrenKycEntity.builder()
                .applicantId(externalApplicantId)
                .parentApplicantId(parentExternalApplicantId)
                .verificationStatus(switch (verificationStatus) {
                    case REJECTED -> VerificationStatusEntity.REJECTED;
                    case UNDER_REVIEW -> VerificationStatusEntity.UNDER_REVIEW;
                    case STARTED -> VerificationStatusEntity.STARTED;
                    case CLOSED -> VerificationStatusEntity.CLOSED;
                    case VERIFIED -> VerificationStatusEntity.VERIFIED;
                    case NOT_STARTED -> VerificationStatusEntity.NOT_STARTED;
                })
                .build());
    }
}
