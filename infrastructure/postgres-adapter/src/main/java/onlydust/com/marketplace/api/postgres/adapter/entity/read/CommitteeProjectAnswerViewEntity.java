package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Immutable
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "committee_project_answers", schema = "public")
@IdClass(CommitteeProjectAnswerViewEntity.PrimaryKey.class)
public class CommitteeProjectAnswerViewEntity {
    @Id
    @NonNull
    UUID committeeId;
    @Id
    @NonNull
    UUID projectId;
    @Id
    @NonNull
    UUID questionId;
    @NonNull
    UUID userId;
    String answer;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "questionId", insertable = false, updatable = false)
    CommitteeProjectQuestionViewEntity projectQuestion;

    @EqualsAndHashCode
    @AllArgsConstructor
    @Data
    @NoArgsConstructor(force = true)
    public static class PrimaryKey implements Serializable {
        UUID projectId;
        UUID committeeId;
        UUID questionId;
    }
}
