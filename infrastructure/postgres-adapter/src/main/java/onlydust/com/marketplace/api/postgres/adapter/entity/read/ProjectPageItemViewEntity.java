package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.model.Project;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.EcosystemView;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Entity
@Table(name = "project_details", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectPageItemViewEntity {
    @Id
    @Column(name = "project_id")
    UUID projectId;
    Boolean hiring;
    @Column(name = "logo_url")
    String logoUrl;
    String key;
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Type(type = "project_visibility")
    @Enumerated(EnumType.STRING)
    ProjectVisibilityEnumEntity visibility;
    Integer repoCount;
    Integer contributorsCount;
    Boolean isPendingProjectLead;
    Boolean isMissingGithubAppInstallation;
    @Type(type = "jsonb")
    List<Ecosystem> ecosystems;
    @Type(type = "jsonb")
    List<ProjectLead> projectLeads;
    @Type(type = "jsonb")
    List<Map<String, Long>> technologies;
    @Type(type = "jsonb")
    List<Tag> tags;

    public static String getEcosystemsJsonPath(List<UUID> ecosystemIds) {
        if (isNull(ecosystemIds) || ecosystemIds.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", ecosystemIds.stream().map(s -> "@.id == \"" + s + "\"").toList()) + ")";
    }

    public static String getTechnologiesJsonPath(List<String> technologies) {
        if (isNull(technologies) || technologies.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", technologies.stream().map(t -> "@.\"" + t + "\" > 0").toList()) + ")";
    }

    public static String getTagsJsonPath(List<Project.Tag> tags) {
        if (isNull(tags) || tags.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", tags.stream().map(Enum::name).map(s -> "@.name == \"" + s + "\"").toList()) + ")";
    }

    public ProjectCardView toView(UUID userId) {
        final ProjectCardView view = ProjectCardView.builder()
                .repoCount(this.repoCount)
                .id(this.projectId)
                .slug(this.key)
                .name(this.name)
                .shortDescription(this.shortDescription)
                .logoUrl(this.logoUrl)
                .hiring(this.hiring)
                .contributorCount(this.contributorsCount)
                .isInvitedAsProjectLead(this.isPendingProjectLead)
                .visibility(switch (this.visibility) {
                    case PUBLIC -> ProjectVisibility.PUBLIC;
                    case PRIVATE -> ProjectVisibility.PRIVATE;
                })
                .build();
        if (nonNull(this.technologies) && !this.technologies.isEmpty()) {
            this.technologies.forEach(view::addTechnologies);
        }
        if (nonNull(this.ecosystems) && !this.ecosystems.isEmpty()) {
            this.ecosystems.forEach(ecosystem -> view.addEcosystem(EcosystemView.builder()
                    .id(ecosystem.id)
                    .logoUrl(ecosystem.logoUrl)
                    .name(ecosystem.name)
                    .url(ecosystem.url)
                    .build()));
        }
        if (nonNull(this.projectLeads) && !this.projectLeads.isEmpty()) {
            this.projectLeads.forEach(projectLead -> view.addProjectLeader(ProjectLeaderLinkView.builder()
                    .avatarUrl(projectLead.avatarUrl)
                    .url(projectLead.url)
                    .githubUserId(projectLead.githubId)
                    .login(projectLead.login)
                    .id(projectLead.id)
                    .build()));
            if (userId != null && this.projectLeads.stream().anyMatch(lead -> userId.equals(lead.id))) {
                view.setIsMissingGithubAppInstallation(this.isMissingGithubAppInstallation);
            }
        }
        if (nonNull(this.tags)) {
            this.tags.forEach(tag -> view.addTag(switch (tag.name) {
                case "BEGINNERS_WELCOME" -> Project.Tag.BEGINNERS_WELCOME;
                case "STRONG_EXPERTISE" -> Project.Tag.STRONG_EXPERTISE;
                case "LIKELY_TO_SEND_REWARDS" -> Project.Tag.LIKELY_TO_SEND_REWARDS;
                case "FAST_PACED" -> Project.Tag.FAST_PACED;
                default -> throw OnlyDustException.internalServerError(String.format("Invalid project tag %s which is not contained in enum", tag));
            }));
        }
        return view;
    }

    @EqualsAndHashCode
    public static class ProjectLead {

        @JsonProperty("id")
        UUID id;

        @JsonProperty("url")
        String url;
        @JsonProperty("avatarUrl")
        String avatarUrl;
        @JsonProperty("login")
        String login;
        @JsonProperty("githubId")
        Long githubId;
    }

    @EqualsAndHashCode
    public static class Ecosystem {

        @JsonProperty("url")
        String url;

        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
    }

    @EqualsAndHashCode
    public static class Tag {
        @JsonProperty("name")
        String name;
    }
}
