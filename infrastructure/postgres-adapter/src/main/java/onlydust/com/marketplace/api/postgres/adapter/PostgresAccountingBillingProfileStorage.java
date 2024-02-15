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
}
