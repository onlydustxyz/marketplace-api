package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;

import java.time.ZonedDateTime;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId.Type.SPONSOR_ACCOUNT;

@AllArgsConstructor
public class AccountBookProjector implements AccountBookObserver {
    private final AccountBookStorage accountBookStorage;
    private final SponsorAccountStorage sponsorAccountStorage;

    @Override
    public void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull ZonedDateTime at, @NonNull List<AccountBook.Transaction> transactions) {
        final var projections = transactions.stream()
                .map(t -> project(at, t))
                .collect(groupingBy(AccountBookTransactionProjection::key,
                        reducing(null, AccountBookTransactionProjection::merge)))
                .values();

        projections.forEach(accountBookStorage::save);
    }

    private AccountBookTransactionProjection project(@NonNull ZonedDateTime at, AccountBook.Transaction transaction) {
        final var sponsorAccountId = transaction.path().stream().filter(a -> a.type() == SPONSOR_ACCOUNT).findFirst()
                .orElseThrow(() -> new IllegalStateException("No sponsor account found in transaction"))
                .sponsorAccountId();

        final var sponsorAccount = sponsorAccountStorage.get(sponsorAccountId)
                .orElseThrow(() -> new IllegalStateException("Sponsor account %s not found".formatted(sponsorAccountId)));

        return AccountBookTransactionProjection.of(at, sponsorAccount.currency().id(), sponsorAccount.sponsorId(), transaction);
    }
}
