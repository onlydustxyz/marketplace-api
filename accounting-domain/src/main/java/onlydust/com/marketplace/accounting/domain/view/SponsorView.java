package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Accessors(fluent = true, chain = true)
@Builder
public record SponsorView(@NonNull UUID id, @NonNull String name, @NonNull String logoUrl, String url, List<ShortProjectView> projects) {
    public ShortSponsorView toShortView() {
        return ShortSponsorView.builder()
                .name(name)
                .logoUrl(logoUrl)
                .build();
    }
}
