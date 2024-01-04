package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BudgetViewEntity;

@AllArgsConstructor
public class CustomProjectBudgetRepository {

  private static final String GET_PROJECT_BUDGETS_BY_PROJECT_ID = """
      select b.id,
             b.currency,
             b.remaining_amount,
             b.initial_amount,
             b.initial_amount * cuq.price as initial_amount_dollars_equivalent,
             b.remaining_amount * cuq.price as remaining_amount_dollars_equivalent,
             cuq.price dollars_conversion_rate
      from projects_budgets pb
              join budgets b on pb.budget_id = b.id and pb.project_id = :projectId
              left join crypto_usd_quotes cuq on cuq.currency = b.currency""";

  private final EntityManager entityManager;

  public List<BudgetViewEntity> findProjectBudgetByProjectId(final UUID projectId) {
    return entityManager.createNativeQuery(GET_PROJECT_BUDGETS_BY_PROJECT_ID, BudgetViewEntity.class)
        .setParameter("projectId", projectId)
        .getResultList();
  }
}
