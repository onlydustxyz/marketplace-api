package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;

public interface AccountBookObserver {
    default void on(IdentifiedAccountBookEvent<?> event) {
        if (event.data() instanceof AccountBookAggregate.MintEvent mintEvent)
            onMint(mintEvent.account(), mintEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.BurnEvent burnEvent)
            onBurn(burnEvent.account(), burnEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.TransferEvent transferEvent)
            onTransfer(transferEvent.from(), transferEvent.to(), transferEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.RefundEvent refundEvent)
            onRefund(refundEvent.from(), refundEvent.to(), refundEvent.amount());
        else if (event.data() instanceof AccountBookAggregate.FullRefundEvent fullRefundEvent)
            onFullRefund(fullRefundEvent.from());
    }

    void onMint(@NonNull AccountId to, @NonNull PositiveAmount amount);

    void onBurn(@NonNull AccountId from, @NonNull PositiveAmount amount);

    void onTransfer(@NonNull AccountId from, AccountId to, @NonNull PositiveAmount amount);

    void onRefund(@NonNull AccountId from, AccountId to, @NonNull PositiveAmount amount);

    void onFullRefund(@NonNull AccountId from);
}
