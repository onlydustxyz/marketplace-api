package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.AllowanceTransaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class AccountBookProjector implements AccountBookObserver {
    private final SponsorAccountStorage sponsorAccountStorage;
    private final AccountBookStorage accountBookStorage;

    // TODO remove all other methods and use only the new projection
    @Override
    public void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull AccountBook.Transaction transaction) {
        accountBookStorage.save(AccountBookTransactionProjection.of(ZonedDateTime.now(), accountBookId, transaction));
    }

    @Override
    public void onMint(@NonNull ZonedDateTime timestamp, @NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!to.isSponsorAccount()) return;

        final var account = getSponsorAccount(to);
        account.getAllowanceTransactions().add(AllowanceTransaction.mint(timestamp, amount));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onBurn(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, @NonNull PositiveAmount amount) {
        if (!from.isSponsorAccount()) return;

        final var account = getSponsorAccount(from);
        account.getAllowanceTransactions().add(AllowanceTransaction.burn(timestamp, amount));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onTransfer(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!from.isSponsorAccount()) return;
        if (!to.isProject()) return;

        final var account = getSponsorAccount(from);
        account.getAllowanceTransactions().add(AllowanceTransaction.transfer(timestamp, amount, to.projectId()));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onRefund(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!from.isProject()) return;
        if (!to.isSponsorAccount()) return;

        final var account = getSponsorAccount(to);
        account.getAllowanceTransactions().add(AllowanceTransaction.refund(timestamp, amount, from.projectId()));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onFullRefund(@NonNull ZonedDateTime timestamp, @NonNull AccountId from) {
    }

    private SponsorAccount getSponsorAccount(AccountId to) {
        return sponsorAccountStorage.get(to.sponsorAccountId())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor account %s not found".formatted(to)));
    }
}
