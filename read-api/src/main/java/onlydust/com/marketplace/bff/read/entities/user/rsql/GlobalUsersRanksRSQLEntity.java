package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "global_users_ranks", schema = "public")
public class GlobalUsersRanksRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;

    @NonNull
    @Column(name = "rank")
    Long value;
}
