package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingBillingProfileStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;

import java.time.ZonedDateTime;
import java.util.Date;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresAccountingBillingProfileStorage implements AccountingBillingProfileStorage {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;

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
}
