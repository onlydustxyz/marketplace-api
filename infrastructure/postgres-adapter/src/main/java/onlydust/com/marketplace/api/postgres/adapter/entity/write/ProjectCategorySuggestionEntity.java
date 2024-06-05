package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "project_category_suggestions", schema = "public")
public class ProjectCategorySuggestionEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String name;
    private @NonNull UUID projectId;

    public static ProjectCategorySuggestionEntity fromDomain(ProjectCategorySuggestion suggestion) {
        return ProjectCategorySuggestionEntity.builder()
                .id(suggestion.id().value())
                .name(suggestion.name())
                .projectId(suggestion.projectId())
                .build();
    }
}
