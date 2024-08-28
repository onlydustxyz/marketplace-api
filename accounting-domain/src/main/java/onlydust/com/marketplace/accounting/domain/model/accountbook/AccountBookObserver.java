package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.List;

public interface AccountBookObserver {
    void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull ZonedDateTime at, @NonNull List<AccountBook.Transaction> transactions);
}
