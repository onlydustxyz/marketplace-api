package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class HackathonHelper {
    private final Faker faker = new Faker();
    @Autowired
    private HackathonStoragePort hackathonStoragePort;

    public Hackathon.Id createHackathon(Hackathon.Status status, List<String> labels, List<ProjectId> projectIds) {
        final Hackathon.Id id = Hackathon.Id.random();
        final Hackathon hackathon = Hackathon.builder()
                .title(faker.rickAndMorty().character())
                .location(faker.lorem().word())
                .endDate(ZonedDateTime.now())
                .status(status)
                .startDate(ZonedDateTime.now())
                .id(id)
                .description(faker.lorem().sentence())
                .build();
        hackathon.githubLabels().addAll(labels);
        hackathonStoragePort.save(hackathon);
        return id;
    }
}
