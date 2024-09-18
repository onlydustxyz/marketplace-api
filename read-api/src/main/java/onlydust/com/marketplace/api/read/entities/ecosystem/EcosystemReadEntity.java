package onlydust.com.marketplace.api.read.entities.ecosystem;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.EcosystemResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemDetailsResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemLinkResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemPageItemResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemShortResponseBanners;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectCategoryReadEntity;
import onlydust.com.marketplace.api.read.entities.project.PublicProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.api.read.mapper.ProjectMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "ecosystems", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemReadEntity {
    @Id
    @NonNull
    UUID id;

    @NaturalId
    @NonNull
    String slug;

    @NonNull
    String name;
    String description;
    @NonNull
    String logoUrl;
    String url;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ecosystem_leads",
            schema = "public",
            joinColumns = @JoinColumn(name = "ecosystemId"),
            inverseJoinColumns = @JoinColumn(name = "userId", referencedColumnName = "userId")
    )
    @NonNull
    Set<AllUserReadEntity> leads;

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

    public EcosystemLinkResponse toLinkResponse() {
        return new EcosystemLinkResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .hidden(hidden)
                ;
    }

    public EcosystemResponse toBoDetailsResponse() {
        return new EcosystemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .hidden(hidden)
                .leads(leads.stream()
                        .map(AllUserReadEntity::toBoLinkResponse)
                        .toList())
                ;
    }

    public onlydust.com.backoffice.api.contract.model.EcosystemPageItemResponse toBoPageItemResponse() {
        return new onlydust.com.backoffice.api.contract.model.EcosystemPageItemResponse()
                .id(id)
                .name(name)
                .logoUrl(logoUrl)
                .url(url)
                .slug(slug)
                .hidden(hidden)
                .leads(leads.stream().map(AllUserReadEntity::toBoLinkResponse).toList());
    }
}
