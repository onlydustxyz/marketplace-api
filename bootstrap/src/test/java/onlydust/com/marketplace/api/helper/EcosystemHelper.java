package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.EcosystemFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EcosystemHelper {
    @Autowired
    private EcosystemFacadePort ecosystemFacadePort;

    private final Faker faker = new Faker();

    public Ecosystem create(String name) {
        return create(name, List.of());
    }


    public Ecosystem create(String name, UserAuthHelper.AuthenticatedUser lead) {
        return create(name, List.of(lead.userId()));
    }

    public Ecosystem create(String name, List<UserId> leads) {
        return ecosystemFacadePort.createEcosystem(name,
                faker.internet().url(),
                faker.internet().url(),
                faker.lorem().sentence(),
                false,
                leads);
    }
}
