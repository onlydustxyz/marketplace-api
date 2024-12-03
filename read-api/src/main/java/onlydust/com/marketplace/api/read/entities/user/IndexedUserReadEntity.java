package onlydust.com.marketplace.api.read.entities.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.billing_profile.KycReadEntity;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_indexed_users", schema = "iam")
public class IndexedUserReadEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "ownerId", insertable = false, updatable = false)
    KycReadEntity kyc;
}
