package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class GithubRepositoryLinkedToProjectEntity {
    @Id
    Long id;
    String name;
    String owner;
    @Type(type = "jsonb")
    Map<String, Long> technologies;
    UUID projectId;

}
