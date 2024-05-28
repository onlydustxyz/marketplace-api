package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.backoffice.api.contract.model.UserPageItemResponse;
import org.hibernate.annotations.Immutable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class UserShortEntity {
    @Id
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    Date lastSeenAt;
    Date createdAt;

    public UserPageItemResponse toDomain() {
        return new UserPageItemResponse()
                .id(id)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ZonedDateTime.ofInstant(lastSeenAt.toInstant(), ZoneOffset.UTC))
                .signedUpAt(ZonedDateTime.ofInstant(createdAt.toInstant(), ZoneOffset.UTC));
    }
}
