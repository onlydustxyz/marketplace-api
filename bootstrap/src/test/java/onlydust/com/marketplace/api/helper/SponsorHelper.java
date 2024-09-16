package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.SponsorFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class SponsorHelper {
    @Autowired
    private DatabaseHelper databaseHelper;
    @Autowired
    private SponsorFacadePort sponsorFacadePort;
    @Autowired
    private SponsorRepository sponsorRepository;

    private final Faker faker = new Faker();

    public Sponsor create() {
        return create(faker.lordOfTheRings().character() + " " + faker.random().nextLong());
    }

    public Sponsor create(String name) {
        return sponsorFacadePort.createSponsor(
                name,
                URI.create(faker.internet().url()),
                URI.create(faker.internet().url()),
                List.of());
    }

    public Sponsor create(UserAuthHelper.AuthenticatedUser lead) {
        final var sponsor = create();
        addLead(sponsor.id(), lead);
        return sponsor;
    }

    public void addLead(SponsorId sponsorId, UserAuthHelper.AuthenticatedUser lead) {
        databaseHelper.executeQuery("""
                INSERT INTO sponsor_leads
                VALUES (:sponsorId, :userId)
                ON CONFLICT DO NOTHING
                """, Map.of(
                "userId", lead.user().getId(),
                "sponsorId", sponsorId.value()
        ));
    }
}
