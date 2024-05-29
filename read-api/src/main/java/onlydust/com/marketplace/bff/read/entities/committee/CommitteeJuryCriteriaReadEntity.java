package onlydust.com.marketplace.bff.read.entities.committee;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
@Table(name = "committee_jury_criteria", schema = "public")
@Accessors(fluent = true)
public class CommitteeJuryCriteriaReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    String criteria;
    @NonNull
    UUID committeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committeeId", insertable = false, updatable = false)
    private CommitteeReadEntity committee;
}
