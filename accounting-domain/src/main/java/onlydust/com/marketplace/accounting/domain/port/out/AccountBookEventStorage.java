package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;

import java.util.List;
import java.util.Optional;

public interface AccountBookEventStorage {
    @NonNull List<IdentifiedAccountBookEvent> getAll(Currency currency);

    @NonNull List<IdentifiedAccountBookEvent> getSince(Currency currency, long eventId);

    void insert(@NonNull Currency currency, @NonNull List<IdentifiedAccountBookEvent> pendingEvents);

    @NonNull Optional<Long> getLastEventId(@NonNull Currency currency);
}
