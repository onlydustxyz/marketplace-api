package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;

public interface AccountBookObserver {
    void on(@NonNull AccountBookAggregate.Id accountBookId, @NonNull AccountBook.Transaction transaction);
}
