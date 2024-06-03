package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import onlydust.com.backoffice.api.contract.model.ProjectCategoryPageItemResponse;
import onlydust.com.backoffice.api.contract.model.ProjectCategorySuggestionStatus;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Immutable
public class ProjectCategoryPageItemReadEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String name;
    private String iconSlug;
    @Enumerated(EnumType.STRING)
    private ProjectCategorySuggestionStatus status;
    private Integer projectCount;

    public ProjectCategoryPageItemResponse toDto() {
        return new ProjectCategoryPageItemResponse()
                .id(id)
                .name(name)
                .iconSlug(iconSlug)
                .status(status)
                .projectCount(projectCount)
                ;
    }
}
