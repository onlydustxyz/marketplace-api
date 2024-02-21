package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.SelfEmployedBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.project.domain.model.OldCompanyBillingProfile;
import onlydust.com.marketplace.project.domain.model.OldIndividualBillingProfile;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.GlobalSettingsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;

import java.time.ZonedDateTime;
import java.util.Date;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresAccountingBillingProfileStorage implements AccountingBillingProfileStorage {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull GlobalSettingsRepository globalSettingsRepository;

    @Override
    public boolean isAdmin(UserId userId, BillingProfile.Id billingProfileId) {
        final var admin = companyBillingProfileRepository.findById(billingProfileId.value()).map(CompanyBillingProfileEntity::getUserId)
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value()).map(IndividualBillingProfileEntity::getUserId))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        return admin.equals(userId.value());
    }

    @Override
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
    public void save(IndividualBillingProfile billingProfile) {

    }

    @Override
    public void save(SelfEmployedBillingProfile billingProfile) {

    }

    @Override
    public void save(CompanyBillingProfile billingProfile) {

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
