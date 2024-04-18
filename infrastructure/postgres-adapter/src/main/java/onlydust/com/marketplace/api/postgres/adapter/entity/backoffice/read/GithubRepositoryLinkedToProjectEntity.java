package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
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
public class GithubRepositoryLinkedToProjectEntity {
    @EmbeddedId
    Id id;
    String name;
    String owner;
    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Long> technologies;

    @Embeddable
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Id implements Serializable {
        Long id;
        UUID projectId;
    }
}
