package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
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
    UUID id;
    @NonNull
    Date applicationStartDate;
    @NonNull
    Date applicationEndDate;
    @NonNull
    String name;
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "committee_status")
    CommitteeStatusEntity status;
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
                .status(CommitteeStatusEntity.fromDomain(committee.status()))
                .sponsorId(committee.sponsorId())
                .votePerJury(committee.votePerJury())
                .build();
    }

    public Committee toDomain() {
        return Committee.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .applicationStartDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .applicationEndDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .status(this.status.toDomain())
                .votePerJury(this.votePerJury)
                .build();
    }

}
