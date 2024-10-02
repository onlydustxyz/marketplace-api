package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.DepositObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.kernel.model.UserId;

import java.time.ZonedDateTime;

import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId.Type.SPONSOR_ACCOUNT;

@AllArgsConstructor
public class AccountBookProjector implements AccountBookObserver, DepositObserverPort {
    private final AccountBookStorage accountBookStorage;
    private final SponsorAccountStorage sponsorAccountStorage;

    @Override
    public void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull ZonedDateTime at, @NonNull AccountBook.Transaction transaction) {
        final var sponsorAccountId = transaction.path().stream().filter(a -> a.type() == SPONSOR_ACCOUNT).findFirst()
                .orElseThrow(() -> new IllegalStateException("No sponsor account found in transaction"))
                .sponsorAccountId();

        final var sponsorAccount = sponsorAccountStorage.get(sponsorAccountId)
                .orElseThrow(() -> new IllegalStateException("Sponsor account %s not found".formatted(sponsorAccountId)));

        accountBookStorage.save(AccountingTransactionProjection.of(at, sponsorAccount.currency().id(), sponsorAccount.sponsorId(), transaction));
    }

    @Override
    public void onDepositSubmittedByUser(UserId userId, Deposit deposit) {
        accountBookStorage.save(AccountingTransactionProjection.of(deposit));
    }

    @Override
    public void onDepositRejected(Deposit deposit) {
        accountBookStorage.save(AccountingTransactionProjection.of(deposit));
    }

    @Override
    public void onDepositApproved(Deposit deposit) {
        accountBookStorage.save(AccountingTransactionProjection.of(deposit));
    }
}
