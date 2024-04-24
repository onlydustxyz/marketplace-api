package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountTransactionViewRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresSponsorAccountStorageAdapter implements SponsorAccountStorage {
    private final SponsorAccountRepository sponsorAccountRepository;
    private final SponsorAccountTransactionViewRepository sponsorAccountTransactionViewRepository;

    @Override
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return sponsorAccountRepository.findById(id.value()).map(SponsorAccountEntity::toDomain);
    }

    @Override
    public void save(SponsorAccount... sponsorAccounts) {
        sponsorAccountRepository.saveAllAndFlush(Arrays.stream(sponsorAccounts).map(SponsorAccountEntity::of).toList());
    }

    @Override
    public void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId) {
        final var sponsorAccount = sponsorAccountRepository.findById(sponsorAccountId.value())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor account %s not found".formatted(sponsorAccountId)));

        sponsorAccount.getTransactions().removeIf(t -> t.getId().equals(transactionId.value()));
        sponsorAccountRepository.saveAndFlush(sponsorAccount);
    }

    @Override
    public List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountRepository.findAllBySponsorId(sponsorId.value()).stream()
                .map(SponsorAccountEntity::toDomain)
                .sorted(comparing(a -> a.currency().code().toString().toUpperCase()))
                .toList();
    }

    @Override
    public Page<HistoricalTransaction> transactionsOf(@NonNull SponsorId sponsorId,
                                                      @NonNull HistoricalTransaction.Filters filters,
                                                      @NonNull Integer pageIndex,
                                                      @NonNull Integer pageSize,
                                                      @NonNull HistoricalTransaction.Sort sort,
                                                      @NonNull SortDirection direction) {
        final var format = new SimpleDateFormat("yyyy-MM-dd");

        final var currencyIds = filters.getCurrencies().stream().map(Currency.Id::value).toList();
        final var projectIds = filters.getProjectIds().stream().map(ProjectId::value).toList();
        final var types = filters.getTypes().stream().map(SponsorAccountTransactionViewEntity.TransactionType::of).map(Enum::name).toList();
        final var fromDate = isNull(filters.getFrom()) ? null : format.format(filters.getFrom());
        final var toDate = isNull(filters.getTo()) ? null : format.format(filters.getTo());
        final var sortBy = SponsorAccountTransactionViewRepository.sortBy(sort, direction == SortDirection.asc ? Sort.Direction.ASC : Sort.Direction.DESC);

        final var page = sponsorAccountTransactionViewRepository.findAll(
                sponsorId.value(),
                currencyIds,
                projectIds,
                types,
                fromDate,
                toDate,
                PageRequest.of(pageIndex, pageSize, sortBy));

        return Page.<HistoricalTransaction>builder()
                .content(page.getContent().stream().map(SponsorAccountTransactionViewEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    public List<SponsorAccount> find(SponsorId sponsorId, Currency.Id currencyId) {
        return sponsorAccountRepository.findBySponsorIdAndCurrencyId(sponsorId.value(), currencyId.value())
                .stream().map(SponsorAccountEntity::toDomain).toList();
    }
}
