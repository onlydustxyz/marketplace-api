package onlydust.com.marketplace.bff.read.entities.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.UserLinkResponse;
import onlydust.com.backoffice.api.contract.model.UserPageItemResponse;
import onlydust.com.marketplace.bff.read.entities.hackathon.HackathonRegistrationReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_users", schema = "iam")
public class AllUserReadEntity {
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
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserReadEntity registered;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    Set<HackathonRegistrationReadEntity> hackathonRegistrations;

    public UserPageItemResponse toBoPageItemResponse() {
        return new UserPageItemResponse()
                .id(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ofNullable(registered).map(u -> u.lastSeenAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .signedUpAt(ofNullable(registered).map(u -> u.createdAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                ;
    }

    public UserLinkResponse toLinkResponse() {
        return new UserLinkResponse()
                .userId(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl);
    }
}
