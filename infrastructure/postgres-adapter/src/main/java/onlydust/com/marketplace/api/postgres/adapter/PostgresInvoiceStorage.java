package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CompanyBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.IndividualBillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OldBankAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OldWalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.BankAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OldWalletRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresInvoiceStorage implements InvoiceStoragePort {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;
    private final @NonNull OldWalletRepository oldWalletRepository;
    private final @NonNull BankAccountRepository bankAccountRepository;
    private final @NonNull InvoiceRepository invoiceRepository;
    private final @NonNull PaymentRequestRepository paymentRequestRepository;
    private final @NonNull RewardRepository rewardRepository;

    @Override
    public Invoice preview(BillingProfile.@NonNull Id billingProfileId, @NonNull List<RewardId> rewardIds) {
        final var sequenceNumber = invoiceRepository.countByBillingProfileIdAndStatusNot(billingProfileId.value(), InvoiceEntity.Status.DRAFT) + 1;
        final var preview = companyBillingProfileRepository.findById(billingProfileId.value())
                .map(CompanyBillingProfileEntity::forInvoice)
                .map(info -> Invoice.of(billingProfileId, sequenceNumber, info))
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(IndividualBillingProfileEntity::forInvoice)
                        .map(info -> Invoice.of(billingProfileId, sequenceNumber, info)))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        final var rewards = invoiceRewardRepository.findAll(rewardIds.stream().map(RewardId::value).toList())
                .stream()
                .map(InvoiceRewardEntity::forInvoice).toList();

        final var userId = companyBillingProfileRepository.findById(billingProfileId.value())
                .map(CompanyBillingProfileEntity::getUserId)
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value())
                        .map(IndividualBillingProfileEntity::getUserId))
                .orElseThrow();

        // TODO filter in domain using SponsorAccount network
        preview.wallets(oldWalletRepository.findAllByUserId(userId).stream().map(OldWalletEntity::forInvoice).toList());
        preview.bankAccount(bankAccountRepository.findById(userId).map(OldBankAccountEntity::forInvoice).orElse(null));

        return preview.rewards(rewards);
    }

    @Override
    @Transactional
    public void create(final @NonNull Invoice invoice) {
        final var entity = new InvoiceEntity().id(invoice.id().value());
        entity.updateWith(invoice);
        invoiceRepository.saveAndFlush(entity);

        final var paymentRequests = paymentRequestRepository.findAllById(invoice.rewards().stream().map(r -> r.id().value()).toList());
        paymentRequests.forEach(pr -> pr.setInvoice(entity));
        paymentRequestRepository.saveAll(paymentRequests);

        //TODO: save invoiceId in rewards
    }

    @Override
    @Transactional
    public void update(final @NonNull Invoice invoice) {
        final var entity = invoiceRepository.findById(invoice.id().value())
                .orElseThrow(() -> notFound("Invoice %s not found".formatted(invoice.id())));

        entity.updateWith(invoice);
        invoiceRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteDraftsOf(final @NonNull BillingProfile.Id billingProfileId) {
        final var drafts = invoiceRepository.findAllByBillingProfileIdAndStatus(billingProfileId.value(), InvoiceEntity.Status.DRAFT);

        drafts.forEach(invoice -> {
            final var paymentRequests = paymentRequestRepository.findAllById(invoice.data().rewards().stream().map(InvoiceRewardEntity::id).toList());
            paymentRequests.forEach(pr -> pr.setInvoice(null));
            paymentRequestRepository.saveAll(paymentRequests);
        });

        invoiceRepository.deleteAll(drafts);
        invoiceRepository.flush();
    }

    @Override
    public Page<Invoice> invoicesOf(final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber, final @NonNull Integer pageSize,
                                    final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction) {
        final var page = invoiceRepository.findAllByBillingProfileIdAndStatusNot(billingProfileId.value(), InvoiceEntity.Status.DRAFT,
                PageRequest.of(pageNumber, pageSize, sortBy(sort, direction == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC)));
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

    @Override
    public Page<Invoice> findAll(final @NonNull List<Invoice.Id> ids, final @NonNull List<Invoice.Status> statuses, Integer pageIndex, Integer pageSize) {
        final var pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        final var page = ids.isEmpty() ? invoiceRepository.findAllByStatusIn(statuses.stream().map(InvoiceEntity.Status::of).toList(), pageRequest) :
                invoiceRepository.findAllByIdInAndStatusNot(ids.stream().map(Invoice.Id::value).toList(), InvoiceEntity.Status.DRAFT, pageRequest);

        return Page.<Invoice>builder()
                .content(page.getContent().stream().map(InvoiceEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Optional<Invoice> invoiceOf(RewardId rewardId) {
        final var reward = rewardRepository.findById(rewardId.value()).orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)));
        return Optional.ofNullable(reward.getInvoice()).map(InvoiceEntity::toDomain);
    }

    private Sort sortBy(Invoice.Sort sort, Sort.Direction direction) {
        return switch (sort) {
            case CREATED_AT -> Sort.by(direction, "createdAt");
            case AMOUNT -> Sort.by(direction, "amount");
            case NUMBER -> Sort.by(direction, "number");
            case STATUS -> Sort.by(direction, "status");
        };
    }
}
