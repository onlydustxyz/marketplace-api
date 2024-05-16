package onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoGithubRepositoryLinkedToProjectQueryEntity {
    @EmbeddedId
    Id id;
    String name;
    String owner;
    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Long> technologies;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Serializable {
        Long id;
        UUID projectId;
    }
}
