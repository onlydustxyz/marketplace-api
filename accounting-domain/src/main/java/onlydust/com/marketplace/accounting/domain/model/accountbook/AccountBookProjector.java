package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;

import java.time.ZonedDateTime;

@AllArgsConstructor
public class AccountBookProjector implements AccountBookObserver {
    private final AccountBookStorage accountBookStorage;

    @Override
    public void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull AccountBook.Transaction transaction) {
        accountBookStorage.save(AccountBookTransactionProjection.of(ZonedDateTime.now(), accountBookId, transaction));
    }
}
