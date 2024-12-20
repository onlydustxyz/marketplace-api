package onlydust.com.marketplace.api.postgres.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.output.FgaPort;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PostgresFgaAdapter implements FgaPort.Project {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    public void setMaintainers(ProjectId projectId, List<UserId> userIds) {
        executeQuery("select fga.ocerride_maintainers(cast(:projectId as fga.project), cast(:userIds as fga.user[]))",
                Map.of("projectId", projectId.value(), "userIds", userIds.stream().map(UserId::value).toArray()));
    }

    @Override
    public void addGrantingProgram(ProjectId projectId, ProgramId programId) {
        executeQuery("select fga.add_granting_programs(cast(:projectId as fga.project), cast(:programIds as fga.program[]))",
                Map.of("projectId", projectId.value(), "programIds", List.of(programId.value()).toArray()));
    }

    @Override
    public boolean canEdit(ProjectId projectId, UserId userId) {
        return executeReadQuery("select fga.can_edit(cast(:projectId as fga.project), cast(:userId as fga.user))",
                Map.of("projectId", projectId.value(), "userId", userId.value()));
    }

    @Override
    public boolean canEditPermissions(ProjectId projectId, UserId userId) {
        return executeReadQuery("select fga.can_edit_permissions(cast(:projectId as fga.project), cast(:userId as fga.user))",
                Map.of("projectId", projectId.value(), "userId", userId.value()));
    }

    @Override
    public boolean canReadFinancial(ProjectId projectId, UserId userId) {
        return executeReadQuery("select fga.can_read_financial(cast(:projectId as fga.project), cast(:userId as fga.user))",
                Map.of("projectId", projectId.value(), "userId", userId.value()));
    }

    private void executeQuery(@Language("PostgreSQL") final @NonNull String query, final Map<String, Object> parameters) {
        final EntityManager em = entityManagerFactory.createEntityManager();

        em.getTransaction().begin();
        final var q = em.createNativeQuery(query);
        parameters.forEach(q::setParameter);
        q.executeUpdate();

        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    private <Result> Result executeReadQuery(@Language("PostgreSQL") final @NonNull String query, final Map<String, Object> parameters) {
        EntityManager em = entityManagerFactory.createEntityManager();
        Result result = null;
        try {
            em.getTransaction().begin();
            final var q = em.createNativeQuery(query);
            parameters.forEach(q::setParameter);
            return (Result) q.getSingleResult();
        } finally {
            em.flush();
            em.getTransaction().commit();
            em.close();
        }
    }
}
