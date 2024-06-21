package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
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
    List<ProjectCategoryReadEntity> categories;
}
