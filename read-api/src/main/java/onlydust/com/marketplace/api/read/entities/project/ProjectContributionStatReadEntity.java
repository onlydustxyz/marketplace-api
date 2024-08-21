package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "project_contribution_stats", schema = "bi")
public class ProjectContributionStatReadEntity {
    @Id
    @NonNull
    UUID projectId;

    @NonNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectReadEntity project;

    int currentPeriodMergedPrCount;
    int lastPeriodMergedPrCount;
    int currentPeriodActiveContributorCount;
    int lastPeriodActiveContributorCount;
    int currentPeriodNewContributorCount;
    int lastPeriodNewContributorCount;
}
