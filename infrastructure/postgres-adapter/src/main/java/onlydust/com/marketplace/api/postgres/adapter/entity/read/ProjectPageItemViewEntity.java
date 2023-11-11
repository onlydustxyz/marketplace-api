package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.model.ProjectVisibility;
import onlydust.com.marketplace.api.domain.view.ProjectCardView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.domain.view.SponsorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
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
    @Type(type = "jsonb")
    List<Sponsor> sponsors;
    @Type(type = "jsonb")
    List<ProjectLead> projectLeads;
    @Type(type = "jsonb")
    List<Map<String, Long>> technologies;

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
    public static class Sponsor {

        @JsonProperty("url")
        String url;

        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
    }

    public ProjectCardView toView() {
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
        if (nonNull(this.sponsors) && !this.sponsors.isEmpty()) {
            this.sponsors.forEach(sponsor -> view.addSponsor(SponsorView.builder()
                    .id(sponsor.id)
                    .logoUrl(sponsor.logoUrl)
                    .name(sponsor.name)
                    .url(sponsor.url)
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
        }
        return view;
    }

    public static String getSponsorsJsonPath(List<String> sponsors) {
        if (isNull(sponsors) || sponsors.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", sponsors.stream().map(s -> "@.name == \"" + s + "\"").toList()) + ")";
    }

    public static String getTechnologiesJsonPath(List<String> technologies) {
        if (isNull(technologies) || technologies.isEmpty()) {
            return null;
        }
        return "$[*] ? (" + String.join(" || ", technologies.stream().map(t -> "@.\"" + t + "\" > 0").toList()) + ")";
    }
}
