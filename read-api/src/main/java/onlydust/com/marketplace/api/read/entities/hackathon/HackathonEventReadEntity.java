package onlydust.com.marketplace.api.read.entities.hackathon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.HackathonsEvent;
import onlydust.com.marketplace.api.contract.model.HackathonsEventItemResponse;
import onlydust.com.marketplace.api.contract.model.SimpleLink;
import onlydust.com.marketplace.project.domain.model.NamedLink;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Table(name = "hackathon_events", schema = "public")
@Immutable
public class HackathonEventReadEntity {
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

    public HackathonsEventItemResponse toDto() {
        return new HackathonsEventItemResponse()
                .name(name)
                .subtitle(subtitle)
                .iconSlug(iconSlug)
                .startDate(startAt)
                .endDate(endAt)
                .links(links.stream().map(link -> new SimpleLink().url(link.getUrl()).value(link.getValue())).toList());
    }

    public HackathonsEvent toBoDto() {
        return new HackathonsEvent()
                .id(id)
                .name(name)
                .subtitle(subtitle)
                .iconSlug(iconSlug)
                .startDate(startAt)
                .endDate(endAt)
                .links(links.stream().map(link -> new onlydust.com.backoffice.api.contract.model.SimpleLink().url(link.getUrl()).value(link.getValue())).toList());
    }
}
