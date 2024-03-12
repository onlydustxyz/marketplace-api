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
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class SponsorViewEntity {
    @Id
    UUID id;
    String name;
    String logoUrl;
    String url;
    @Type(type = "jsonb")
    List<ProjectLink> projects;

    @Data
    public static class ProjectLink {
        String name;
        String logoUrl;
        String slug;
        String shortDescription;
        UUID id;
    }
}
