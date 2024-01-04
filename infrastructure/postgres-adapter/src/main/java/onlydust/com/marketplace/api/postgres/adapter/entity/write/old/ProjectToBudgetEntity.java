package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
