package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    public JuryCriteria toDomain() {
        return JuryCriteria.builder()
                .criteria(this.criteria)
                .id(JuryCriteria.Id.of(this.id))
                .build();
    }
}
