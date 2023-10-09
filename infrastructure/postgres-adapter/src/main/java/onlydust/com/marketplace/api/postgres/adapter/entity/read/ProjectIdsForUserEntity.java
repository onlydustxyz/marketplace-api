package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class ProjectIdsForUserEntity {

    @Id
    @Column(name = "project_id")
    UUID id;
    @Column(name = "is_lead")
    Boolean isLead;
    @Column(name = "name")
    String name;
    @Column(name = "logo_url")
    String logoUrl;
}
