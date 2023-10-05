package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class ProjectCardView {
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String logoUrl;
    Boolean hiring;
    @Builder.Default
    Set<SponsorView> sponsors = new HashSet<>();
    @Builder.Default
    Set<ProjectLeadView> projectLeadViews = new HashSet<>();
    Integer repoCount;
    Integer contributorCount;
    @Builder.Default
    Map<String, Integer> technologies = new HashMap<>();

    public void addProjectLead(final ProjectLeadView projectLeadView) {
        this.getProjectLeadViews().add(projectLeadView);
    }

    public void addSponsor(final SponsorView sponsorView) {
        this.getSponsors().add(sponsorView);
    }

    public void addTechnologies(final Map<String, Integer> technologiesToAdd) {
        technologiesToAdd.forEach((key, value) -> {
            if (this.getTechnologies().containsKey(key)) {
                this.getTechnologies().replace(key, this.getTechnologies().get(key) + value);
            } else {
                this.getTechnologies().put(key, value);
            }
        });
    }

    public enum SortBy {
        CONTRIBUTORS_COUNT, REPOS_COUNT, RANK, NAME;
    }
}
