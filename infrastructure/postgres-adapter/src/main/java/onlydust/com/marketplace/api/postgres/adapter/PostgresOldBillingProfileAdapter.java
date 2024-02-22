package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.OldBillingProfileStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresOldBillingProfileAdapter implements OldBillingProfileStoragePort {

    private final UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    private final CompanyBillingProfileRepository companyBillingProfileRepository;
    private final IndividualBillingProfileRepository individualBillingProfileRepository;
    private final CustomUserRewardRepository userRewardRepository;
    private final ChildrenKycRepository childrenKycRepository;
    private final GlobalSettingsRepository globalSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<OldCompanyBillingProfile> findCompanyProfileForUser(UUID userId) {
        return companyBillingProfileRepository.findByUserId(userId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public void saveCompanyProfileForUser(OldCompanyBillingProfile companyBillingProfile) {
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OldIndividualBillingProfile> findIndividualBillingProfile(UUID userId) {
        return individualBillingProfileRepository.findByUserId(userId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public void saveIndividualProfileForUser(OldIndividualBillingProfile individualBillingProfile) {
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile));
    }

    @Override
    @Transactional
    public void saveProfileTypeForUser(OldBillingProfileType oldBillingProfileType, UUID userId) {
        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(switch (oldBillingProfileType) {
                    case COMPANY -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY;
                    case INDIVIDUAL -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL;
                })
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OldBillingProfileType> getBillingProfileTypeForUser(UUID userId) {
        return userBillingProfileTypeRepository.findById(userId).map(entity -> switch (entity.getBillingProfileType()) {
            case INDIVIDUAL -> OldBillingProfileType.INDIVIDUAL;
            case COMPANY -> OldBillingProfileType.COMPANY;
        });
    }

    @Override
    @Transactional
    public void updateBillingProfileType(UUID userId, OldBillingProfileType oldBillingProfileType) {
        userBillingProfileTypeRepository.save(UserBillingProfileTypeEntity.builder()
                .userId(userId)
                .billingProfileType(switch (oldBillingProfileType) {
                    case COMPANY -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.COMPANY;
                    case INDIVIDUAL -> UserBillingProfileTypeEntity.BillingProfileTypeEntity.INDIVIDUAL;
                })
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OldCompanyBillingProfile> findCompanyProfileById(UUID billingProfileId) {
        return companyBillingProfileRepository.findById(billingProfileId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OldIndividualBillingProfile> findIndividualProfileById(UUID billingProfileId) {
        return individualBillingProfileRepository.findById(billingProfileId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional
    public OldCompanyBillingProfile saveCompanyProfile(OldCompanyBillingProfile companyBillingProfile) {
        return companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile))
                .toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate());
    }

    @Override
    @Transactional
    public OldIndividualBillingProfile saveIndividualProfile(OldIndividualBillingProfile individualBillingProfile) {
        return individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile))
                .toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OldBillingProfile> all(UUID userId, Long githubUserId) {
        final var type = getBillingProfileTypeForUser(userId)
                .orElseThrow(() -> notFound("Billing profile type not found for user " + userId));

        final var billingProfile = (switch (type) {
            case COMPANY -> findCompanyProfileForUser(userId).map(OldBillingProfile::of);
            case INDIVIDUAL -> findIndividualBillingProfile(userId).map(OldBillingProfile::of);
        }).orElseThrow(() -> notFound("Billing profile not found for user " + userId));

        final var rewardCount = userRewardRepository.getPendingInvoicesViewEntities(githubUserId).size();

        return List.of(billingProfile.rewardCount(rewardCount));
    }

    @Override
    public Boolean hasValidBillingProfileForUserAndType(UUID userId, OldBillingProfileType oldBillingProfileType) {
        return switch (oldBillingProfileType) {
            case COMPANY -> companyBillingProfileRepository.findByUserId(userId)
                    .map(e -> e.getVerificationStatus().equals(OldVerificationStatusEntity.VERIFIED))
                    .orElse(false);
            case INDIVIDUAL -> individualBillingProfileRepository.findByUserId(userId)
                    .map(e -> e.getVerificationStatus().equals(OldVerificationStatusEntity.VERIFIED))
                    .orElse(false);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OldCompanyBillingProfile> findCompanyByExternalVerificationId(String billingProfileExternalVerificationId) {
        return companyBillingProfileRepository.findByApplicantId(billingProfileExternalVerificationId).map(
                entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OldVerificationStatus> findKycStatusesFromParentKybExternalVerificationId(String billingProfileExternalVerificationId) {
        return childrenKycRepository.findAllByParentApplicantId(billingProfileExternalVerificationId)
                .stream()
                .map(ChildrenKycEntity::getVerificationStatus)
                .map(verificationStatusEntity -> switch (verificationStatusEntity) {
                    case REJECTED -> OldVerificationStatus.REJECTED;
                    case VERIFIED -> OldVerificationStatus.VERIFIED;
                    case UNDER_REVIEW -> OldVerificationStatus.UNDER_REVIEW;
                    case CLOSED -> OldVerificationStatus.CLOSED;
                    case NOT_STARTED -> OldVerificationStatus.NOT_STARTED;
                    case STARTED -> OldVerificationStatus.STARTED;
                }).toList();
    }

    @Override
    @Transactional
    public void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, OldVerificationStatus oldVerificationStatus) {
        childrenKycRepository.save(ChildrenKycEntity.builder()
                .applicantId(externalApplicantId)
                .parentApplicantId(parentExternalApplicantId)
                .verificationStatus(switch (oldVerificationStatus) {
                    case REJECTED -> OldVerificationStatusEntity.REJECTED;
                    case UNDER_REVIEW -> OldVerificationStatusEntity.UNDER_REVIEW;
                    case STARTED -> OldVerificationStatusEntity.STARTED;
                    case CLOSED -> OldVerificationStatusEntity.CLOSED;
                    case VERIFIED -> OldVerificationStatusEntity.VERIFIED;
                    case NOT_STARTED -> OldVerificationStatusEntity.NOT_STARTED;
                })
                .build());
    }
}
