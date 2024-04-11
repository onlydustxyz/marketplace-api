package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.AllowanceTransaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@AllArgsConstructor
public class AccountBookProjector implements AccountBookObserver {
    private final SponsorAccountStorage sponsorAccountStorage;

    @Override
    public void onMint(@NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!to.isSponsorAccount()) return;

        final var account = getSponsorAccount(to);
        account.getAllowanceTransactions().add(AllowanceTransaction.mint(amount));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onBurn(@NonNull AccountId from, @NonNull PositiveAmount amount) {
        if (!from.isSponsorAccount()) return;

        final var account = getSponsorAccount(from);
        account.getAllowanceTransactions().add(AllowanceTransaction.burn(amount));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onTransfer(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!from.isSponsorAccount()) return;
        if (!to.isProject()) return;

        final var account = getSponsorAccount(from);
        account.getAllowanceTransactions().add(AllowanceTransaction.transfer(amount, to.projectId()));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onRefund(@NonNull AccountId from, @NonNull AccountId to, @NonNull PositiveAmount amount) {
        if (!from.isProject()) return;
        if (!to.isSponsorAccount()) return;

        final var account = getSponsorAccount(to);
        account.getAllowanceTransactions().add(AllowanceTransaction.refund(amount, from.projectId()));
        sponsorAccountStorage.save(account);
    }

    @Override
    public void onFullRefund(@NonNull AccountId from) {
    }

    private SponsorAccount getSponsorAccount(AccountId to) {
        return sponsorAccountStorage.get(to.sponsorAccountId())
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor account %s not found".formatted(to)));
    }
}
