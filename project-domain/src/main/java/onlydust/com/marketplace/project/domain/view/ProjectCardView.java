package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;

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
    Boolean isInvitedAsProjectLead = false;
    @Builder.Default
    Boolean isMissingGithubAppInstallation = null;
    @Builder.Default
    Set<Project.Tag> tags = new HashSet<>();
    @Builder.Default
    Set<LanguageView> languages = new HashSet<>();


    public void addProjectLeader(final ProjectLeaderLinkView leader) {
        this.getLeaders().add(leader);
    }

    public void addEcosystem(final EcosystemView ecosystem) {
        this.getEcosystems().add(ecosystem);
    }

    public void addLanguages(final LanguageView language) {
        this.getLanguages().add(language);
    }

    public void addTag(final Project.Tag tag) {
        this.getTags().add(tag);
    }

    public enum SortBy {
        CONTRIBUTORS_COUNT, REPOS_COUNT, RANK, NAME;
    }

    public enum FilterBy {
        ECOSYSTEMS, LANGUAGES
    }
}
