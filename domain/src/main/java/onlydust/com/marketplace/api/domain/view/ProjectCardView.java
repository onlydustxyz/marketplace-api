package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;

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
    ProjectVisibility visibility;
    Integer repoCount;
    Integer contributorCount;
    @Builder.Default
    Set<SponsorView> sponsors = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> leaders = new HashSet<>();
    @Builder.Default
    Map<String, Integer> technologies = new HashMap<>();

    public void addProjectLeader(final ProjectLeaderLinkView leader) {
        this.getLeaders().add(leader);
    }

    public void addSponsor(final SponsorView sponsor) {
        this.getSponsors().add(sponsor);
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
