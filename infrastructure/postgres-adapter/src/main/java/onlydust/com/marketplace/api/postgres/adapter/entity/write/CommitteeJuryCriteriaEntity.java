package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "committee_jury_criteria", schema = "public")
public class CommitteeJuryCriteriaEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    String criteria;
    @NonNull
    UUID committeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    private CommitteeEntity committee;

    public static CommitteeJuryCriteriaEntity fromDomain(CommitteeEntity entity, JuryCriteria juryCriterion) {
        return CommitteeJuryCriteriaEntity.builder()
                .id(juryCriterion.id().value())
                .criteria(juryCriterion.criteria())
                .committeeId(entity.getId())
                .committee(entity)
                .build();
    }

    public JuryCriteria toDomain() {
        return JuryCriteria.builder()
                .criteria(this.criteria)
                .id(JuryCriteria.Id.of(this.id))
                .build();
    }
}
