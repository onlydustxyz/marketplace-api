package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.InvoicePreview;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BankAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CompanyBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IndividualBillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BankAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.WalletRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresInvoiceStorage implements InvoiceStoragePort {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BankAccountRepository bankAccountRepository;
    private final @NonNull InvoiceRepository invoiceRepository;

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

    @Override
    public void save(@NonNull Invoice invoice) {
        invoiceRepository.save(InvoiceEntity.of(invoice));
    }

    @Override
    public void deleteDraftsOf(final BillingProfile.@NonNull Id billingProfileId) {
        invoiceRepository.deleteAllByBillingProfileIdAndStatus(billingProfileId.value(), InvoiceEntity.Status.DRAFT);
    }

    @Override
    public Page<Invoice> invoicesOf(final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber, final @NonNull Integer pageSize) {
        final var page = invoiceRepository.findAllByBillingProfileId(billingProfileId.value(), PageRequest.of(pageNumber, pageSize));
        return Page.<Invoice>builder()
                .content(page.getContent().stream().map(InvoiceEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Optional<Invoice> get(final @NonNull Invoice.Id invoiceId) {
        return invoiceRepository.findById(invoiceId.value()).map(InvoiceEntity::toDomain);
    }
}
