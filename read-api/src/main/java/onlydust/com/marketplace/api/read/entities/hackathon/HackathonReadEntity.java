package onlydust.com.marketplace.api.read.entities.hackathon;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HackathonStatus;
import onlydust.com.backoffice.api.contract.model.HackathonsEvent;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathons", schema = "public")
@Immutable
public class HackathonReadEntity {
    @Id
    UUID id;
    @NonNull
    String slug;
    @NonNull
    Integer index;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "hackathon_status")
    @NonNull
    Hackathon.Status status;

    @NonNull
    String title;
    String description;
    String location;
    String budget;
    @NonNull
    Date startDate;
    @NonNull
    Date endDate;

    @Type(value = StringArrayType.class)
    @Column(nullable = false, columnDefinition = "text[]")
    String[] githubLabels;

    @JdbcTypeCode(SqlTypes.JSON)
    List<NamedLink> communityLinks;

    @JdbcTypeCode(SqlTypes.JSON)
    List<NamedLink> links;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "hackathon_sponsors",
            joinColumns = @JoinColumn(name = "hackathonId"),
            inverseJoinColumns = @JoinColumn(name = "sponsorId")
    )
    @NonNull
    Set<SponsorReadEntity> sponsors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "hackathon_projects",
            joinColumns = @JoinColumn(name = "hackathonId"),
            inverseJoinColumns = @JoinColumn(name = "projectId")
    )
    @NonNull
    Set<ProjectLinkReadEntity> projects;

    @OneToMany(mappedBy = "hackathonId", fetch = FetchType.LAZY)
    @NonNull
    Set<HackathonRegistrationReadEntity> registrations;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "hackathonId")
    HackathonIssueCountReadEntity issueCounts;

    @OneToMany(mappedBy = "hackathonId", fetch = FetchType.LAZY)
    @NonNull
    Set<HackathonEventReadEntity> events;


    public HackathonsDetailsResponse toResponse(final Boolean isRegistered) {
        return new HackathonsDetailsResponse()
                .id(this.id)
                .slug(this.slug)
                .index(this.index)
                .me(Optional.ofNullable(isRegistered).map(value -> new HackathonsDetailsResponseAllOfMe().hasRegistered(value)).orElse(null))
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .totalBudget(this.budget)
                .subscriberCount(this.registrations.size())
                .issueCount(isNull(issueCounts) ? 0 : issueCounts.issueCount())
                .openIssueCount(isNull(issueCounts) ? 0 : issueCounts.openIssueCount())
                .startDate(ZonedDateTime.ofInstant(this.startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(this.endDate.toInstant(), ZoneOffset.UTC))
                .githubLabels(isNull(this.githubLabels) ? List.of() : Arrays.asList(this.githubLabels))
                .communityLinks(isNull(this.communityLinks) ? List.of() : this.communityLinks.stream().map(link -> new SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .links(isNull(this.links) ? List.of() : this.links.stream().map(link -> new SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .sponsors(sponsors.stream()
                        .sorted(Comparator.comparing(SponsorReadEntity::name))
                        .map(SponsorReadEntity::toLinkResponse)
                        .toList())
                .projects(projects.stream()
                        .map(ProjectLinkReadEntity::toShortResponse)
                        .sorted(Comparator.comparing(ProjectShortResponse::getName))
                        .toList())
                .events(events.stream()
                        .map(HackathonEventReadEntity::toDto)
                        .sorted(Comparator.comparing(HackathonsEventItemResponse::getStartDate)
                                .thenComparing(HackathonsEventItemResponse::getEndDate)
                                .thenComparing(HackathonsEventItemResponse::getName))
                        .toList());
    }


    public onlydust.com.backoffice.api.contract.model.HackathonsDetailsResponse toBoResponse() {
        return new onlydust.com.backoffice.api.contract.model.HackathonsDetailsResponse()
                .id(this.id)
                .slug(this.slug)
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .totalBudget(this.budget)
                .status(HackathonStatus.valueOf(this.status.name()))
                .startDate(ZonedDateTime.ofInstant(this.startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(this.endDate.toInstant(), ZoneOffset.UTC))
                .githubLabels(isNull(this.githubLabels) ? List.of() : Arrays.asList(this.githubLabels))
                .subscriberCount(this.registrations.size())
                .communityLinks(isNull(this.communityLinks) ? List.of() :
                        this.communityLinks.stream().map(link -> new onlydust.com.backoffice.api.contract.model.SimpleLink()
                                .value(link.getValue())
                                .url(link.getUrl())
                        ).toList())
                .links(isNull(this.links) ? List.of() : this.links.stream().map(link -> new onlydust.com.backoffice.api.contract.model.SimpleLink()
                        .value(link.getValue())
                        .url(link.getUrl())
                ).toList())
                .sponsors(this.sponsors.stream().map(sponsor -> new onlydust.com.backoffice.api.contract.model.SponsorResponse()
                        .id(sponsor.id())
                        .name(sponsor.name())
                        .url(sponsor.url())
                        .logoUrl(sponsor.logoUrl())
                ).toList())
                .projects(projects.stream().map(ProjectLinkReadEntity::toBoLinkResponse).toList())
                .events(events.stream()
                        .map(HackathonEventReadEntity::toBoDto)
                        .sorted(Comparator.comparing(HackathonsEvent::getStartDate)
                                .thenComparing(HackathonsEvent::getEndDate)
                                .thenComparing(HackathonsEvent::getName))
                        .toList());
    }
}
