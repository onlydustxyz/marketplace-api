package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.RewardAssociations;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.InvoiceViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BillingProfileEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresInvoiceStorage implements InvoiceStoragePort {
    private final @NonNull InvoiceRewardRepository invoiceRewardRepository;
    private final @NonNull InvoiceRepository invoiceRepository;
    private final @NonNull InvoiceViewRepository invoiceViewRepository;
    private final @NonNull RewardRepository rewardRepository;
    private final @NonNull RewardViewRepository rewardViewRepository;

    @Override
    public List<Invoice.Reward> findRewards(List<RewardId> rewardIds) {
        return invoiceRewardRepository.findAll(rewardIds.stream().map(RewardId::value).toList())
                .stream()
                .map(InvoiceRewardEntity::forInvoice).toList();
    }

    @Override
    public List<Invoice> getAll(List<Invoice.Id> invoiceIds) {
        return invoiceRepository.findAllById(invoiceIds.stream().map(Invoice.Id::value).toList())
                .stream()
                .map(InvoiceEntity::toDomain).toList();
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
        rewardRepository.saveAllAndFlush(rewards.stream().map(pr -> pr.invoiceId(invoice.id().value())).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardAssociations> getRewardAssociations(List<RewardId> rewardIds) {
        return rewardRepository.findAllById(rewardIds.stream().map(RewardId::value).toList())
                .stream().map(r -> new RewardAssociations(
                        RewardId.of(r.id()),
                        r.status().toDomain(),
                        r.invoiceId() == null ? null : Invoice.Id.of(r.invoiceId()),
                        r.billingProfileId() == null ? null : BillingProfile.Id.of(r.billingProfileId())
                )).toList();
    }


    @Override
    @Transactional
    public void update(final @NonNull Invoice invoice) {
        final var entity = invoiceRepository.findById(invoice.id().value())
                .orElseThrow(() -> notFound("Invoice %s not found".formatted(invoice.id())));

        entity.updateWith(invoice);
        invoiceRepository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public void deleteDraftsOf(final @NonNull BillingProfile.Id billingProfileId) {
        final var drafts = invoiceRepository.findAllByBillingProfileIdAndStatus(billingProfileId.value(), InvoiceEntity.Status.DRAFT);

        drafts.forEach(invoice -> {
            final var rewards = rewardRepository.findAllByInvoiceId(invoice.id());
            rewardRepository.saveAllAndFlush(rewards.stream().map(pr -> pr.invoiceId(null)).toList());
        });

        invoiceRepository.deleteAll(drafts);
        invoiceRepository.flush();
    }

    @Override
    @Transactional
    public Page<InvoiceView> invoicesOf(final @NonNull BillingProfile.Id billingProfileId, final @NonNull Integer pageNumber, final @NonNull Integer pageSize,
                                        final @NonNull Invoice.Sort sort, final @NonNull SortDirection direction) {
        final var page = invoiceViewRepository.findAllByBillingProfileIdAndStatusNot(billingProfileId.value(), InvoiceEntity.Status.DRAFT,
                PageRequest.of(pageNumber, pageSize, sortBy(sort, direction == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC)));
        return Page.<InvoiceView>builder()
                .content(page.getContent().stream().map(InvoiceViewEntity::toView).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Optional<Invoice> get(final @NonNull Invoice.Id invoiceId) {
        return invoiceRepository.findById(invoiceId.value()).map(InvoiceEntity::toDomain);
    }

    @Override
    @Transactional
    public Optional<InvoiceView> getView(final @NonNull Invoice.Id invoiceId) {
        return invoiceViewRepository.findById(invoiceId.value()).map(InvoiceViewEntity::toView);
    }

    @Override
    public Page<Invoice> findAll(@NonNull List<Invoice.Id> ids, @NonNull List<Invoice.Status> statuses, @NonNull List<Currency.Id> currencyIds,
                                 @NonNull List<BillingProfile.Type> billingProfileTypes, String search, @NonNull Integer pageIndex, @NonNull Integer pageSize) {
        final var page = invoiceRepository.findAllExceptDrafts(
                ids.stream().map(Invoice.Id::value).toList(),
                statuses.stream().map(InvoiceEntity.Status::of).map(Enum::toString).toList(),
                currencyIds.stream().map(Currency.Id::value).toList(),
                billingProfileTypes.stream().map(BillingProfileEntity.Type::of).map(Enum::toString).toList(),
                search == null ? "" : search,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "created_at")));

        return Page.<Invoice>builder()
                .content(page.getContent().stream().map(InvoiceEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public Optional<Invoice> invoiceOf(RewardId rewardId) {
        final var reward = rewardViewRepository.findById(rewardId.value()).orElseThrow(() -> notFound("Reward %s not found".formatted(rewardId)));
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
