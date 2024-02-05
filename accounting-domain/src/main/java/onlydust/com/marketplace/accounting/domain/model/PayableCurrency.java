package onlydust.com.marketplace.accounting.domain.model;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.net.URI;

public record PayableCurrency(
        @NonNull Currency.Id id,
        @NonNull Currency.Code code,
        @NonNull String name,
        URI logoUrl,
        @NonNull Currency.Type type,
        Currency.Standard standard,
        Blockchain blockchain,
        ContractAddress address
) {
}
