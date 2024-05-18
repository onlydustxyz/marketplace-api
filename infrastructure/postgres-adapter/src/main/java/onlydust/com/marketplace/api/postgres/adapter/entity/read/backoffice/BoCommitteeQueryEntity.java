package onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.ProjectQuestionJsonEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import onlydust.com.marketplace.project.domain.view.ShortSponsorView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
@Immutable
public class BoCommitteeQueryEntity {

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
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectQuestionJsonEntity> projectQuestions;
    String sponsorName;
    String sponsorLogoUrl;

    public CommitteeView toView() {
        return CommitteeView.builder()
                .id(Committee.Id.of(this.id))
                .name(this.name)
                .status(this.status.toDomain())
                .startDate(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC))
                .endDate(ZonedDateTime.ofInstant(endDate.toInstant(), ZoneOffset.UTC))
                .projectQuestions(this.projectQuestions.stream()
                        .map(projectQuestionJsonEntity -> new Committee.ProjectQuestion(projectQuestionJsonEntity.question(),
                                projectQuestionJsonEntity.required())).toList())
                .sponsor(isNull(this.sponsorName) ? null : new ShortSponsorView(this.sponsorName, this.sponsorLogoUrl))
                .build();
    }

}
