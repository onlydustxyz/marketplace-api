package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;

import java.net.URI;

@AllArgsConstructor
public class ProgramHelper {
    private final @NonNull EntityManagerFactory entityManagerFactory;
    private final @NonNull BackofficeFacadePort backofficeFacadePort;
    private final @NonNull Faker faker;

    public SponsorId create() {
        final var sponsor = backofficeFacadePort.createSponsor(
                faker.lordOfTheRings().character() + " " + faker.random().nextLong(),
                URI.create(faker.internet().url()),
                URI.create(faker.internet().url()));
        return SponsorId.of(sponsor.id());
    }

    public SponsorId create(UserAuthHelper.AuthenticatedUser lead) {
        final var sponsorId = create();
        addLead(sponsorId, lead);
        return sponsorId;
    }

    public void addLead(SponsorId sponsorId, UserAuthHelper.AuthenticatedUser lead) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("""
                        INSERT INTO sponsors_users
                        VALUES (:sponsorId, :userId)
                        ON CONFLICT DO NOTHING
                        """)
                .setParameter("userId", lead.user().getId())
                .setParameter("sponsorId", sponsorId.value())
                .executeUpdate();
        em.flush();
        em.getTransaction().commit();
        em.close();
    }
}
