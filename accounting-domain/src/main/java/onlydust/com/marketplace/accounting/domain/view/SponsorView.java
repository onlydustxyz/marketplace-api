package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

import java.util.List;

@Accessors(fluent = true, chain = true)
@Builder
public record SponsorView(@NonNull SponsorId id, @NonNull String name, @NonNull String logoUrl, String url, List<ProjectShortView> projects) {
    public ShortSponsorView toShortView() {
        return ShortSponsorView.builder()
                .name(name)
                .logoUrl(logoUrl)
                .build();
    }
}
