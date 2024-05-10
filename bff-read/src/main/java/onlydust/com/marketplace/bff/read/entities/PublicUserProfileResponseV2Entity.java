package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.PublicUserProfileResponseV2;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class PublicUserProfileResponseV2Entity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull Long githubUserId;

    @OneToOne
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull BffUserViewEntity user;

//    UserProfileStatsSummary statsSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    List<UUID> ecosystems;

    public PublicUserProfileResponseV2 toDto() {
        return new PublicUserProfileResponseV2()
                .githubUserId(githubUserId)
                .login(user().login())
                .avatarUrl(user().avatarUrl())
                .id(user.userId())
                .htmlUrl(URI.create(user.github().htmlUrl()))
                .location(Optional.ofNullable(user.profile()).map(UserProfileViewEntity::location).orElse(user.github().location()))
                .bio(Optional.ofNullable(user.profile()).map(UserProfileViewEntity::bio).orElse(user.github().bio()))
                .website(Optional.ofNullable(user.profile()).map(UserProfileViewEntity::website).orElse(user.github().website()))
                .signedUpOnGithubAt(null) // TODO
                .signedUpAt(Optional.ofNullable(user.registered()).map(RegisteredUserViewEntity::createdAt).orElse(null))
                .lastSeenAt(Optional.ofNullable(user.registered()).map(RegisteredUserViewEntity::lastSeenAt).orElse(null))
                .contacts(Optional.ofNullable(user.profile()).flatMap(UserProfileViewEntity::contacts).orElse(user.github().contacts()))
                .statsSummary(null) // TODO
                .ecosystems(ecosystems)
                ;
    }
}
