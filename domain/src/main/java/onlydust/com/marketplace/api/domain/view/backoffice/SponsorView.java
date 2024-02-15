package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Value
@EqualsAndHashCode
@Accessors(fluent = true)
public class SponsorView {
    private static final int MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR = 6;

    UUID id;
    String name;
    String url;
    String logoUrl;
    Set<UUID> projectIds;

    public SponsorView(UUID id, String name, String url, String logoUrl) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.logoUrl = logoUrl;
        this.projectIds = new HashSet<>();
    }

    public void addProjectId(UUID projectId, ZonedDateTime lastAllocationDate) {
        if (lastAllocationDate == null || lastAllocationDate.isAfter(ZonedDateTime.now().minusMonths(MONTHS_SINCE_LAST_ALLOCATION_TO_BE_A_SPONSOR))) {
            this.projectIds.add(projectId);
        }
    }

    @Value
    @Builder
    public static class Filters {
        @Builder.Default
        List<UUID> projects = List.of();
        @Builder.Default
        List<UUID> sponsors = List.of();
    }
}
