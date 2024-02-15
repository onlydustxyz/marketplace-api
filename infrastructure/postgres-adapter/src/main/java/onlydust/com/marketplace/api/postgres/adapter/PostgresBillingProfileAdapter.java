package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.BillingProfile;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.VerificationStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserBillingProfileTypeRepository;
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

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyBillingProfile> findCompanyProfileForUser(UUID userId) {
        return companyBillingProfileRepository.findByUserId(userId).map(CompanyBillingProfileEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveCompanyProfileForUser(CompanyBillingProfile companyBillingProfile) {
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndividualBillingProfile> findIndividualBillingProfile(UUID userId) {
        return individualBillingProfileRepository.findByUserId(userId)
                .map(IndividualBillingProfileEntity::toDomain);
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
        return companyBillingProfileRepository.findById(billingProfileId).map(CompanyBillingProfileEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndividualBillingProfile> findIndividualProfileById(UUID billingProfileId) {
        return individualBillingProfileRepository.findById(billingProfileId).map(IndividualBillingProfileEntity::toDomain);
    }

    @Override
    @Transactional
    public CompanyBillingProfile saveCompanyProfile(CompanyBillingProfile companyBillingProfile) {
        return companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile))
                .toDomain();
    }

    @Override
    @Transactional
    public IndividualBillingProfile saveIndividualProfile(IndividualBillingProfile individualBillingProfile) {
        return individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile)).toDomain();
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
}
