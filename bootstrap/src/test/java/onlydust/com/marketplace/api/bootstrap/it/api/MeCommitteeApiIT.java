package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagMe;
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

@TagMe
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
    static Committee.ProjectAnswer q1BretzelAnswer = new Committee.ProjectAnswer(q1.id(), faker.lorem().paragraph());

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

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
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
                q1BretzelAnswer
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

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "score": null,
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel",
                            "name": "Bretzel",
                            "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                            "projectLeads": [
                              {
                                "githubUserId": 16590657,
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                "id": "fc92397c-3431-4a84-8054-845376b630a0"
                              },
                              {
                                "githubUserId": 98735421,
                                "login": "pacovilletard",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/98735421?v=4",
                                "id": "f20e6812-8de8-432b-9c31-2920434fe7d0"
                              },
                              {
                                "githubUserId": 8642470,
                                "login": "gregcha",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/8642470?v=4",
                                "id": "45e98bf6-25c2-4edf-94da-e340daba8964"
                              }
                            ],
                            "longDescription": "[Bretzel](http://bretzel.club/) is your best chance to match with your secret crush      \\nEver liked someone but never dared to tell them?      \\n      \\n**Bretzel** is your chance to match with your secret crush      \\nAll you need is a LinkedIn profile.      \\n      \\n1. **Turn LinkedIn into a bretzel party:** Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.      \\n2. **Give your bretzels under the radar:** Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?      \\n3. **Ooh la la, it's a match!**  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!",
                            "shortDescription": "A project for people who love fruits",
                            "last3monthsMetrics": {
                              "activeContributors": 0,
                              "newContributors": 0,
                              "contributorsRewarded": 0,
                              "openIssues": 0,
                              "contributionsCompleted": 0,
                              "amountSentInUsd": 0
                            }
                          },
                          "answers": [
                            {
                              "id": "%s",
                              "question": "Q1",
                              "answer": "%s",
                              "required": false
                            }
                          ],
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "criteria": "c1",
                              "vote": null
                            },
                            {
                              "criteriaId": "%s",
                              "criteria": "c2",
                              "vote": null
                            }
                          ]
                        }
                        """.formatted(q1BretzelAnswer.projectQuestionId(), q1BretzelAnswer.answer(),
                        c1.id(), c2.id()));

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

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "score": 2.0,
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel"
                          },
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "criteria": "c1",
                              "vote": 2
                            },
                            {
                              "criteriaId": "%s",
                              "criteria": "c2",
                              "vote": null
                            }
                          ]
                        }
                        """.formatted(c1.id(), c2.id()));
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

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "score": 4.0,
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel"
                          },
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "criteria": "c1",
                              "vote": 3
                            },
                            {
                              "criteriaId": "%s",
                              "criteria": "c2",
                              "vote": 5
                            }
                          ]
                        }
                        """.formatted(c1.id(), c2.id()));
    }

    @Test
    @Order(12)
    void should_not_mix_votes_of_different_juries() {
        final UserAuthHelper.AuthenticatedUser antho = userAuthHelper.authenticateAnthony();

        client.put()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + antho.jwt())
                .contentType(APPLICATION_JSON)
                .bodyValue("""
                        {
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "vote": 1
                            },
                            {
                              "criteriaId": "%s",
                              "vote": 2
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
                .header("Authorization", "Bearer " + antho.jwt())
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
                              "score": 1.5
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_COMMITTEE_PROJECTS.formatted(committee.id(), bretzel)))
                .header("Authorization", "Bearer " + antho.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "score": 1.5,
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel"
                          },
                          "votes": [
                            {
                              "criteriaId": "%s",
                              "criteria": "c1",
                              "vote": 1
                            },
                            {
                              "criteriaId": "%s",
                              "criteria": "c2",
                              "vote": 2
                            }
                          ]
                        }
                        """.formatted(c1.id(), c2.id()));
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
