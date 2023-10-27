package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReceiptView {
    Type type;
    String iban;
    String walletAddress;
    String ens;
    String transactionReference;

    public enum Type {
        FIAT, CRYPTO
    }
}
