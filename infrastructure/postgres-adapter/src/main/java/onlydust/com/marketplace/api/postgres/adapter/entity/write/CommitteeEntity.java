package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
public class CommitteeEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    Date startDate;
    @NonNull
    Date endDate;
    @NonNull
    String name;
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "committee_status")
    Status status;
    @Column(insertable = false, updatable = false)
    Date techCreatedAt;

    public static CommitteeEntity fromDomain(final Committee committee) {
        return CommitteeEntity.builder()
                .id(committee.id().value())
                .name(committee.name())
                .endDate(Date.from(committee.endDate().toInstant()))
                .startDate(Date.from(committee.startDate().toInstant()))
                .status(Status.fromDomain(committee.status()))
                .build();
    }

    public Committee toDomain() {
        return Committee.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .status(this.status.toDomain())
                .build();
    }

    public CommitteeLinkView toLink() {
        return CommitteeLinkView.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .status(this.status.toDomain())
                .build();
    }

    public enum Status {
        DRAFT, OPEN_TO_APPLICATIONS, OPEN_TO_VOTES, CLOSED;

        public static Status fromDomain(final Committee.Status status) {
            return switch (status) {
                case DRAFT -> DRAFT;
                case CLOSED -> CLOSED;
                case OPEN_TO_VOTES -> OPEN_TO_VOTES;
                case OPEN_TO_APPLICATIONS -> OPEN_TO_APPLICATIONS;
            };
        }

        public Committee.Status toDomain() {
            return switch (this) {
                case DRAFT -> Committee.Status.DRAFT;
                case CLOSED -> Committee.Status.CLOSED;
                case OPEN_TO_VOTES -> Committee.Status.OPEN_TO_VOTES;
                case OPEN_TO_APPLICATIONS -> Committee.Status.OPEN_TO_APPLICATIONS;
            };
        }
    }
}
