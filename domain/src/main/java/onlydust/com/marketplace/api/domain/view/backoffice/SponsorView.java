package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
@Accessors(fluent = true)
public class SponsorView {
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
        List<UUID> sponsors = List.of();
    }
}
