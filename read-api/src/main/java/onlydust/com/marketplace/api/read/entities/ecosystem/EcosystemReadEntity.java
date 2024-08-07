package onlydust.com.marketplace.api.read.entities.ecosystem;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.EcosystemDetailsResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemPageItemResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemShortResponseBanners;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.mapper.ProjectMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "ecosystems", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NaturalId
    @NonNull
    String slug;
    @NonNull
    String name;
    String description;
    Integer featuredRank;
    Boolean hidden;

    @ManyToOne
    EcosystemBannerReadEntity mdBanner;
    @ManyToOne
    EcosystemBannerReadEntity xlBanner;

    @ManyToMany
    @JoinTable(name = "projects_ecosystems",
            joinColumns = @JoinColumn(name = "ecosystem_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    @NonNull
    Set<PublicProjectReadEntity> projects;

    @ManyToMany
    @JoinTable(name = "ecosystems_articles",
            joinColumns = @JoinColumn(name = "ecosystem_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id"))
    @NonNull
    Set<EcosystemArticleReadEntity> articles;

    @ManyToMany
    @JoinTable(name = "ecosystem_languages",
            joinColumns = @JoinColumn(name = "ecosystem_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    @NonNull
    Set<LanguageReadEntity> languages;

    @ManyToMany
    @JoinTable(name = "ecosystem_project_categories",
            joinColumns = @JoinColumn(name = "ecosystem_id"),
            inverseJoinColumns = @JoinColumn(name = "project_category_id"))
    @NonNull
    Set<ProjectCategoryReadEntity> projectCategories;

    public EcosystemPageItemResponse toPageItemResponse() {
        return new EcosystemPageItemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .description(description)
                .banners(new EcosystemShortResponseBanners()
                        .md(mdBanner == null ? null : mdBanner.toDto())
                        .xl(xlBanner == null ? null : xlBanner.toDto()))
                .projectCount(projects().size())
                .topProjects(projects.stream().limit(3).map(ProjectMapper::map).toList())
                .projectCategoryCount(projectCategories().size())
                .topProjectCategories(projectCategories.stream().limit(3).map(ProjectCategoryReadEntity::toDto).toList())
                ;
    }

    public EcosystemDetailsResponse toDetailsResponse() {
        return new EcosystemDetailsResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .description(description)
                .banners(new EcosystemShortResponseBanners()
                        .md(mdBanner == null ? null : mdBanner.toDto())
                        .xl(xlBanner == null ? null : xlBanner.toDto()))
                .relatedArticles(articles.stream().map(EcosystemArticleReadEntity::toDto).toList())
                ;
    }
}
