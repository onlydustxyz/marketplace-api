package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import static java.util.Objects.nonNull;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.domain.view.NewcomerView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@EqualsAndHashCode
@Data
@Entity
@TypeDef(name = "profile_cover", typeClass = PostgreSQLEnumType.class)
public class NewcomerViewEntity {

  @Id
  Long id;
  String login;
  String htmlUrl;
  String avatarUrl;
  Boolean isRegistered;
  @Enumerated(EnumType.STRING)
  @Type(type = "profile_cover")
  ProfileCoverEnumEntity cover;
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
        .cover(nonNull(cover) ? cover.toDomain() : null)
        .location(location)
        .bio(bio)
        .firstContributedAt(firstContributionCreatedAt)
        .build();
  }
}
