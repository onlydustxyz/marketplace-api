package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ShortProjectViewEntity {
    @Id
    UUID id;
    String key;
    String name;
    String shortDescription;
    String longDescription;
    String logoUrl;
    String telegramLink;
    Boolean hiring;
    @Enumerated(EnumType.STRING)
    @Type(type = "project_visibility")
    ProjectVisibilityEnumEntity visibility;
}
