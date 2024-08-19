package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.view.ProjectSponsorView;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode
@Accessors(fluent = true)
public class BoSponsorView {
    UUID id;
    String name;
    String url;
    String logoUrl;

    @Getter(AccessLevel.NONE)
    Set<ProjectSponsorView> projects;
    Set<UserShortView> leads;

    public BoSponsorView(UUID id, String name, String url, String logoUrl, Set<ProjectSponsorView> projects, Set<UserShortView> leads) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.logoUrl = logoUrl;
        this.projects = projects;
        this.leads = leads;
    }

    public Set<ProjectSponsorView> projectsWhereSponsorIsActive() {
        return projects.stream()
                .filter(ProjectSponsorView::isActive)
                .collect(Collectors.toSet());
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
