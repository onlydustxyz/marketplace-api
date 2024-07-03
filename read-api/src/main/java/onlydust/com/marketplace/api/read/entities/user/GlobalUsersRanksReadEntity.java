package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.UserRankCategory;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "global_users_ranks", schema = "public")
public class GlobalUsersRanksReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;

    @NonNull
    Long leadedProjectCount;

    @NonNull
    Long rank;

    @Formula("""
            case
                when rank_percentile <= 0.02 then 'A'
                when rank_percentile <= 0.04 then 'B'
                when rank_percentile <= 0.06 then 'C'
                when rank_percentile <= 0.08 then 'D'
                when rank_percentile <= 0.10 then 'E'
                else 'F'
            end
            """)
    @Enumerated(EnumType.STRING)
    @NonNull
    UserRankCategory rankCategory;
}
