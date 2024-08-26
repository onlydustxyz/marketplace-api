package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Service
public class ProgramHelper {
    @Autowired
    private ProgramFacadePort programFacadePort;

    private final Faker faker = new Faker();

    public Program create() {
        return programFacadePort.create(
                faker.lordOfTheRings().character() + " " + faker.random().nextLong(),
                URI.create(faker.internet().url()),
                URI.create(faker.internet().url()),
                null);
    }

    public Program create(UserAuthHelper.AuthenticatedUser lead) {
        final var program = create();
        addLead(program.id(), lead);
        return program;
    }

    public Program create(UserAuthHelper.AuthenticatedBackofficeUser lead) {
        final var program = create();
        addLead(program.id(), lead);
        return program;
    }

    public void addLead(ProgramId programId, UserAuthHelper.AuthenticatedUser lead) {
        addLead(programId, lead.user().getId());
    }

    public void addLead(ProgramId programId, UserAuthHelper.AuthenticatedBackofficeUser lead) {
        addLead(programId, lead.user().getId());
    }

    private void addLead(ProgramId programId, UUID leadId) {
        programFacadePort.addLead(programId, UserId.of(leadId));
    }
}
