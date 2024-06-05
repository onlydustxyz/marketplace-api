package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

@Builder
@Value
@Accessors(fluent = true)
public class Ecosystem {
    @NonNull
    @Builder.Default
    UUID id = UUID.randomUUID();
    @NonNull
    String name;
    @NonNull
    String url;
    @NonNull
    String logoUrl;
    @NonNull
    String description;
    @NonNull
    Boolean hidden;

    public String slug() {
        return name.replaceAll("[^a-zA-Z0-9_\\- ]+", "")
                .replaceAll("\s+", "-")
                .toLowerCase();
    }
}
