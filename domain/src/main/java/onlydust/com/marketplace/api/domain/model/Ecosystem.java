package onlydust.com.marketplace.api.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Builder
@Data
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
}
