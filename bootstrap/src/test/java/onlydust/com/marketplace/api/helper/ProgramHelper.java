package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgramHelper {
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private BackofficeFacadePort backofficeFacadePort;
    @Autowired
    private AccountingFacadePort accountingFacadePort;

    private final Faker faker = new Faker();

    public Program create() {
//        final var sponsor = backofficeFacadePort.createSponsor(
//                faker.lordOfTheRings().character() + " " + faker.random().nextLong(),
//                URI.create(faker.internet().url()),
//                URI.create(faker.internet().url()));
        return Program.builder()
//                .id(sponsor.id())
//                .name(sponsor.name())
                .build();
    }

    public Program create(UserAuthHelper.AuthenticatedUser lead) {
        final var program = create();
//        addLead(Sponsor.Id.of(sponsor.id()), lead);
        return program;
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
