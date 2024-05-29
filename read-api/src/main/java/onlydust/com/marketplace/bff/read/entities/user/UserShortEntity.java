package onlydust.com.marketplace.bff.read.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class UserShortEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    Long githubUserId;
    String login;
    String avatarUrl;
    String email;
    Date lastSeenAt;
    Date createdAt;
}
