package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
