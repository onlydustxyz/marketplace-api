package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReadOnlyAccountBookState {
    @NonNull PositiveAmount balanceOf(@NonNull final AccountBook.AccountId account);

    @NonNull PositiveAmount amountReceivedBy(@NonNull final AccountBook.AccountId account);

    @NonNull PositiveAmount refundableBalance(@NonNull AccountBook.AccountId from, @NonNull AccountBook.AccountId to);

    @NonNull PositiveAmount transferredAmount(@NonNull AccountBook.AccountId from, @NonNull AccountBook.AccountId to);

    boolean hasParent(@NonNull AccountBook.AccountId to, @NonNull Collection<AccountBook.AccountId> from);

    @NonNull List<AccountBook.Transaction> transactionsFrom(@NonNull AccountBook.AccountId from);

    @NonNull List<AccountBook.Transaction> transactionsTo(@NonNull AccountBook.AccountId to);

    @NonNull Map<AccountBook.AccountId, PositiveAmount> transferredAmountPerOrigin(@NonNull AccountBook.AccountId to);

    @NonNull Map<AccountBook.AccountId, PositiveAmount> balancePerOrigin(@NonNull AccountBook.AccountId to);

    @NonNull Map<AccountBook.AccountId, PositiveAmount> unspentChildren(@NonNull AccountBook.AccountId of);

    @NonNull Map<AccountBook.AccountId, PositiveAmount> unspentChildren();

    void export(@NonNull AccountBookState.Exporter exporter);
}
