package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.OldVerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.project.domain.model.OldBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldBillingProfileType;
import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import onlydust.com.marketplace.project.domain.port.output.OldBillingProfileStoragePort;
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
    private final GlobalSettingsRepository globalSettingsRepository;
    private final UserRewardViewRepository userRewardViewRepository;

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
        return individualBillingProfileRepository.findByUserId(userId).map(IndividualBillingProfileEntity::toDomain);
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
    public List<OldBillingProfile> all(UUID userId, Long githubUserId) {
        final var type = getBillingProfileTypeForUser(userId)
                .orElseThrow(() -> notFound("Billing profile type not found for user " + userId));

        final var billingProfile = (switch (type) {
            case COMPANY -> findCompanyProfileForUser(userId).map(OldBillingProfile::of);
            case INDIVIDUAL -> findIndividualBillingProfile(userId).map(OldBillingProfile::of);
        }).orElseThrow(() -> notFound("Billing profile not found for user " + userId));

        final var rewardCount = userRewardViewRepository.findPendingPaymentRequestForRecipient(githubUserId).size();

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
}
