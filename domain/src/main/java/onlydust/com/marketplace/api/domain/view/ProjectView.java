package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ProjectView {
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
    Integer repositoryCount;
    Integer contributorCount;
    @Builder.Default
    Set<RepositoryView> repositories = new HashSet<>();

    public void addProjectLead(final ProjectLeadView projectLeadView) {
        this.getProjectLeadViews().add(projectLeadView);
    }

    public void addSponsor(final SponsorView sponsorView) {
        this.getSponsors().add(sponsorView);
    }

    public void addRepository(final RepositoryView repositoryView){
        this.getRepositories().add(repositoryView);
    }
}
