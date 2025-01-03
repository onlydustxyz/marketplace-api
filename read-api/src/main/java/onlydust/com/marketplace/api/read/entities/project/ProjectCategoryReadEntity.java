package onlydust.com.marketplace.api.read.entities.project;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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
    private @NonNull String description;
    private @NonNull String iconSlug;
    @Formula("""
            (select count(ppc.project_id)
            from projects_project_categories ppc
            where ppc.project_category_id = id)
            """)
    private Integer projectCount;

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
                .description(description)
                .iconSlug(iconSlug)
                ;
    }

    public onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse toDto() {
        return new onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .description(description)
                .iconSlug(iconSlug)
                .projectCount(isNull(projectCount) ? 0 : projectCount)
                ;
    }
}
