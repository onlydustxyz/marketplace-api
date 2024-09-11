package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.net.URI;
import java.util.List;

@Builder(toBuilder = true)
public record Sponsor(@NonNull SponsorId id,
                      @NonNull String name,
                      URI url,
                      @NonNull URI logoUrl,
                      @NonNull List<UserId> leads) {
    public static Sponsor create(@NonNull String name, URI url, @NonNull URI logoUrl, @NonNull List<UserId> leads) {
        return Sponsor.builder()
                .id(SponsorId.random())
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .leads(leads)
                .build();
    }

}
