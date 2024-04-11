package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.SponsorAccountTransactionViewRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

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
    public Page<HistoricalTransaction> transactionsOf(SponsorId sponsorId, List<HistoricalTransaction.Type> types, Integer pageIndex, Integer pageSize) {
        final var page = sponsorAccountTransactionViewRepository.findAllBySponsorAccountSponsorIdAndTypeIn(
                sponsorId.value(), types.stream().map(SponsorAccountTransactionViewEntity.TransactionType::of).toList(), PageRequest.of(pageIndex,
                        pageSize, Sort.by(Sort.Direction.DESC, "timestamp")));

        return Page.<HistoricalTransaction>builder()
                .content(page.getContent().stream().map(SponsorAccountTransactionViewEntity::toDomain).toList())
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }
}
