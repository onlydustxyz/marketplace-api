package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
public class ContributorViewEntity {
    @Id
    Long githubUserId;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
}
