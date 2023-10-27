package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CryptoReceiptJsonEntity {
    @JsonProperty("recipient_ens")
    String recipientEns;
    @JsonProperty("transaction_hash")
    String transactionHash;
    @JsonProperty("recipient_address")
    String recipientAddress;

    public enum Crypto {
        Ethereum, Optimism, Aptos, Starknet
    }
}
