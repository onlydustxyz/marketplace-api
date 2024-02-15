package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@Value
@Table(name = "projects_sponsors", schema = "public")
@IdClass(ProjectSponsorEntity.PrimaryKey.class)
public class ProjectSponsorEntity {
    @Id
    @NonNull
    @Column(name = "project_id")
    UUID projectId;
    @Id
    @NonNull
    @Column(name = "sponsor_id")
    UUID sponsorId;

    @Column(name = "last_allocation_date")
    Date lastAllocationDate;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID sponsorId;
    }
}
