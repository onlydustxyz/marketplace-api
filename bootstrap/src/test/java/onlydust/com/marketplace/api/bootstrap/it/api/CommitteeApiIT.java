package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.CommitteeApplicationRequest;
import onlydust.com.marketplace.api.contract.model.CommitteeProjectAnswerRequest;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommitteeApiIT extends AbstractMarketplaceApiIT {

    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    static Committee.Id committeeId;

    @Test
    @Order(1)
    void should_get_not_existing_application() {
        // Given
        final Committee committee = committeeFacadePort.createCommittee(faker.rickAndMorty().character(),
                faker.date().past(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()),
                faker.date().future(5, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));
        committeeId = committee.id();
        committeeFacadePort.updateStatus(committeeId, Committee.Status.OPEN_TO_APPLICATIONS);
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When

        // Then
    }

    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @Test
    @Order(2)
    void should_put_application() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        projectLeadRepository.save(new ProjectLeadEntity(bretzel, pierre.user().getId()));
        final CommitteeApplicationRequest committeeApplicationRequest = new CommitteeApplicationRequest();
        committeeApplicationRequest.setAnswers(List.of(
                new CommitteeProjectAnswerRequest()
                        .answer(faker.pokemon().name())
                        .question(faker.lordOfTheRings().character())
                        .required(false),
                new CommitteeProjectAnswerRequest()
                        .answer(faker.pokemon().name())
                        .question(faker.lordOfTheRings().character())
                        .required(true),
                new CommitteeProjectAnswerRequest()
                        .answer(null)
                        .question(faker.lordOfTheRings().character())
                        .required(false)
        ));

        // When
        client.put()
                .uri(getApiURI(PUT_COMMITTEES_APPLICATIONS.formatted(committeeId.value(), bretzel)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(committeeApplicationRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);

    }


}
