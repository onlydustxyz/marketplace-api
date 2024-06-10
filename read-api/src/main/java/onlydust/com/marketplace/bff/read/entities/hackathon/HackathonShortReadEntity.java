package onlydust.com.marketplace.bff.read.entities.hackathon;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.HackathonsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListItemResponse;
import onlydust.com.marketplace.project.domain.model.Hackathon;
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
public class HackathonShortReadEntity {

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


    public HackathonsListItemResponse toHackathonsListItemResponse() {
        return new HackathonsListItemResponse()
                .id(id)
                .slug(slug)
                .title(title)
                .location(location)
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                ;
    }

    public HackathonsPageItemResponse toHackathonsPageItemResponse() {
        return new HackathonsPageItemResponse()
                .id(id)
                .slug(slug)
                .title(title)
                .location(location)
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC));
    }

}
