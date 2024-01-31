package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class EcosystemView {
    UUID id;
    String name;
    String url;
    String logoUrl;
    List<UUID> projectIds;

    @Value
    @Builder
    public static class Filters {
        @Builder.Default
        List<UUID> projects = List.of();
        @Builder.Default
        List<UUID> ecosystems = List.of();
    }
}
