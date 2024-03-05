package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MarkPaymentReceivedDTO {
    BigDecimal amount;
    String currency;
    @JsonProperty("recipient_wallet")
    String recipientWallet;
    @JsonProperty("recipient_iban")
    String recipientIban;
    @JsonProperty("transaction_reference")
    String transactionReference;
}
