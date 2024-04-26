package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.backoffice.UserShortView;
import org.hibernate.annotations.Immutable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoUserShortViewEntity {
    @Id
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    Date lastSeenAt;
    Date createdAt;

    public UserShortView toDomain() {
        return UserShortView.builder()
                .id(id)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ZonedDateTime.ofInstant(lastSeenAt.toInstant(), ZoneOffset.UTC))
                .signedUpAt(ZonedDateTime.ofInstant(createdAt.toInstant(), ZoneOffset.UTC))
                .build();
    }
}
