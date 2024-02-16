package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.domain.view.ProjectSponsorView;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode
@Accessors(fluent = true)
public class SponsorView {
    UUID id;
    String name;
    String url;
    String logoUrl;

    @Getter(AccessLevel.NONE)
    Set<ProjectSponsorView> projects;

    public SponsorView(UUID id, String name, String url, String logoUrl, Set<ProjectSponsorView> projects) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.logoUrl = logoUrl;
        this.projects = projects;
    }

    public Set<UUID> projectIdsWhereSponsorIsActive() {
        return projects.stream()
                .filter(ProjectSponsorView::isActive)
                .map(ProjectSponsorView::projectId).collect(Collectors.toSet());
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
