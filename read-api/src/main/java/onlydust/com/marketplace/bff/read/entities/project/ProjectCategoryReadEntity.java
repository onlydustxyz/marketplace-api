package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryResponse;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Immutable
@Table(name = "project_categories", schema = "public")
public class ProjectCategoryReadEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    private @NonNull String name;
    private @NonNull String iconSlug;

    public ProjectCategoryResponse toBoDto() {
        return new ProjectCategoryResponse()
                .id(id)
                .name(name)
                .iconSlug(iconSlug)
                ;
    }

    public onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse toDto() {
        return new onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse()
                .id(id)
                .name(name)
                .iconSlug(iconSlug)
                ;
    }
}
