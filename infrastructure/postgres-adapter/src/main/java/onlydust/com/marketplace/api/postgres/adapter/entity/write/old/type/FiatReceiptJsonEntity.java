package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FiatReceiptJsonEntity {
    @JsonProperty("recipient_iban")
    String recipientIban;
    @JsonProperty("transaction_reference")
    String transactionReference;
}
