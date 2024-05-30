package onlydust.com.marketplace.bff.read.entities.ecosystem;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemPageItemResponse;
import onlydust.com.marketplace.api.contract.model.EcosystemShortResponseBanners;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "ecosystems", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private UUID id;

    @NaturalId
    private @NonNull String slug;
    private @NonNull String name;
    private String description;

    @ManyToOne
    private EcosystemBannerReadEntity mdBanner;
    @ManyToOne
    private EcosystemBannerReadEntity xlBanner;

    @ManyToMany
    @JoinTable(name = "projects_ecosystems",
            joinColumns = @JoinColumn(name = "ecosystem_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<ProjectLinkViewEntity> projects;

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
                ;
    }
}
