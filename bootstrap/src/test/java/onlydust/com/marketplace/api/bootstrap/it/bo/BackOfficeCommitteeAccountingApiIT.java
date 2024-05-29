package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.backoffice.api.contract.model.CommitteeBudgetAllocationsCreateRequest;
import onlydust.com.backoffice.api.contract.model.CommitteeBudgetAllocationsUpdateRequest;
import onlydust.com.backoffice.api.contract.model.CommitteeProjectAllocationRequest;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.Committee.ProjectAnswer;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;
    // Users
    private static final UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");
    private static final UUID olivierId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");
    private static final UUID pacoId = UUID.fromString("f20e6812-8de8-432b-9c31-2920434fe7d0");

    // Projects
    private static final UUID apibaraId = UUID.fromString("2073b3b2-60f4-488c-8a0a-ab7121ed850c");
    private static final UUID bretzelId = UUID.fromString("247ac542-762d-44cb-b8d4-4d6199c916be");
    private static final UUID cairoFoundryId = UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4");
    private static final UUID delugeId = UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371");
    private static final UUID starklingsId = UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c");

    // Sponsors
    private static final UUID cocaColax = UUID.fromString("44c6807c-48d1-4987-a0a6-ac63f958bdae");

    // Currencies
    private static final UUID STRK = UUID.fromString("81b7e948-954f-4718-bad3-b70a0edd27e1");

    private static final Faker faker = new Faker();
    private UUID committeeId;


    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    CommitteeJuryVoteRepository committeeJuryVoteRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_MARKETING_ADMIN));

        committeeId = fakeCommittee().id().value();
    }

    @Test
    void should_add_budget_to_committee() {
        final var request = new CommitteeBudgetAllocationsCreateRequest()
                .amount(BigDecimal.valueOf(10000))
                .currencyId(STRK);

        client.post()
                .uri(getApiURI(COMMITTEE_BUDGET_ALLOCATIONS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .body(fromValue(request))
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(COMMITTEE_BUDGET_ALLOCATIONS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "currency": {
                            "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                            "code": "STRK",
                            "name": "StarkNet Token",
                            "logoUrl": null,
                            "decimals": 18
                          },
                          "totalAllocationAmount": 10000.00000,
                          "projectAllocations": [
                            {
                              "project": {
                                "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "slug": "cairo-foundry",
                                "name": "Cairo foundry",
                                "logoUrl": null
                              },
                              "score": null,
                              "allocation": {
                                "amount": 1111.11111,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "id": "ade75c25-b39f-4fdf-a03a-e2391c1bc371",
                                "slug": "deluge",
                                "name": "Deluge",
                                "logoUrl": null
                              },
                              "score": null,
                              "allocation": {
                                "amount": 3333.33333,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "score": null,
                              "allocation": {
                                "amount": 5555.55556,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            }
                          ]
                        }
                        """);

        client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "applications": [
                            {
                              "project": {
                                "slug": "cairo-foundry"
                              },
                              "score": 3.0,
                              "allocation": {
                                "amount": 1111.11111,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "slug": "apibara"
                              },
                              "score": 1.0,
                              "allocation": null
                            },
                            {
                              "project": {
                                "slug": "deluge"
                              },
                              "score": 4.0,
                              "allocation": {
                                "amount": 3333.33333,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "slug": "bretzel-196"
                              },
                              "score": 2.0,
                              "allocation": null
                            },
                            {
                              "project": {
                                "slug": "starklings"
                              },
                              "score": 5.0,
                              "allocation": {
                                "amount": 5555.55556,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            }
                          ],
                          "allocation": {
                            "amount": 10000.00000,
                            "currency": {
                              "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                              "code": "STRK",
                              "name": "StarkNet Token",
                              "logoUrl": null,
                              "decimals": 18
                            }
                          }
                        }
                        """);

        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS_BY_IDS.formatted(committeeId, starklingsId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalScore": 5.0,
                          "allocation": null
                        }
                        """);
    }

    @Test
    void should_override_budget_allocations() {
        final var request = new CommitteeBudgetAllocationsUpdateRequest()
                .currencyId(STRK)
                .allocations(List.of(
                        new CommitteeProjectAllocationRequest()
                                .projectId(cairoFoundryId)
                                .amount(BigDecimal.valueOf(1000)),
                        new CommitteeProjectAllocationRequest()
                                .projectId(delugeId)
                                .amount(BigDecimal.valueOf(2000)),
                        new CommitteeProjectAllocationRequest()
                                .projectId(starklingsId)
                                .amount(BigDecimal.valueOf(3000))
                ));

        client.put()
                .uri(getApiURI(COMMITTEE_BUDGET_ALLOCATIONS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .body(fromValue(request))
                .exchange()
                // Then
                .expectStatus()
                .isNoContent();

        client.get()
                .uri(getApiURI(COMMITTEE_BUDGET_ALLOCATIONS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "currency": {
                            "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                            "code": "STRK",
                            "name": "StarkNet Token",
                            "logoUrl": null,
                            "decimals": 18
                          },
                          "totalAllocationAmount": 6000,
                          "projectAllocations": [
                            {
                              "project": {
                                "id": "8156fc5f-cec5-4f70-a0de-c368772edcd4",
                                "slug": "cairo-foundry",
                                "name": "Cairo foundry",
                                "logoUrl": null
                              },
                              "score": null,
                              "allocation": {
                                "amount": 1000,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "id": "ade75c25-b39f-4fdf-a03a-e2391c1bc371",
                                "slug": "deluge",
                                "name": "Deluge",
                                "logoUrl": null
                              },
                              "score": null,
                              "allocation": {
                                "amount": 2000,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            },
                            {
                              "project": {
                                "id": "6239cb20-eece-466a-80a0-742c1071dd3c",
                                "slug": "starklings",
                                "name": "Starklings",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/13746458086965388437.jpg"
                              },
                              "score": null,
                              "allocation": {
                                "amount": 3000,
                                "currency": {
                                  "id": "81b7e948-954f-4718-bad3-b70a0edd27e1",
                                  "code": "STRK",
                                  "name": "StarkNet Token",
                                  "logoUrl": null,
                                  "decimals": 18
                                }
                              }
                            }
                          ]
                        }
                        """);
    }


    private Committee fakeCommittee() {
        var committee = committeeFacadePort.createCommittee("My committee", ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        committee = committee.toBuilder()
                .sponsorId(cocaColax)
                .projectQuestions(IntStream.range(1, 10).mapToObj(i -> fakeProjectQuestion()).toList())
                .juryIds(List.of(anthoId, olivierId, pacoId))
                .juryCriteria(IntStream.range(1, 10).mapToObj(i1 -> fakeJuryCriteria()).toList())
                .votePerJury(2)
                .build();

        committeeFacadePort.update(committee);

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_APPLICATIONS);
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, apibaraId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, bretzelId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, cairoFoundryId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, delugeId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, starklingsId));

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_VOTES);
        vote(committee, apibaraId, 1);
        vote(committee, bretzelId, 2);
        vote(committee, cairoFoundryId, 3);
        vote(committee, delugeId, 4);
        vote(committee, starklingsId, 5);

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.CLOSED);

        return committee;
    }

    @NonNull
    private void vote(Committee committee, final UUID projectId, int score) {
        final var votes = committeeJuryVoteRepository.findAllByCommitteeIdAndProjectId(committee.id().value(), projectId);
        votes.forEach(v -> v.setScore(score));
        committeeJuryVoteRepository.saveAll(votes);
    }

    @NonNull
    private Committee.Application fakeApplication(Committee committee, UUID projectId) {
        final var projectLead = projectLeadRepository.findAll().stream().filter(pl -> pl.getProjectId().equals(projectId)).findFirst().orElseThrow();
        return new Committee.Application(projectLead.getUserId(),
                projectId,
                committee.projectQuestions().stream().map(q -> fakeProjectAnswer(q.id())).toList());
    }

    private ProjectQuestion fakeProjectQuestion() {
        return new ProjectQuestion(faker.lorem().paragraph(), faker.bool().bool());
    }

    private JuryCriteria fakeJuryCriteria() {
        return new JuryCriteria(faker.lorem().paragraph());
    }

    private ProjectAnswer fakeProjectAnswer(@NonNull ProjectQuestion.Id projectQuestionId) {
        return new ProjectAnswer(projectQuestionId, faker.lorem().paragraph());
    }
}
