package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Ecosystem;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class EcosystemHelper {
    @Autowired
    private BackofficeFacadePort backofficeFacadePort;
    @Autowired
    private DatabaseHelper databaseHelper;

    private final Faker faker = new Faker();

    public Ecosystem create(String name) {
        final var ecosystem = Ecosystem.builder()
                .name(name)
                .description(faker.lorem().sentence())
                .hidden(false)
                .logoUrl(faker.internet().url())
                .url(faker.internet().url())
                .build();

        backofficeFacadePort.createEcosystem(ecosystem);

        return ecosystem;
    }

    public UUID getByName(String name) {
        return databaseHelper.executeReadQuery("""
                select id from ecosystems where name = :name limit 1
                """, Map.of("name", name));
    }
}
