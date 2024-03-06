package onlydust.com.marketplace.api.od.rust.api.client.adapter.dto;

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
    String recipientWallet;
    String recipientIban;
    String transactionReference;
}
