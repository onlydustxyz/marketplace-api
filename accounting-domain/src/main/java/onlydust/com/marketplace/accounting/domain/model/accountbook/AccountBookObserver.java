package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;

import java.time.ZonedDateTime;

public interface AccountBookObserver {
    void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull ZonedDateTime at, @NonNull AccountBook.Transaction transaction);
}
