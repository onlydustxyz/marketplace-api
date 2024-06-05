package onlydust.com.marketplace.bff.read.entities.project;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Accessors(fluent = true)
@Immutable
@Table(name = "project_categories", schema = "public")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProjectCategoryReadEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    private @NonNull String slug;
    private @NonNull String name;
    private @NonNull String iconSlug;

    @ManyToMany
    @JoinTable(name = "ecosystem_project_categories",
            joinColumns = @JoinColumn(name = "project_category_id"),
            inverseJoinColumns = @JoinColumn(name = "ecosystem_id"))
    private Set<EcosystemReadEntity> ecosystems;

    public ProjectCategoryResponse toBoDto() {
        return new ProjectCategoryResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .iconSlug(iconSlug)
                ;
    }

    public onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse toDto() {
        return new onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .iconSlug(iconSlug)
                ;
    }
}
