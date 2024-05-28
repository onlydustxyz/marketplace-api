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

import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeCommitteeApiIT extends AbstractMarketplaceApiIT {

    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
    private final UUID apibara = UUID.fromString("2073b3b2-60f4-488c-8a0a-ab7121ed850c");

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    ProjectLeadRepository projectLeadRepository;
    static Committee committee;
    static JuryCriteria c1 = new JuryCriteria(JuryCriteria.Id.random(), "c1");
    static JuryCriteria c2 = new JuryCriteria(JuryCriteria.Id.random(), "c2");
    static ProjectQuestion q1 = new ProjectQuestion("Q1", false);
    static ProjectQuestion q2 = new ProjectQuestion("Q2", true);

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
                .votePerJury(2)
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
    void should_not_vote_when_not_open_for_votes() {
        // Given
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();
        final UserAuthHelper.AuthenticatedUser antho = userAuthHelper.authenticateAnthony();

        committee.projectQuestions().addAll(List.of(q1, q2));
        committee.juryIds().addAll(List.of(
                olivier.user().getId(),
                antho.user().getId()));

        committee.juryCriteria().addAll(List.of(c1, c2));
        committeeFacadePort.update(committee);
        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_APPLICATIONS);


        client.put()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "vote": 2
                            }
                          ]
                        }
                        """.formatted(c1.id()))
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(3)
    void should_get_my_committee_assignements() {
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        projectLeadRepository.save(new ProjectLeadEntity(bretzel, pierre.user().getId()));
        projectLeadRepository.save(new ProjectLeadEntity(apibara, pierre.user().getId()));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), new Committee.Application(pierre.user().getId(), bretzel, List.of(
                new Committee.ProjectAnswer(q1.id(), faker.lorem().paragraph())
        )));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), new Committee.Application(pierre.user().getId(), apibara, List.of(
                new Committee.ProjectAnswer(q1.id(), faker.lorem().paragraph()),
                new Committee.ProjectAnswer(q2.id(), faker.lorem().paragraph())
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
                                "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                                "slug": "apibara",
                                "name": "Apibara",
                                "logoUrl": null,
                                "shortDescription": "Listen to starknet events using gRPC and build your own node",
                                "visibility": null
                              },
                              "score": null
                            },
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                                "shortDescription": "A project for people who love fruits",
                                "visibility": null
                              },
                              "score": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(10)
    void should_vote_partially() {
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        client.put()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "vote": 2
                            }
                          ]
                        }
                        """.formatted(c1.id()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

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
                                "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                                "slug": "apibara",
                                "name": "Apibara",
                                "logoUrl": null,
                                "shortDescription": "Listen to starknet events using gRPC and build your own node",
                                "visibility": null
                              },
                              "score": null
                            },
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                                "shortDescription": "A project for people who love fruits",
                                "visibility": null
                              },
                              "score": 2.0
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(11)
    void should_vote_completely() {
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        client.put()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "vote": 3
                            },
                            {
                              "criteriaId": "%s",
                              "vote": 5
                            }
                          ]
                        }
                        """.formatted(c1.id(), c2.id()))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

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
                                "id": "2073b3b2-60f4-488c-8a0a-ab7121ed850c",
                                "slug": "apibara",
                                "name": "Apibara",
                                "logoUrl": null,
                                "shortDescription": "Listen to starknet events using gRPC and build your own node",
                                "visibility": null
                              },
                              "score": null
                            },
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                                "shortDescription": "A project for people who love fruits",
                                "visibility": null
                              },
                              "score": 4.0
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(20)
    void should_not_vote_when_not_a_jury() {
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();

        client.put()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + hayden.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "vote": 2
                            }
                          ]
                        }
                        """.formatted(c1.id()))
                // Then
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
