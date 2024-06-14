package onlydust.com.marketplace.bff.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.UserSearchPageItemResponse;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.UserReadEntity;
import org.hibernate.annotations.Immutable;

import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Entity
@Immutable
@Accessors(fluent = true)
@Table(name = "all_users", schema = "iam")
public class AllUserRSQLEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
    String email;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserReadEntity registered;

    @ManyToMany
    @JoinTable(
            name = "projects_contributors",
            schema = "public",
            joinColumns = @JoinColumn(name = "githubUserId", referencedColumnName = "githubUserId"),
            inverseJoinColumns = @JoinColumn(name = "projectId", referencedColumnName = "id")
    )
    Set<ProjectReadEntity> contributedProjects;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    UnitedStatsPerUserRSQLEntity global;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem;


    public UserSearchPageItemResponse toBoPageItemResponse() {
        return new UserSearchPageItemResponse()
                .id(userId)
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .lastSeenAt(ofNullable(registered).map(u -> u.lastSeenAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .signedUpAt(ofNullable(registered).map(u -> u.createdAt().toInstant().atZone(ZoneId.systemDefault())).orElse(null))
                .global(ofNullable(global).map(UnitedStatsPerUserRSQLEntity::toDto).orElse(null))
                .language(language.stream().map(UnitedStatsPerLanguagePerUserRSQLEntity::toDto).toList())
                .ecosystem(ecosystem.stream().map(UnitedStatsPerEcosystemPerUserRSQLEntity::toDto).toList())
                ;
    }
}
