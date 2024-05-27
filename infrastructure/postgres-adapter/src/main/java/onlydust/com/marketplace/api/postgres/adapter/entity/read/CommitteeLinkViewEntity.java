package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Immutable
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "committees", schema = "public")
public class CommitteeLinkViewEntity {

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
    Integer projectCount;


    public CommitteeLinkView toLink() {
        return CommitteeLinkView.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .startDate(ZonedDateTime.ofInstant(applicationStartDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(applicationEndDate.toInstant(), ZoneOffset.UTC))
                .status(this.status.toDomain())
                .projectCount(this.projectCount)
                .build();
    }
}
