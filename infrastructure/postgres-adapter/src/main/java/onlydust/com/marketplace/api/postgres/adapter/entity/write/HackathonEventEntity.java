package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Accessors(chain = true, fluent = true)
@Table(name = "hackathon_events", schema = "public")
public class HackathonEventEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    UUID hackathonId;
    @NonNull
    String name;
    @NonNull
    String subtitle;
    @NonNull
    String iconSlug;
    @NonNull
    ZonedDateTime startAt;
    @NonNull
    ZonedDateTime endAt;
    @NonNull
    @JdbcTypeCode(SqlTypes.JSON)
    List<NamedLink> links;

    public static HackathonEventEntity of(Hackathon.Id hackathonId, Hackathon.Event event) {
        return new HackathonEventEntity()
                .id(event.id())
                .hackathonId(hackathonId.value())
                .name(event.name())
                .subtitle(event.subtitle())
                .iconSlug(event.iconSlug())
                .startAt(event.startDate())
                .endAt(event.endDate())
                .links(event.links().stream().toList());
    }

    public Hackathon.Event toDomain() {
        return Hackathon.Event.builder()
                .id(id)
                .name(name)
                .subtitle(subtitle)
                .iconSlug(iconSlug)
                .startDate(startAt)
                .endDate(endAt)
                .links(new HashSet<>(links))
                .build();
    }
}
