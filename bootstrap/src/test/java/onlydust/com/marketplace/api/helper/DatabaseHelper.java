package onlydust.com.marketplace.api.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.intellij.lang.annotations.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

@Service
public class DatabaseHelper {
    @Autowired
    EntityManagerFactory entityManagerFactory;

    public void executeQuery(@Language("PostgreSQL") final @NonNull String query, final Map<String, Object> parameters) {
        final EntityManager em = entityManagerFactory.createEntityManager();

        em.getTransaction().begin();
        final var q = em.createNativeQuery(query);
        parameters.forEach(q::setParameter);
        q.executeUpdate();

        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public <Result> Result executeReadQuery(@Language("PostgreSQL") final @NonNull String query, final Map<String, Object> parameters) {
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
