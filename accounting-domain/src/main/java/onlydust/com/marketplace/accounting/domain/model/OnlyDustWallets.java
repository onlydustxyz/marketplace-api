package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OnlyDustWallets {
    String ethereum;
    String optimism;
    String starknet;
    String aptos;
    String stellar;
    String sepa;

    public Optional<String> get(Network network) {
        return switch (network) {
            case ETHEREUM -> Optional.ofNullable(ethereum);
            case OPTIMISM -> Optional.ofNullable(optimism);
            case STARKNET -> Optional.ofNullable(starknet);
            case APTOS -> Optional.ofNullable(aptos);
            case STELLAR -> Optional.ofNullable(stellar);
            case SEPA -> Optional.ofNullable(sepa);
        };
    }

    public Optional<String> get(Blockchain blockchain) {
        return switch (blockchain) {
            case ETHEREUM -> Optional.ofNullable(ethereum);
            case OPTIMISM -> Optional.ofNullable(optimism);
            case STARKNET -> Optional.ofNullable(starknet);
            case APTOS -> Optional.ofNullable(aptos);
            case STELLAR -> Optional.ofNullable(stellar);
        };
    }
}