package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.json.ApplicationAnswerJsonEntity;
import onlydust.com.marketplace.project.domain.model.Committee;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_applications", schema = "public")
@IdClass(CommitteeApplicationEntity.PrimaryKey.class)
public class CommitteeApplicationEntity {

    @Id
    @NonNull
    UUID committeeId;
    @Id
    @NonNull
    UUID projectId;
    @NonNull
    UUID userId;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ApplicationAnswerJsonEntity> answers;

    public static CommitteeApplicationEntity fromDomain(final Committee.Id committeeId, final Committee.Application application) {
        return CommitteeApplicationEntity.builder()
                .committeeId(committeeId.value())
                .projectId(application.projectId())
                .userId(application.userId())
                .answers(application.answers().stream().map(projectAnswer -> ApplicationAnswerJsonEntity.builder()
                        .answer(projectAnswer.answer())
                        .required(projectAnswer.projectQuestion().required())
                        .question(projectAnswer.projectQuestion().question())
                        .build()
                ).toList())
                .build();

    }

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID committeeId;
    }
}
