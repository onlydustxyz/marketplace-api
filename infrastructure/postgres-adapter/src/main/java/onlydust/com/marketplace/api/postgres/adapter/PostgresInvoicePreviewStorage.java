package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoicePreviewStoragePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresInvoicePreviewStorage implements InvoicePreviewStoragePort {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;

    @Override
    public InvoicePreview generate(BillingProfile.@NonNull Id billingProfileId, @NonNull List<RewardId> rewardIds) {
        int sequenceNumber = 1;
        final var preview = companyBillingProfileRepository.findById(billingProfileId.value())
                .map(CompanyBillingProfileEntity::forInvoicePreview)
                .map(info -> InvoicePreview.of(sequenceNumber, info))
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(IndividualBillingProfileEntity::forInvoicePreview)
                        .map(info -> InvoicePreview.of(sequenceNumber, info)))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        final var rewards = invoiceRewardRepository.findAll(rewardIds.stream().map(RewardId::value).toList())
                .stream().map(InvoiceRewardEntity::forInvoicePreview).toList();

        return preview.rewards(rewards);
    }
}
