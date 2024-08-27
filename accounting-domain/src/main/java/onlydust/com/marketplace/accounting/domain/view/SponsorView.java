package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.net.URI;

@Builder
public record SponsorView(
        @NonNull SponsorId id,
        @NonNull String name,
        URI url,
        @NonNull URI logoUrl
) {
}
