package onlydust.com.marketplace.api.read.entities.user.rsql;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.UserSearchPageItemResponse;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.UserReadEntity;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
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

    @Formula("(select ci.contact from contact_informations ci where ci.user_id = user_id and ci.channel = 'telegram')")
    String telegram;

    @OneToOne(fetch = FetchType.EAGER)
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


    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    UnitedStatsPerUserRSQLEntity global;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language1;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language2;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language3;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language4;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language5;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language6;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language7;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language8;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerLanguagePerUserRSQLEntity> language9;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem1;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem2;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem3;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem4;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem5;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem6;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem7;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem8;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<UnitedStatsPerEcosystemPerUserRSQLEntity> ecosystem9;

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
                .language(language1.stream().map(UnitedStatsPerLanguagePerUserRSQLEntity::toDto).toList())
                .ecosystem(ecosystem1.stream().map(UnitedStatsPerEcosystemPerUserRSQLEntity::toDto).toList())
                ;
    }
}
