package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.backoffice.api.contract.model.ProjectCategorySuggestionResponse;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "project_category_suggestions", schema = "public")
public class ProjectCategorySuggestionReadEntity {
    @Id
    private @NonNull UUID id;
    private @NonNull String name;

    public ProjectCategorySuggestionResponse toDto() {
        return new ProjectCategorySuggestionResponse()
                .id(id)
                .name(name);
    }
}
