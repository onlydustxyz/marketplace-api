package onlydust.com.marketplace.api.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import onlydust.com.marketplace.api.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;

import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
public class ProjectDetailsView {
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String moreInfoUrl;
    Boolean hiring;
    ProjectVisibility visibility;
    Integer contributorCount;
    @Builder.Default
    Set<ContributorLinkView> topContributors = new HashSet<>();
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Set<ProjectOrganizationView> organizations = new HashSet<>();
    @Builder.Default
    Set<SponsorView> sponsors = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> leaders = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> invitedLeaders = new HashSet<>();
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Map<String, Integer> technologies = new HashMap<>();
    BigDecimal remainingUsdBudget;
    ProjectRewardSettings rewardSettings;

    public void addOrganization(final ProjectOrganizationView organization) {
        organizations.add(organization);
        organization.getRepos().stream().filter(r -> r.isIncludedInProject).forEach(repo -> {
            addTechnologies(repo.getTechnologies());
        });
    }

    private void addTechnologies(final Map<String, Long> technologiesToAdd) {
        technologiesToAdd.forEach((key, value) -> {
            if (this.getTechnologies().containsKey(key)) {
                this.getTechnologies().replace(key, Math.toIntExact(this.getTechnologies().get(key) + value));
            } else {
                this.getTechnologies().put(key, Math.toIntExact(value));
            }
        });
    }
}
