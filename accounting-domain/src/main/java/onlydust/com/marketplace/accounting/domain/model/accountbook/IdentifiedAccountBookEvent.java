package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Value
@EqualsAndHashCode
@Accessors(fluent = true)
public class IdentifiedAccountBookEvent<R> {
    long id;
    @EqualsAndHashCode.Exclude
    @NonNull ZonedDateTime timestamp;
    AccountBookEvent<R> data;

    public static <R> IdentifiedAccountBookEvent<R> of(long id, AccountBookEvent<R> data) {
        return new IdentifiedAccountBookEvent<>(id, ZonedDateTime.now(), data);
    }
}
