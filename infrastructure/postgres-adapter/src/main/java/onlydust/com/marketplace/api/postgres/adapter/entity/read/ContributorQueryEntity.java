package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class ContributorQueryEntity {
    @Id
    Long githubUserId;
    String login;
    String avatarUrl;
    Boolean isRegistered;
}
