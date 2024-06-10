package onlydust.com.marketplace.bff.read.entities.hackathon;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.HackathonStatus;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Immutable
public class HackathonDetailsReadEntity {
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
    List<Track> tracks;

    @JdbcTypeCode(SqlTypes.JSON)
    Set<Project> projects;

    @JdbcTypeCode(SqlTypes.JSON)
    List<RegisteredContributorLinkView> registeredUsers;

    public record Project(@NonNull UUID id,
                          @NonNull String slug,
                          @NonNull String name,
                          String logoUrl,
                          @NonNull String shortDescription,
                          @NonNull ProjectVisibility visibility
    ) {
    }

    public record Track(@NonNull String name,
                        String subtitle,
                        String description,
                        String iconSlug,
                        @NonNull List<UUID> projectIds) {
    }

    public HackathonsDetailsResponse toResponse(final Boolean isRegistered) {
        return new HackathonsDetailsResponse()
                .id(this.id)
                .slug(this.slug)
                .me(Optional.ofNullable(isRegistered).map(value -> new HackathonsDetailsResponseMe().hasRegistered(value)).orElse(null))
                .title(this.title)
                .subtitle(this.subtitle)
                .description(this.description)
                .location(this.location)
                .totalBudget(this.budget)
                .startDate(ZonedDateTime.ofInstant(this.startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(this.endDate.toInstant(), ZoneOffset.UTC))
                .links(isNull(this.links) ? List.of() : this.links.stream().map(link -> new SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .sponsors(isNull(this.sponsors) ? List.of() : this.sponsors.stream().map(sponsor -> new SponsorResponse()
                        .id(sponsor.getId())
                        .name(sponsor.getName())
                        .url(sponsor.getUrl())
                        .logoUrl(sponsor.getLogoUrl())
                ).toList())
                .tracks(isNull(this.tracks) ? List.of() : this.tracks.stream().map(track -> new HackathonsTrackResponse()
                        .name(track.name())
                        .subtitle(track.subtitle())
                        .description(track.description())
                        .iconSlug(track.iconSlug())
                        .projects(track.projectIds.stream()
                                .map(projectId -> this.projects.stream().filter(project -> projectId.equals(project.id)).findFirst().orElseThrow(() -> OnlyDustException.internalServerError("Project %s not found in track".formatted(projectId))))
                                .map(project -> new ProjectShortResponse()
                                        .id(project.id())
                                        .slug(project.slug())
                                        .name(project.name())
                                        .logoUrl(project.logoUrl())
                                        .shortDescription(project.shortDescription())
                                        .visibility(onlydust.com.marketplace.api.contract.model.ProjectVisibility.valueOf(project.visibility().name()))
                                ).toList())
                ).toList())
                .projects(isNull(this.projects) ? List.of() : this.projects.stream().map(project -> new ProjectLinkResponse()
                        .id(project.id())
                        .slug(project.slug())
                        .name(project.name())
                        .logoUrl(project.logoUrl())
                ).toList());
    }


    public onlydust.com.backoffice.api.contract.model.HackathonsDetailsResponse toBoResponse() {
        return new onlydust.com.backoffice.api.contract.model.HackathonsDetailsResponse()
                .id(this.id)
                .slug(this.slug)
                .title(this.title)
                .subtitle(this.subtitle)
                .description(this.description)
                .location(this.location)
                .totalBudget(this.budget)
                .status(HackathonStatus.valueOf(this.status.name()))
                .startDate(ZonedDateTime.ofInstant(this.startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(this.endDate.toInstant(), ZoneOffset.UTC))
                .links(isNull(this.links) ? List.of() : this.links.stream().map(link -> new onlydust.com.backoffice.api.contract.model.SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .sponsors(isNull(this.sponsors) ? List.of() :
                        this.sponsors.stream().map(sponsor -> new onlydust.com.backoffice.api.contract.model.SponsorResponse()
                                .id(sponsor.getId())
                                .name(sponsor.getName())
                                .url(sponsor.getUrl())
                                .logoUrl(sponsor.getLogoUrl())
                        ).toList())
                .tracks(isNull(this.tracks) ? List.of() :
                        this.tracks.stream().map(track -> new onlydust.com.backoffice.api.contract.model.HackathonsTrackResponse()
                                .name(track.name())
                                .subtitle(track.subtitle())
                                .description(track.description())
                                .iconSlug(track.iconSlug())
                                .projects(track.projectIds.stream()
                                        .map(projectId -> this.projects.stream().filter(project -> projectId.equals(project.id)).findFirst().orElseThrow(() -> OnlyDustException.internalServerError("Project %s not found in track".formatted(projectId))))
                                        .map(project -> new onlydust.com.backoffice.api.contract.model.ProjectLinkResponse()
                                                .id(project.id())
                                                .slug(project.slug())
                                                .name(project.name())
                                                .logoUrl(project.logoUrl())
                                        ).toList())
                        ).toList());
    }
}
