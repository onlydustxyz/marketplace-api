package onlydust.com.marketplace.api.read.entities.hackathon;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HackathonStatus;
import onlydust.com.backoffice.api.contract.model.HackathonsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.SponsorViewEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import org.hibernate.annotations.*;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Objects.isNull;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathons", schema = "public")
@Immutable
public class HackathonReadEntity {
    @Id
    @EqualsAndHashCode.Include
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
    Set<SponsorViewEntity> sponsors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            schema = "public",
            name = "hackathon_projects",
            joinColumns = @JoinColumn(name = "hackathonId"),
            inverseJoinColumns = @JoinColumn(name = "projectId")
    )
    @NonNull
    Set<ProjectReadEntity> projects;

    @OneToMany(mappedBy = "hackathonId", fetch = FetchType.LAZY)
    @NonNull
    Set<HackathonRegistrationReadEntity> registrations;

    @Formula("""
            (SELECT count(distinct hi.issue_id)
             FROM hackathon_issues hi
             WHERE hi.hackathon_id = id)
            """)
    Integer issueCount;

    @Formula("""
            (SELECT count(distinct i.id)
             FROM hackathon_issues hi
                      JOIN indexer_exp.github_issues i on i.id = hi.issue_id
                      LEFT JOIN indexer_exp.github_issues_assignees gia ON gia.issue_id = i.id
             WHERE hi.hackathon_id = id
               AND i.status = 'OPEN'
               AND gia.user_id IS NULL)
            """)
    Integer openIssueCount;


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
                .issueCount(issueCount)
                .openIssueCount(openIssueCount)
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
                .sponsors(sponsors.stream().map(sponsor -> new SponsorResponse()
                                .id(sponsor.getId())
                                .name(sponsor.getName())
                                .url(sponsor.getUrl())
                                .logoUrl(sponsor.getLogoUrl())
                        )
                        .sorted(Comparator.comparing(SponsorResponse::getName))
                        .toList())
                .projects(projects.stream()
                        .map(ProjectReadEntity::toShortResponse)
                        .sorted(Comparator.comparing(ProjectShortResponse::getName))
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
                        .id(sponsor.getId())
                        .name(sponsor.getName())
                        .url(sponsor.getUrl())
                        .logoUrl(sponsor.getLogoUrl())
                ).toList())
                .projects(projects.stream().map(ProjectReadEntity::toBoLinkResponse).toList());
    }

    public HackathonsListItemResponse toHackathonsListItemResponse() {
        return new HackathonsListItemResponse()
                .id(id)
                .slug(slug)
                .index(index)
                .title(title)
                .githubLabels(isNull(this.githubLabels) ? List.of() : Arrays.asList(this.githubLabels))
                .location(location)
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .projects(projects.stream().map(ProjectReadEntity::toLinkResponse).toList())
                .subscriberCount(this.registrations.size())
                .issueCount(issueCount)
                .openIssueCount(openIssueCount);
    }

    public HackathonsPageItemResponse toHackathonsPageItemResponse() {
        return new HackathonsPageItemResponse()
                .id(id)
                .slug(slug)
                .status(HackathonStatus.valueOf(status.name()))
                .title(title)
                .githubLabels(isNull(this.githubLabels) ? List.of() : Arrays.asList(this.githubLabels))
                .subscriberCount(this.registrations.size())
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC));
    }
}
