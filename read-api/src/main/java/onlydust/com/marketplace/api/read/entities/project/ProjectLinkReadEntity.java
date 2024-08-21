package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponse;
import onlydust.com.marketplace.api.contract.model.ProjectVisibility;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "projects", schema = "public")
@Immutable
@Accessors(fluent = true)
public class ProjectLinkReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "name")
    String name;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "slug")
    String slug;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "project_languages",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    Set<LanguageReadEntity> languages;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "project_leads",
            joinColumns = @JoinColumn(name = "projectId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId")
    )
    Set<AllUserReadEntity> leads;


    public ProjectLinkResponse toLinkResponse() {
        return new ProjectLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug);
    }

    public onlydust.com.backoffice.api.contract.model.ProjectLinkResponse toBoLinkResponse() {
        return new onlydust.com.backoffice.api.contract.model.ProjectLinkResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug);
    }

    public ProjectShortResponse toShortResponse() {
        return new ProjectShortResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .slug(slug)
                .shortDescription(shortDescription)
                .visibility(visibility)
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList());
    }

}
