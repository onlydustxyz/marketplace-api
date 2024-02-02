package onlydust.com.marketplace.accounting.domain.stubs;

import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;

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
    public void deleteTransaction(String reference) {
        SPONSOR_ACCOUNTS.forEach(l -> l.getTransactions().removeIf(t -> t.reference().equals(reference)));
    }
}
