package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProgramEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import onlydust.com.marketplace.api.read.repositories.ProgramReadRepository;
import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.port.input.ProgramFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class ProgramHelper {
    @Autowired
    private ProgramFacadePort programFacadePort;
    @Autowired
    private ProgramReadRepository programReadRepository;

    private final Faker faker = new Faker();
    @Autowired
    private SponsorHelper sponsorHelper;
    @Autowired
    private DatabaseHelper databaseHelper;

    @Transactional
    public Program create(SponsorId sponsorId) {
        return create(sponsorId, faker.lordOfTheRings().character() + " " + faker.random().nextLong());
    }

    @Transactional
    public Program create(SponsorId sponsorId, String name) {
        return programFacadePort.create(
                name,
                sponsorId,
                URI.create(faker.internet().url()),
                URI.create(faker.internet().url()),
                List.of());
    }

    @Transactional
    public Program create(SponsorId sponsorId, String name, UserAuthHelper.AuthenticatedUser lead) {
        final var program = create(sponsorId, name);
        addLead(program.id(), lead);
        return program;
    }

    @Transactional
    public Program create(SponsorId sponsorId, UserAuthHelper.AuthenticatedUser lead) {
        final var program = create(sponsorId);
        addLead(program.id(), lead);
        return program;
    }

    @Transactional
    public Program create(SponsorId sponsorId, UserAuthHelper.AuthenticatedBackofficeUser lead) {
        final var program = create(sponsorId);
        addLead(program.id(), lead);
        return program;
    }

    @Transactional
    public void addLead(ProgramId programId, UserAuthHelper.AuthenticatedUser lead) {
        addLead(programId, lead.user().getId());
    }

    @Transactional
    public void addLead(ProgramId programId, UserAuthHelper.AuthenticatedBackofficeUser lead) {
        addLead(programId, lead.user().getId());
    }

    private void addLead(ProgramId programId, UUID leadId) {
        final var program = programReadRepository.findById(programId.value()).orElseThrow();
        final var leadIds = new ArrayList<>(program.leads().stream().map(AllUserReadEntity::userId).map(UserId::of).toList());
        leadIds.add(UserId.of(leadId));
        programFacadePort.update(programId.value(),
                program.name(),
                Optional.ofNullable(program.url()).map(URI::create).orElse(null),
                Optional.ofNullable(program.logoUrl()).map(URI::create).orElse(null),
                leadIds);
    }

    @Transactional
    public ProgramId randomId() {
        return create(sponsorHelper.create().id()).id();
    }

    public Program getByName(String name) {
        final ProgramEntity program = databaseHelper.executeReadQuery("""
                select * from programs where name = :name limit 1
                """, Map.of("name", name));

        return program.toDomain();
    }
}
