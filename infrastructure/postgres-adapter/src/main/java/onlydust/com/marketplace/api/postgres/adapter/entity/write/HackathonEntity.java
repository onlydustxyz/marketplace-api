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

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

}
