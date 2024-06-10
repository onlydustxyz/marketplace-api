package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.HackathonTrack;
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
import java.util.UUID;

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

    @Type(value = UUIDArrayType.class)
    @Column(nullable = false, columnDefinition = "uuid[]")
    UUID[] sponsorIds;

    @JdbcTypeCode(SqlTypes.JSON)
    List<HackathonTrack> tracks;

    public static HackathonEntity of(Hackathon hackathon) {
        return HackathonEntity.builder()
                .id(hackathon.id().value())
                .slug(hackathon.slug())
                .status(hackathon.status())
                .title(hackathon.title())
                .subtitle(hackathon.subtitle())
                .description(hackathon.description())
                .location(hackathon.location())
                .budget(hackathon.totalBudget())
                .startDate(Date.from(hackathon.startDate().toInstant()))
                .endDate(Date.from(hackathon.endDate().toInstant()))
                .links(hackathon.links())
                .sponsorIds(hackathon.sponsorIds().toArray(UUID[]::new))
                .tracks(hackathon.tracks().stream().map(HackathonTrack::of).toList())
                .build();
    }

    public Hackathon toDomain() {
        final Hackathon hackathon = Hackathon.builder()
                .id(Hackathon.Id.of(id))
                .description(description)
                .title(title)
                .subtitle(subtitle)
                .totalBudget(budget)
                .status(Hackathon.Status.valueOf(status.name()))
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .build();
        if (nonNull(tracks)) {
            hackathon.tracks().addAll(tracks.stream().map(HackathonTrack::toDomain).toList());
        }
        if (nonNull(sponsorIds)) {
            hackathon.sponsorIds().addAll(List.of(sponsorIds));
        }
        if (nonNull(links)) {
            hackathon.links().addAll(links);
        }
        return hackathon;
    }
}
