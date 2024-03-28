package onlydust.com.marketplace.accounting.domain.model.accountbook;

public record IdentifiedAccountBookEvent<R>(long id, AccountBookEvent<R> data) {
    public static <R> IdentifiedAccountBookEvent<R> of(long id, AccountBookEvent<R> data) {
        return new IdentifiedAccountBookEvent<>(id, data);
    }
}
