package onlydust.com.marketplace.project.domain.view;

import lombok.*;
import onlydust.com.marketplace.project.domain.model.MoreInfoLink;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
public class ProjectDetailsView {
    UUID id;
    String slug;
    String name;
    Date createdAt;
    String shortDescription;
    String longDescription;
    String logoUrl;
    List<MoreInfoLink> moreInfos;
    Boolean hiring;
    ProjectVisibility visibility;
    Integer contributorCount;
    @Builder.Default
    List<ContributorLinkView> topContributors = new ArrayList<>();
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Set<ProjectOrganizationView> organizations = new HashSet<>();
    @Builder.Default
    Set<EcosystemView> ecosystems = new HashSet<>();
    @Builder.Default
    @Getter(AccessLevel.NONE)
    Set<ProjectSponsorView> sponsors = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> leaders = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> invitedLeaders = new HashSet<>();
    @Builder.Default
    @Setter(AccessLevel.NONE)
    Map<String, Long> technologies = new HashMap<>();
    Boolean hasRemainingBudget;
    ProjectRewardSettings rewardSettings;
    Me me;
    @Builder.Default
    Set<Project.Tag> tags = new HashSet<>();

    public Set<ProjectSponsorView> getActiveSponsors() {
        return sponsors.stream().filter(ProjectSponsorView::isActive).collect(Collectors.toSet());
    }

    public record Me(Boolean isLeader, Boolean isInvitedAsProjectLead, Boolean isContributor, Boolean hasApplied) {
        public Boolean isMember() {
            return isLeader || isInvitedAsProjectLead || isContributor;
        }
    }

    public void addOrganization(final ProjectOrganizationView organization) {
        organizations.add(organization);
        organization.getRepos().stream().filter(r -> r.isIncludedInProject).forEach(repo -> {
            addTechnologies(repo.getTechnologies());
        });
    }

    private void addTechnologies(final Map<String, Long> technologiesToAdd) {
        technologiesToAdd.forEach((key, value) -> {
            if (this.getTechnologies().containsKey(key)) {
                this.getTechnologies().replace(key, this.getTechnologies().get(key) + value);
            } else {
                this.getTechnologies().put(key, value);
            }
        });
    }

}
