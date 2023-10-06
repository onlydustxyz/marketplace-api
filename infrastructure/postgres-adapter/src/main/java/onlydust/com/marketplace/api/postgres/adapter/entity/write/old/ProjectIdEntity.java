package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "projects", schema = "public")
public class ProjectIdEntity {

    @Id
    @Column(name = "id")
    UUID id;
}
