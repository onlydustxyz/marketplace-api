package onlydust.com.marketplace.api.read.entities.project;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Immutable
@Table(name = "project_category_suggestions", schema = "public")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ProjectCategorySuggestionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    private @NonNull String name;
    private @NonNull UUID projectId;
}
