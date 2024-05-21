package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;


@Builder
public record ShortSponsorView(@NonNull SponsorId id, @NonNull String name, @NonNull String logoUrl) {
}
