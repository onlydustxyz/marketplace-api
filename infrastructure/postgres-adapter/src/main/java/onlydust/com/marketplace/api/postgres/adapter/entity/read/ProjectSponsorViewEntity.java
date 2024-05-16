package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@Data
@Table(name = "projects_sponsors", schema = "public")
@Accessors(fluent = true)
@IdClass(ProjectSponsorViewEntity.PrimaryKey.class)
@Immutable
public class ProjectSponsorViewEntity {
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
    SponsorViewEntity sponsor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false, name = "project_id")
    @EqualsAndHashCode.Exclude
    ProjectViewEntity project;

    @EqualsAndHashCode
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID sponsorId;
    }
}
