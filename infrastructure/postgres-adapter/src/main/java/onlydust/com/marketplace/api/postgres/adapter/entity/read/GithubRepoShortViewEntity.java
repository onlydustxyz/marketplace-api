package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.util.Date;

@EqualsAndHashCode
@Data
@Entity
@Immutable
public class GithubRepoShortViewEntity {
    @Id
    Long id;
    String ownerLogin;
    String name;
    String htmlUrl;
    Date updatedAt;
    String description;
    Long starsCount;
    Long forksCount;
}
