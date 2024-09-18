package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.kernel.model.UserId;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
@Value
@Accessors(fluent = true)
public class Ecosystem {
    @NonNull
    @Builder.Default
    UUID id = UUID.randomUUID();
    @NonNull
    String name;
    URI url;
    @NonNull
    URI logoUrl;
    String description;
    @NonNull
    Boolean hidden;
    @NonNull
    List<UserId> leads;

    public static Ecosystem create(@NonNull String name,
                                   @NonNull URI url,
                                   @NonNull URI logoUrl,
                                   @NonNull String description,
                                   @NonNull Boolean hidden,
                                   @NonNull List<UserId> leads) {
        return Ecosystem.builder()
                .id(UUID.randomUUID())
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .description(description)
                .hidden(hidden)
                .leads(leads)
                .build();
    }

    public String slug() {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]+", "")
                .replaceAll("\s+", "-")
                .toLowerCase();
    }
}
