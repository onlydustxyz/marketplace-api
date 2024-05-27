package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeCommitteeApiIT extends AbstractMarketplaceApiIT {

    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    static Committee committee;

    @Test
    @Order(1)
    void should_get_no_assignements() {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        committee = committeeFacadePort.createCommittee("Mr. Needful",
                ZonedDateTime.parse("2024-05-19T02:58:44.399Z"),
                ZonedDateTime.parse("2024-05-25T20:06:27.482Z"));
        committee = committee.toBuilder()
                .status(Committee.Status.DRAFT)
                .votePerJury(1)
                .build();

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_ASSIGNEMENTS.formatted(committee.id())))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    @Order(2)
    void should_get_my_committee_assignements() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        committee.projectQuestions().addAll(List.of(q1, q2));
        committee.juryIds().add(olivier.user().getId());
        committee.juryCriteria().add(new JuryCriteria(JuryCriteria.Id.random(), "Criteria 1"));
        committeeFacadePort.update(committee);
        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_APPLICATIONS);

        projectLeadRepository.save(new ProjectLeadEntity(bretzel, pierre.user().getId()));
        final Committee.ProjectAnswer projectAnswer = new Committee.ProjectAnswer(q1.id(), faker.lorem().paragraph());
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), new Committee.Application(pierre.user().getId(), bretzel, List.of(
                projectAnswer
        )));
        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_VOTES);

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_ASSIGNEMENTS.formatted(committee.id())))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Mr. Needful",
                          "status": "OPEN_TO_VOTES",
                          "projectAssignments": [
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "score": null
                            }
                          ]
                        }
                        """);
    }
}
