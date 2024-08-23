package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

@Service
public class ProgramHelper {
    @Autowired
    private DatabaseHelper databaseHelper;
    @Autowired
    private ProgramFacadePort programFacadePort;

    private final Faker faker = new Faker();

    public Program create() {
        return programFacadePort.create(
                faker.lordOfTheRings().character() + " " + faker.random().nextLong(),
                URI.create(faker.internet().url()));
    }

    public Program create(UserAuthHelper.AuthenticatedUser lead) {
        final var program = create();
        addLead(program.id(), lead);
        return program;
    }

    public void addLead(ProgramId programId, UserAuthHelper.AuthenticatedUser lead) {
        databaseHelper.executeQuery("""
                INSERT INTO program_leads
                VALUES (:programId, :userId)
                ON CONFLICT DO NOTHING
                """, Map.of(
                "userId", lead.user().getId(),
                "programId", programId.value()
        ));
    }
}
