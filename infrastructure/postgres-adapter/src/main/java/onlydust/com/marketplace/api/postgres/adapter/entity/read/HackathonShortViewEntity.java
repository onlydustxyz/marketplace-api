package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "hackathons", schema = "public")
@Immutable
public class HackathonShortViewEntity {
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
    String location;
    @NonNull
    Date startDate;
    @NonNull
    Date endDate;


    public HackathonShortView toDomain() {
        return new HackathonShortView(
                Hackathon.Id.of(id),
                slug,
                status,
                title,
                location,
                ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC),
                ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC)
        );
    }
}
