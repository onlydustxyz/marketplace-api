package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibilityEnumEntity visibility;
}
