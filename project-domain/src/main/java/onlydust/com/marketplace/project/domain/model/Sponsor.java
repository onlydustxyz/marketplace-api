package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.net.URI;

@Builder(toBuilder = true)
public record Sponsor(@NonNull SponsorId id,
                      @NonNull String name,
                      URI url,
                      @NonNull URI logoUrl) {
    public static Sponsor create(@NonNull String name, URI url, @NonNull URI logoUrl) {
        return Sponsor.builder()
                .id(SponsorId.random())
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .build();
    }

}
