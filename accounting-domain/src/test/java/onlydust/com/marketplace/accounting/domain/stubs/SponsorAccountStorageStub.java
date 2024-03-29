package onlydust.com.marketplace.accounting.domain.stubs;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SponsorAccountStorageStub implements SponsorAccountStorage {
    private static final List<SponsorAccount> SPONSOR_ACCOUNTS = new ArrayList<>();

    @SneakyThrows
    @Override
    public void save(SponsorAccount... sponsorAccounts) {
        SponsorAccountStorageStub.SPONSOR_ACCOUNTS.removeAll(List.of(sponsorAccounts));
        SponsorAccountStorageStub.SPONSOR_ACCOUNTS.addAll(List.of(sponsorAccounts));
    }

    @Override
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return SPONSOR_ACCOUNTS.stream().filter(l -> l.id().equals(id)).findFirst().map(SponsorAccount::of);
    }

    @Override
    public void delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId) {
        SPONSOR_ACCOUNTS.stream().filter(a -> a.id().equals(sponsorAccountId))
                .forEach(l -> l.getTransactions().removeIf(t -> t.id().equals(transactionId)));
    }

    @Override
    public List<SponsorAccount> getSponsorAccounts(SponsorId sponsorId) {
        return SPONSOR_ACCOUNTS.stream().filter(l -> l.sponsorId().equals(sponsorId)).toList();
    }

    @Override
    public Page<HistoricalTransaction> transactionsOf(SponsorId sponsorId, Integer pageIndex, Integer pageSize) {
        return null;
    }
}
