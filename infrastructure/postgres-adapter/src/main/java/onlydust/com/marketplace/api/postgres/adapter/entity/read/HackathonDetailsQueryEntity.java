package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.HackathonTrack;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class HackathonDetailsQueryEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    String slug;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "hackathon_status")
    @NonNull
    Hackathon.Status status;

    @NonNull
    String title;
    @NonNull
    String subtitle;
    String description;
    String location;
    String budget;
    @NonNull
    Date startDate;
    @NonNull
    Date endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    List<NamedLink> links;

    @JdbcTypeCode(SqlTypes.JSON)
    List<SponsorViewEntity> sponsors;

    @JdbcTypeCode(SqlTypes.JSON)
    List<HackathonTrack> tracks;

    @JdbcTypeCode(SqlTypes.JSON)
    List<Project> projects;

    @JdbcTypeCode(SqlTypes.JSON)
    List<RegisteredContributorLinkView> registeredUsers;

    public record Project(@NonNull UUID id,
                          @NonNull String slug,
                          @NonNull String name,
                          String logoUrl,
                          @NonNull String shortDescription,
                          @NonNull ProjectVisibility visibility
    ) {
        public ProjectShortView toDomain() {
            return new ProjectShortView(id, slug, name, logoUrl, shortDescription, visibility);
        }
    }

    public HackathonDetailsView toDomain() {
        final Map<UUID, Project> projects = isNull(this.projects)
                ? Map.of()
                : this.projects.stream().collect(Collectors.toMap(Project::id, Function.identity(), (p1, p2) -> p1));

        return new HackathonDetailsView(
                Hackathon.Id.of(id),
                slug,
                status,
                title,
                subtitle,
                description,
                location,
                budget,
                ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC),
                ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC),
                isNull(links) ? List.of() : links,
                isNull(sponsors) ? List.of() : sponsors.stream().map(SponsorViewEntity::toDomain).toList(),
                isNull(tracks) ? List.of() : tracks.stream().map(track -> new HackathonDetailsView.Track(
                        track.name(),
                        track.subtitle(),
                        track.description(),
                        track.iconSlug(),
                        track.projectIds().stream().map(projectId -> projects.get(projectId).toDomain()).toList()
                )).toList(),
                projects.values().stream().map(Project::toDomain).toList(),
                isNull(registeredUsers) ? List.of() : registeredUsers
        );
    }
}
