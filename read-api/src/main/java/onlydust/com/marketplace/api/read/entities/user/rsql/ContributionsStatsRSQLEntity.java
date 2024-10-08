package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "contributions_stats_per_user", schema = "public")
public class ContributionsStatsRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long contributorId;

    @NonNull
    @Column(name = "contributionCount")
    Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributorId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity contributor;
}
