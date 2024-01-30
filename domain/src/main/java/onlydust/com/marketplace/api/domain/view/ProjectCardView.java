package onlydust.com.marketplace.api.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;

import java.util.*;

import static java.util.Objects.nonNull;

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
    Set<EcosystemView> ecosystems = new HashSet<>();
    @Builder.Default
    Set<ProjectLeaderLinkView> leaders = new HashSet<>();
    @Builder.Default
    Map<String, Long> technologies = new HashMap<>();
    @Builder.Default
    Boolean isInvitedAsProjectLead = false;
    @Builder.Default
    Boolean isMissingGithubAppInstallation = null;
    @Builder.Default
    Set<Project.Tag> tags = new HashSet<>();

    public void addProjectLeader(final ProjectLeaderLinkView leader) {
        this.getLeaders().add(leader);
    }

    public void addEcosystem(final EcosystemView ecosystem) {
        this.getEcosystems().add(ecosystem);
    }

    public void addTechnologies(final Map<String, Long> technologiesToAdd) {
        if (nonNull(technologiesToAdd)) {
            technologiesToAdd.forEach((key, value) -> {
                if (this.getTechnologies().containsKey(key)) {
                    this.getTechnologies().replace(key, this.getTechnologies().get(key) + value);
                } else {
                    this.getTechnologies().put(key, value);
                }
            });
        }
    }

    public void addTag(final Project.Tag tag){
        this.getTags().add(tag);
    }

    public enum SortBy {
        CONTRIBUTORS_COUNT, REPOS_COUNT, RANK, NAME;
    }

    public enum FilterBy {
        TECHNOLOGIES, ECOSYSTEMS
    }
}
