package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.contract.model.EcosystemLinkResponse;
import onlydust.com.marketplace.api.contract.model.LanguageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@NoArgsConstructor
@Immutable
public class ProjectPageItemFiltersQueryEntity {
    @Id
    Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;

    public List<ProjectCategoryResponse> categories() {
        return categories == null ? List.of() : categories;
    }

    public List<EcosystemLinkResponse> ecosystems() {
        return ecosystems == null ? List.of() : ecosystems;
    }

    public List<LanguageResponse> languages() {
        return languages == null ? List.of() : languages;
    }
}
