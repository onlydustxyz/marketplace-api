package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "github_users", schema = "public")
public class GithubUserViewEntity {
    @Id
    @Column(name = "id", nullable = false)
    Integer githubId;
    @Column(name = "login", nullable = false)
    String login;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "html_url", nullable = false)
    String htmlUrl;
    @Column(name = "bio")
    String bio;
    @Column(name = "location")
    String location;
    @Column(name = "website")
    String website;
    @Column(name = "twitter")
    String twitter;
    @Column(name = "linkedin")
    String linkedin;
    @Column(name = "telegram")
    String telegram;
}
