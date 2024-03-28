package onlydust.com.marketplace.accounting.domain.model.accountbook;

public record IdentifiedAccountBookEvent<R>(long id, AccountBookEvent<R> data) {
}
