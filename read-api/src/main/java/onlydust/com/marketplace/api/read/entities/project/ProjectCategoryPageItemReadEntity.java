package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryPageItemResponse;
import onlydust.com.backoffice.api.contract.model.ProjectCategorySuggestionStatus;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
public class ProjectCategoryPageItemReadEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    private String slug;
    private @NonNull String name;
    private String iconSlug;
    @Enumerated(EnumType.STRING)
    private ProjectCategorySuggestionStatus status;
    private Integer projectCount;

    public ProjectCategoryPageItemResponse toDto() {
        return new ProjectCategoryPageItemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .iconSlug(iconSlug)
                .status(status)
                .projectCount(projectCount)
                ;
    }
}
