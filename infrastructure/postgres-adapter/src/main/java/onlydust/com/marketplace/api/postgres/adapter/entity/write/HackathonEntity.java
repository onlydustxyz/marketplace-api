package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "hackathons", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class HackathonEntity {
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

    @OneToMany(mappedBy = "hackathonId", cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    Set<HackathonSponsorEntity> sponsors;

    @OneToMany(mappedBy = "hackathonId", cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    Set<HackathonProjectEntity> projects;

    @OneToMany(mappedBy = "hackathonId", cascade = CascadeType.ALL, orphanRemoval = true)
    @NonNull
    Set<HackathonEventEntity> events;

    public static HackathonEntity of(Hackathon hackathon) {
        return HackathonEntity.builder()
                .id(hackathon.id().value())
                .slug(hackathon.slug())
                .status(hackathon.status())
                .title(hackathon.title())
                .description(hackathon.description())
                .location(hackathon.location())
                .budget(hackathon.totalBudget())
                .startDate(Date.from(hackathon.startDate().toInstant()))
                .endDate(Date.from(hackathon.endDate().toInstant()))
                .githubLabels(hackathon.githubLabels().toArray(String[]::new))
                .communityLinks(hackathon.communityLinks().stream().toList())
                .links(hackathon.links().stream().toList())
                .sponsors(hackathon.sponsorIds().stream()
                        .map(sponsorId -> new HackathonSponsorEntity(hackathon.id().value(), sponsorId))
                        .collect(Collectors.toSet()))
                .projects(hackathon.projectIds().stream()
                        .map(projectId -> new HackathonProjectEntity(hackathon.id().value(), projectId))
                        .collect(Collectors.toSet()))
                .events(hackathon.events().stream().map(e -> HackathonEventEntity.of(hackathon.id(), e))
                        .collect(Collectors.toSet()))
                .build();
    }

    public Hackathon toDomain() {
        final Hackathon hackathon = Hackathon.builder()
                .id(Hackathon.Id.of(id))
                .status(Hackathon.Status.valueOf(status.name()))
                .title(title)
                .description(description)
                .location(location)
                .totalBudget(budget)
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .build();
        if (nonNull(githubLabels)) {
            hackathon.githubLabels().addAll(List.of(githubLabels));
        }
        if (nonNull(communityLinks)) {
            hackathon.communityLinks().addAll(communityLinks);
        }
        if (nonNull(links)) {
            hackathon.links().addAll(links);
        }
        hackathon.sponsorIds().addAll(sponsors.stream().map(HackathonSponsorEntity::getSponsorId).toList());
        hackathon.projectIds().addAll(projects.stream().map(HackathonProjectEntity::getProjectId).toList());
        hackathon.events().addAll(events.stream().map(HackathonEventEntity::toDomain).toList());
        return hackathon;
    }
}
