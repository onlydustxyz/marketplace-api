package onlydust.com.marketplace.bff.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@Immutable
@Accessors(fluent = true)
public class ProjectPageItemFiltersQueryEntity {
    @Id
    UUID id;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectPageItemQueryEntity.Ecosystem> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageReadEntity> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<Category> categories;

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class Category {
        @EqualsAndHashCode.Include
        @JsonProperty("id")
        UUID id;
        @JsonProperty("name")
        String name;
        @JsonProperty("iconSlug")
        String iconSlug;

        public ProjectCategoryResponse toDto() {
            return new ProjectCategoryResponse()
                    .id(id)
                    .name(name)
                    .iconSlug(iconSlug);
        }
    }
}
