package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProfileCoverEnumEntity;
import onlydust.com.marketplace.project.domain.view.NewcomerView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZonedDateTime;

import static java.util.Objects.nonNull;

@EqualsAndHashCode
@Data
@Entity
public class NewcomerViewEntity {
    @Id
    Long id;
    String login;
    String htmlUrl;
    String avatarUrl;
    Boolean isRegistered;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "profile_cover")
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
