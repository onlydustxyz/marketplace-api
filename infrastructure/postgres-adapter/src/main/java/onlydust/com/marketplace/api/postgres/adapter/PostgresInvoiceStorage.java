package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BankAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BankAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.WalletRepository;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresInvoiceStorage implements InvoiceStoragePort {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BankAccountRepository bankAccountRepository;

    @Override
    public InvoicePreview preview(BillingProfile.@NonNull Id billingProfileId, @NonNull List<RewardId> rewardIds) {
        int sequenceNumber = 1; // TODO
        final var preview = companyBillingProfileRepository.findById(billingProfileId.value())
                .map(CompanyBillingProfileEntity::forInvoicePreview)
                .map(info -> InvoicePreview.of(sequenceNumber, info))
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(IndividualBillingProfileEntity::forInvoicePreview)
                        .map(info -> InvoicePreview.of(sequenceNumber, info)))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        final var rewards = invoiceRewardRepository.findAll(rewardIds.stream().map(RewardId::value).toList())
                .stream()
                .map(InvoiceRewardEntity::forInvoicePreview).toList();

        final var userId = companyBillingProfileRepository.findById(billingProfileId.value())
                .map(CompanyBillingProfileEntity::getUserId)
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(IndividualBillingProfileEntity::getUserId))
                .orElseThrow();

        // TODO filter in domain using SponsorAccount network
        preview.wallets(walletRepository.findAllByUserId(userId).stream().map(WalletEntity::forInvoicePreview).toList());
        preview.bankAccount(bankAccountRepository.findById(userId).map(BankAccountEntity::forInvoicePreview).orElse(null));

        return preview.rewards(rewards);
    }
}
