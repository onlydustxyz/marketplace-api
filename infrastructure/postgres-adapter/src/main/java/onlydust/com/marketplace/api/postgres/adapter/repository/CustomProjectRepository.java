package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class CustomProjectRepository {

    protected static final String FIND_PROJECT_SPONSORS_QUERY = """
                select
                    s.*
                from sponsors s
                join projects_sponsors ps on ps.sponsor_id = s.id and ps.project_id = :projectId
            """;

    private final EntityManager entityManager;

    public List<SponsorEntity> getProjectSponsors(UUID projectId) {
        return entityManager
                .createNativeQuery(FIND_PROJECT_SPONSORS_QUERY, SponsorEntity.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public BigDecimal getUSDBudget(final UUID projectId) {
        final List budgets = entityManager.createNativeQuery("""
                        select b.remaining_amount
                        from projects_budgets pb
                                 join budgets b on pb.budget_id = b.id
                        where b.currency = 'usd' and pb.project_id = :projectId""")
                .setParameter("projectId", projectId)
                .getResultList();
        return budgets.isEmpty() ? null : (BigDecimal) budgets.get(0);

    }
}
