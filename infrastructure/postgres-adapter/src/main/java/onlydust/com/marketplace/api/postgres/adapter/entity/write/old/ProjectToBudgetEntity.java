package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "projects_budgets", schema = "public")
public class ProjectToBudgetEntity {

    @EmbeddedId
    ProjectToBudgetIdEntity id;

    @Data
    @Builder
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectToBudgetIdEntity implements Serializable {
        @Column(name = "project_id")
        UUID projectId;
        @Column(name = "budget_id")
        UUID budgetId;
    }
}
