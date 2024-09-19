package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.SponsorFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class SponsorHelper {
    @Autowired
    private SponsorFacadePort sponsorFacadePort;

    private final Faker faker = new Faker();

    public Sponsor create() {
        return create(null, List.of());
    }

    public Sponsor create(UserAuthHelper.AuthenticatedUser... leads) {
        return create(null, List.of(leads));
    }

    public Sponsor create(String name) {
        return create(name, List.of());
    }

    public Sponsor create(String name, List<UserAuthHelper.AuthenticatedUser> leads) {
        return sponsorFacadePort.createSponsor(
                name != null ? name : faker.lordOfTheRings().character() + " " + faker.random().nextLong(),
                URI.create(faker.internet().url()),
                URI.create(faker.internet().url()),
                leads.stream().map(UserAuthHelper.AuthenticatedUser::userId).toList());
    }
}
