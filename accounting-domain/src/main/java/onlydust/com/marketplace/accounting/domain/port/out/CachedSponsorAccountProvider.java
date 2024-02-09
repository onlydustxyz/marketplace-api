package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class CachedSponsorAccountProvider implements SponsorAccountProvider {
    private final SponsorAccountProvider provider;
    private final Map<SponsorAccount.Id, SponsorAccount> cache = new HashMap<>();

    @Override
    public Optional<SponsorAccount> get(SponsorAccount.Id id) {
        return Optional.ofNullable(cache.computeIfAbsent(id, i -> provider.get(i).orElse(null)));
    }
}
