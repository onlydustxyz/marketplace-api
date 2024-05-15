package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.view.NewcomerView;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;

@EqualsAndHashCode
@Data
@Entity
@Immutable
public class NewcomerQueryEntity {
    @Id
    Long id;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    String location;
    String bio;
    ZonedDateTime firstContributionCreatedAt;

    public NewcomerView toDomain() {
        return NewcomerView.builder()
                .githubId(id)
                .login(login)
                .htmlUrl(htmlUrl)
                .avatarUrl(avatarUrl)
                .isRegistered(isRegistered)
                .location(location)
                .bio(bio)
                .firstContributedAt(firstContributionCreatedAt)
                .build();
    }
}
