package onlydust.com.marketplace.api.read.entities.hackathon;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HackathonStatus;
import onlydust.com.backoffice.api.contract.model.HackathonsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectLinkResponse;
import onlydust.com.marketplace.project.domain.model.Hackathon;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathons", schema = "public")
@Immutable
public class HackathonItemReadEntity {
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
    @NonNull
    Date startDate;
    @NonNull
    Date endDate;
    @Type(value = StringArrayType.class)
    String[] githubLabels;
    @JdbcTypeCode(SqlTypes.JSON)
    Set<ProjectLink> projects;
    int registrationsCount;
    Integer issueCount;
    Integer openIssueCount;

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
                .projects(isNull(projects) ? null : projects.stream()
                        .map(projectLink -> new ProjectLinkResponse().id(projectLink.id).slug(projectLink.slug).name(projectLink.name).logoUrl(projectLink.logoUrl))
                        .toList())
                .subscriberCount(this.registrationsCount)
                .issueCount(isNull(issueCount) ? 0 : issueCount)
                .openIssueCount(isNull(openIssueCount) ? 0 : openIssueCount);
    }

    public HackathonsPageItemResponse toHackathonsPageItemResponse() {
        return new HackathonsPageItemResponse()
                .id(id)
                .slug(slug)
                .status(HackathonStatus.valueOf(status.name()))
                .title(title)
                .githubLabels(isNull(this.githubLabels) ? List.of() : Arrays.asList(this.githubLabels))
                .subscriberCount(this.registrationsCount)
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC));
    }

    public record ProjectLink(
            UUID id,
            String name,
            String logoUrl,
            String slug) {
    }
}
