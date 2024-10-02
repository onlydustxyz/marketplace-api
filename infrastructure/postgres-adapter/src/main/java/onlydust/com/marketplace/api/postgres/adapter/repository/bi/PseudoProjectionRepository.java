package onlydust.com.marketplace.api.postgres.adapter.repository.bi;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;

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

    protected <T> int refresh(T id) {
        return entityManager.createNativeQuery("call refresh_pseudo_projection(:schema, :name, :pkName, :id)")
                .setParameter("schema", schema)
                .setParameter("name", name)
                .setParameter("pkName", primaryKey)
                .setParameter("id", id)
                .executeUpdate();
    }
}
