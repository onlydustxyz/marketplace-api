package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.net.URI;

@Data
@Builder
public class ReceiptView {
    Type type;
    Blockchain blockchain;
    String iban;
    String walletAddress;
    String ens;
    String transactionReference;
    URI transactionReferenceUrl;

    public enum Type {
        FIAT, CRYPTO
    }
}
