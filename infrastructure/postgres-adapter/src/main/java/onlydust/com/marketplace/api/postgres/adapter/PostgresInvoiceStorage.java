package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
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
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;
    private final @NonNull InvoiceRepository invoiceRepository;
    private final @NonNull RewardRepository rewardRepository;

    @Override
    public List<Invoice.Reward> findRewards(List<RewardId> rewardIds) {
        return invoiceRewardRepository.findAll(rewardIds.stream().map(RewardId::value).toList())
                .stream()
                .map(InvoiceRewardEntity::forInvoice).toList();
    }

    @Override
    @Transactional
    public void create(final @NonNull Invoice invoice) {
        final var entity = new InvoiceEntity().id(invoice.id().value());
        entity.updateWith(invoice);
        invoiceRepository.saveAndFlush(entity);

        final var rewards = rewardRepository.findAllById(invoice.rewards().stream().map(r -> r.id().value()).toList());
        if (rewards.size() != invoice.rewards().size()) {
            throw notFound("Some invoice's rewards were not found (invoice %s). This may happen if a reward was cancelled in the meantime.".formatted(invoice.id()));
        }
        rewardRepository.saveAll(rewards.stream().map(pr -> pr.invoice(entity)).toList());
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
            //TODO
//            final var rewards = rewardRepository.findAllByInvoiceId(invoice.id());
//            rewardRepository.saveAll(rewards.stream().map(pr -> pr.invoice(null)).toList());
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
        return Optional.ofNullable(reward.invoice()).map(InvoiceEntity::toDomain);
    }

    @Override
    public int getNextSequenceNumber(BillingProfile.Id billingProfileId) {
        return invoiceRepository.countByBillingProfileIdAndStatusNot(billingProfileId.value(), InvoiceEntity.Status.DRAFT) + 1;
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
