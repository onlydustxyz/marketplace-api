package onlydust.com.marketplace.accounting.domain.model;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.net.URI;
import java.util.Optional;

@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString(exclude = {"logoUrl"})
public class PayableCurrency {
    @Getter
    private final @NonNull Currency.Id id;
    @Getter
    private final @NonNull Currency.Code code;
    @Getter
    private final @NonNull String name;
    URI logoUrl;
    @Getter
    private final @NonNull Currency.Type type;
    private final Currency.Standard standard;
    private final Blockchain blockchain;
    private final Hash address;

    public Optional<Currency.Standard> standard() {
        return Optional.ofNullable(standard);
    }

    public Optional<URI> logoUrl() {
        return Optional.ofNullable(logoUrl);
    }

    public Optional<Blockchain> blockchain() {
        return Optional.ofNullable(blockchain);
    }

    public Network network() {
        return Network.fromBlockchain(blockchain);
    }

    public Optional<Hash> address() {
        return Optional.ofNullable(address);
    }
}
