package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.CompanyBillingProfile;
import onlydust.com.marketplace.api.domain.model.IndividualBillingProfile;
import onlydust.com.marketplace.api.domain.port.output.BillingProfileStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserBillingProfileTypeEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserBillingProfileTypeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {

    private final UserBillingProfileTypeRepository userBillingProfileTypeRepository;
    private final CompanyBillingProfileRepository companyBillingProfileRepository;
    private final IndividualBillingProfileRepository individualBillingProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyBillingProfile> findCompanyProfileForUser(UUID userId) {
        return companyBillingProfileRepository.findByUserId(userId).map(CompanyBillingProfileEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveCompanyProfileForUser(UUID userId, CompanyBillingProfile companyBillingProfile) {
        companyBillingProfileRepository.save(CompanyBillingProfileEntity.fromDomain(companyBillingProfile, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndividualBillingProfile> findIndividualBillingProfile(UUID userId) {
        return individualBillingProfileRepository.findByUserId(userId)
                .map(IndividualBillingProfileEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveIndividualProfileForUser(UUID userId, IndividualBillingProfile individualBillingProfile) {
        individualBillingProfileRepository.save(IndividualBillingProfileEntity.fromDomain(individualBillingProfile, userId));
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
}
