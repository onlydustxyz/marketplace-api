package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStorage;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStorage {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull GlobalSettingsRepository globalSettingsRepository;
    private final @NonNull BillingProfileRepository billingProfileRepository;
    private final @NonNull KybRepository kybRepository;
    private final @NonNull KycRepository kycRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId) {
        final var admin = companyBillingProfileRepository.findById(billingProfileId.value()).map(CompanyBillingProfileEntity::getUserId)
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value()).map(IndividualBillingProfileEntity::getUserId))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        return admin.equals(userId.value());
    }

    @Override
    @Transactional
    public void updateInvoiceMandateAcceptanceDate(@NonNull final BillingProfile.Id billingProfileId, @NonNull final ZonedDateTime acceptanceDate) {
        companyBillingProfileRepository.findById(billingProfileId.value())
                .ifPresentOrElse(companyBillingProfileEntity -> {
                    companyBillingProfileEntity.setInvoiceMandateAcceptedAt(Date.from(acceptanceDate.toInstant()));
                    companyBillingProfileRepository.save(companyBillingProfileEntity);
                }, () -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .ifPresentOrElse(individualBillingProfileEntity -> {
                            individualBillingProfileEntity.setInvoiceMandateAcceptedAt(Date.from(acceptanceDate.toInstant()));
                            individualBillingProfileRepository.save(individualBillingProfileEntity);
                        }, () -> {
                            throw notFound("Billing profile %s not found".formatted(billingProfileId));
                        }));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileView> findIndividualBillingProfileForUser(UserId ownerId) {
        return billingProfileRepository.findIndividualProfilesForUserId(ownerId.value())
                .stream().map(BillingProfileEntity::toView).findFirst();
    }

    @Override
    @Transactional
    public void save(IndividualBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id()));
        final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKycEntity.isEmpty()) {
            kycRepository.save(KycEntity.fromDomain(billingProfile.kyc(), billingProfile.id()));
        }
    }

    @Override
    @Transactional
    public void save(SelfEmployedBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb(), billingProfile.id()));
        }
    }

    @Override
    @Transactional
    public void save(CompanyBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, null));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb(), billingProfile.id()));
        }
    }

    @Override
    public void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {

    }

    @Override
    public boolean isMandateAccepted(BillingProfile.Id billingProfileId) {
        return companyBillingProfileRepository.findById(billingProfileId.value())
                .map(entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate()))
                .map(OldCompanyBillingProfile::isInvoiceMandateAccepted)
                .orElseGet(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(entity -> entity.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate()))
                        .map(OldIndividualBillingProfile::isInvoiceMandateAccepted)
                        .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId))));
    }
}
