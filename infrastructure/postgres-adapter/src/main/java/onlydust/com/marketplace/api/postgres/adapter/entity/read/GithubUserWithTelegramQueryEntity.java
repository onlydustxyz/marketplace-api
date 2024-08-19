package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.view.GithubUserWithTelegramView;
import org.hibernate.annotations.Immutable;

@EqualsAndHashCode
@Data
@Entity
@Immutable
public class GithubUserWithTelegramQueryEntity {
    @Id
    String githubLogin;
    String telegram;

    public GithubUserWithTelegramView toDomain() {
        return new GithubUserWithTelegramView(this.githubLogin, this.telegram);
    }
}
