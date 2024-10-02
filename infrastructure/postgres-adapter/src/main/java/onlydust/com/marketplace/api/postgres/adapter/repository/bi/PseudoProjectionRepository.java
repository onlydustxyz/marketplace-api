package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Map;

@AllArgsConstructor
public abstract class PseudoProjectionRepository {
    private final @NonNull EntityManager entityManager;
    private final @NonNull String schema;
    private final @NonNull String name;
    private final @NonNull String primaryKey;

    public int refresh() {
        return entityManager.createNativeQuery("call refresh_pseudo_projection(:schema, :name, :pkName)")
                .setParameter("schema", schema)
                .setParameter("name", name)
                .setParameter("pkName", primaryKey)
                .executeUpdate();
    }

    @SneakyThrows
    protected int refresh(Map<String, Object> params) {
        return entityManager.createNativeQuery("call refresh_pseudo_projection(:schema, :name, cast(:params as jsonb))")
                .setParameter("schema", schema)
                .setParameter("name", name)
                .setParameter("params", new ObjectMapper().writeValueAsString(params))
                .executeUpdate();
    }
}
