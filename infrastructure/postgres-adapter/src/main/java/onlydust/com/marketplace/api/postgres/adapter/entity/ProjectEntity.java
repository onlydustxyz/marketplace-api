package onlydust.com.marketplace.api.postgres.adapter.entity;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "project_details", schema = "public")
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectEntity {
    @Id
    @Column(name = "project_id", nullable = false)
    private UUID id;
    @Column(name = "name")
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Column(name = "telegram_link")
    String telegramLink;
    @Column(name = "logo_url")
    String logoUrl;
    @Column(name = "hiring")
    Boolean hiring;
    @Column(name = "rank")
    Integer rank;
    @Column(name = "key", insertable = false)
    String key;
    @Enumerated(EnumType.STRING)
    @Type(type = "project_visibility")
    @Column(columnDefinition = "visibility")
    private Visibility visibility;


    public enum Visibility {
        PRIVATE, PUBLIC;
    }
}
