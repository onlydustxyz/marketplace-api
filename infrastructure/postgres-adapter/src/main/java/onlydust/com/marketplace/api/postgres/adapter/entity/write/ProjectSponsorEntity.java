package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Data
@Table(name = "projects_sponsors", schema = "public")
@Accessors(fluent = true)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = "sponsor_id")
    @EqualsAndHashCode.Exclude
    SponsorEntity sponsor;

    public ProjectSponsorEntity(@NonNull UUID projectId, @NonNull UUID sponsorId, Date lastAllocationDate) {
        this.projectId = projectId;
        this.sponsorId = sponsorId;
        this.lastAllocationDate = lastAllocationDate;
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID sponsorId;
    }
}
