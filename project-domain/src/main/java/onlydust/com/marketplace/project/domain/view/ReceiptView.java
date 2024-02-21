package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.blockchain.*;

import java.net.URI;
import java.util.Optional;

@Data
@Builder
public class ReceiptView {
    Type type;
    Blockchain blockchain;
    String iban;
    String walletAddress;
    String ens;
    String transactionReference;

    public Optional<URI> getTransactionReferenceUrl() {
        if (blockchain == null || transactionReference == null) {
            return Optional.empty();
        }

        return Optional.of(switch (blockchain) {
            case ETHEREUM -> Ethereum.BLOCK_EXPLORER.url(Ethereum.transactionHash(transactionReference));
            case APTOS -> Aptos.BLOCK_EXPLORER.url(Aptos.transactionHash(transactionReference));
            case OPTIMISM -> Optimism.BLOCK_EXPLORER.url(Optimism.transactionHash(transactionReference));
            case STARKNET -> StarkNet.BLOCK_EXPLORER.url(StarkNet.transactionHash(transactionReference));
        });
    }

    public enum Type {
        FIAT, CRYPTO
    }
}
