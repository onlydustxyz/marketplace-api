package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
@Entity
public class GithubRepoViewEntity {

  @Id
  Long id;
  String owner;
  String name;
  String htmlUrl;
  Date updatedAt;
  String Description;
  Long starsCount;
  Long forksCount;
}
