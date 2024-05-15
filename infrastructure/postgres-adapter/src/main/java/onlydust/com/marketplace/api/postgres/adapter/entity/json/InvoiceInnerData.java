package onlydust.com.marketplace.api.postgres.adapter.entity.json;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.InvoiceRewardEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record InvoiceInnerData(@NonNull ZonedDateTime dueAt,
                               @NonNull BigDecimal taxRate,
                               Invoice.BillingProfileSnapshot billingProfileSnapshot,
                               List<InvoiceRewardEntity> rewards
) implements Serializable {

    public static InvoiceInnerData of(final @NonNull Invoice invoice) {
        return new InvoiceInnerData(
                invoice.dueAt(),
                invoice.taxRate(),
                invoice.billingProfileSnapshot(),
                invoice.rewards().stream().map(InvoiceRewardEntity::of).toList()
        );
    }
}
