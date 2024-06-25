package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "users_languages_ranks", schema = "public")
@IdClass(PerLanguageUsersRanksRSQLEntity.PrimaryKey.class)
public class PerLanguageUsersRanksRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long contributorId;

    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID languageId;

    @NonNull
    @Column(name = "rank")
    Long value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contributorId", referencedColumnName = "githubUserId", insertable = false, updatable = false)
    AllUserRSQLEntity contributor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "languageId", referencedColumnName = "id", insertable = false, updatable = false)
    LanguageReadEntity language;

    @EqualsAndHashCode
    @NoArgsConstructor
    public static class PrimaryKey implements Serializable {
        Long contributorId;
        UUID languageId;
    }
}
