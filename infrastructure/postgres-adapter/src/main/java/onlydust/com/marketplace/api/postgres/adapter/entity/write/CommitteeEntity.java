package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committees", schema = "public")
public class CommitteeEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull UUID id;
    @NonNull Date applicationStartDate;
    @NonNull Date applicationEndDate;
    @NonNull String name;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @NonNull Committee.Status status;

    @Column(insertable = false, updatable = false)
    Date techCreatedAt;
    UUID sponsorId;
    Integer votePerJury;

    public static CommitteeEntity fromDomain(final Committee committee) {
        return CommitteeEntity.builder()
                .id(committee.id().value())
                .name(committee.name())
                .applicationEndDate(Date.from(committee.applicationEndDate().toInstant()))
                .applicationStartDate(Date.from(committee.applicationStartDate().toInstant()))
                .status(committee.status())
                .sponsorId(committee.sponsorId())
                .votePerJury(committee.votePerJury())
                .build();
    }

    public Committee toDomain() {
        return Committee.builder()
                .id(Committee.Id.of(id))
                .name(name)
                .applicationStartDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .applicationEndDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .status(status)
                .votePerJury(this.votePerJury)
                .build();
    }

    public CommitteeLinkView toLink() {
        return CommitteeLinkView.builder()
                .id(Committee.Id.of(id))
                .name(name)
                .startDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .status(status)
                .build();
    }

}
