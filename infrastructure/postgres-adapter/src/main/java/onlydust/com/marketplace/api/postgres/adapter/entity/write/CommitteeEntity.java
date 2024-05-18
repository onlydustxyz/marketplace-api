package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.ProjectQuestionJsonEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
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
    Date startDate;
    @NonNull
    Date endDate;
    @NonNull
    String name;
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "committee_status")
    CommitteeStatusEntity status;
    @Column(insertable = false, updatable = false)
    Date techCreatedAt;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectQuestionJsonEntity> projectQuestions;
    UUID sponsorId;

    public static CommitteeEntity fromDomain(final Committee committee) {
        return CommitteeEntity.builder()
                .id(committee.id().value())
                .name(committee.name())
                .endDate(Date.from(committee.endDate().toInstant()))
                .startDate(Date.from(committee.startDate().toInstant()))
                .status(CommitteeStatusEntity.fromDomain(committee.status()))
                .projectQuestions(committee.projectQuestions().stream()
                        .map(projectQuestion -> ProjectQuestionJsonEntity.builder().question(projectQuestion.question()).required(projectQuestion.required()).build())
                        .toList())
                .sponsorId(committee.sponsorId())
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

}
