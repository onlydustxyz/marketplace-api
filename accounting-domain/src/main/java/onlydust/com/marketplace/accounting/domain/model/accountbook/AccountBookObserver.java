package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;

import java.time.ZonedDateTime;

public interface AccountBookObserver {
    default void on(IdentifiedAccountBookEvent<?> event) {
        if (event.data() instanceof AccountBookAggregate.MintEvent mintEvent)
            onMint(event.timestamp(), mintEvent.account(), mintEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.BurnEvent burnEvent)
            onBurn(event.timestamp(), burnEvent.account(), burnEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.TransferEvent transferEvent)
            onTransfer(event.timestamp(), transferEvent.from(), transferEvent.to(), transferEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.RefundEvent refundEvent)
            onRefund(event.timestamp(), refundEvent.from(), refundEvent.to(), refundEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.FullRefundEvent fullRefundEvent)
            onFullRefund(event.timestamp(), fullRefundEvent.from());
    }

    void on(@NonNull AccountBook.Transaction transaction);

    void onMint(@NonNull ZonedDateTime timestamp, @NonNull AccountId to, @NonNull PositiveAmount amount);

    void onBurn(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, @NonNull PositiveAmount amount);

    void onTransfer(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, AccountId to, @NonNull PositiveAmount amount);

    void onRefund(@NonNull ZonedDateTime timestamp, @NonNull AccountId from, AccountId to, @NonNull PositiveAmount amount);

    void onFullRefund(@NonNull ZonedDateTime timestamp, @NonNull AccountId from);
}
