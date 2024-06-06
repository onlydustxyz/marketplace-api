package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.bootstrap.suites.tags.TagBO;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;
    private final UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");
    private final UUID olivierId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");
    private final UUID pacoId = UUID.fromString("f20e6812-8de8-432b-9c31-2920434fe7d0");
    private final UUID cocaColax = UUID.fromString("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
    static UUID committeeId;
    static UUID projectQuestionId1;
    static String projectQuestion1;
    private final UUID pierreAppId = UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0");


    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_MARKETING_ADMIN));
    }

    @Test
    @Order(1)
    void should_create_committee() {
        // Given
        final CreateCommitteeRequest createCommitteeRequest = new CreateCommitteeRequest();
        createCommitteeRequest.setName(faker.rickAndMorty().character());
        createCommitteeRequest.setApplicationStartDate(faker.date().past(2, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));
        createCommitteeRequest.setApplicationEndDate(faker.date().future(3, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));

        // When
        final CommitteeResponse committeeResponse = client.post()
                .uri(getApiURI(COMMITTEES))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body((BodyInserters.fromValue(createCommitteeRequest)))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteeResponse.class)
                .returnResult().getResponseBody();

        assertEquals(createCommitteeRequest.getName(), committeeResponse.getName());
        assertEquals(createCommitteeRequest.getApplicationEndDate().toInstant(), committeeResponse.getApplicationEndDate().toInstant());
        assertEquals(createCommitteeRequest.getApplicationStartDate().toInstant(), committeeResponse.getApplicationStartDate().toInstant());
        assertNotNull(committeeResponse.getId());
    }

    @Autowired
    CommitteeRepository committeeRepository;

    @Test
    @Order(2)
    void should_get_committees() {
        // Given
        committeeRepository.saveAll(
                List.of(
                        CommitteeEntity.builder()
                                .id(UUID.randomUUID())
                                .name(faker.gameOfThrones().character())
                                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                                .applicationStartDate(faker.date().past(5, TimeUnit.DAYS))
                                .applicationEndDate(faker.date().future(5, TimeUnit.DAYS))
                                .build(),
                        CommitteeEntity.builder()
                                .id(UUID.randomUUID())
                                .name(faker.gameOfThrones().character())
                                .status(Committee.Status.OPEN_TO_APPLICATIONS)
                                .applicationStartDate(faker.date().past(5, TimeUnit.DAYS))
                                .applicationEndDate(faker.date().future(5, TimeUnit.DAYS))
                                .build())
        );
        final List<CommitteeEntity> allCommittees = committeeRepository.findAll(Sort.by(Sort.Direction.DESC, "techCreatedAt"));

        // When
        final CommitteePageResponse committeePageResponse1 = client.get()
                .uri(getApiURI(COMMITTEES, Map.of("pageIndex", "0", "pageSize", "2")))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteePageResponse.class)
                .returnResult().getResponseBody();

        assertEquals(2, committeePageResponse1.getTotalPageNumber());
        assertEquals(1, committeePageResponse1.getNextPageIndex());
        assertEquals(3, committeePageResponse1.getTotalItemNumber());
        assertEquals(true, committeePageResponse1.getHasMore());
        assertEquals(allCommittees.get(0).getId(), committeePageResponse1.getCommittees().get(0).getId());
        assertEquals(allCommittees.get(1).getId(), committeePageResponse1.getCommittees().get(1).getId());

        final CommitteePageResponse committeePageResponse2 = client.get()
                .uri(getApiURI(COMMITTEES, Map.of("pageIndex", "1", "pageSize", "2")))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteePageResponse.class)
                .returnResult().getResponseBody();

        assertEquals(2, committeePageResponse2.getTotalPageNumber());
        assertEquals(1, committeePageResponse2.getNextPageIndex());
        assertEquals(3, committeePageResponse2.getTotalItemNumber());
        assertEquals(false, committeePageResponse2.getHasMore());
        assertEquals(allCommittees.get(2).getId(), committeePageResponse2.getCommittees().get(0).getId());
    }

    @Test
    @Order(3)
    void should_update_committee() {
        // Given
        committeeId = committeeRepository.findAll().get(0).getId();
        final UpdateCommitteeRequest updateCommitteeRequest1 = new UpdateCommitteeRequest();
        final ProjectQuestionRequest projectQuestionRequest1 = new ProjectQuestionRequest(faker.lorem().characters(), false);
        final ProjectQuestionRequest projectQuestionRequest2 = new ProjectQuestionRequest(faker.lorem().paragraph(), true);
        final ProjectQuestionRequest projectQuestionRequest3 = new ProjectQuestionRequest(faker.lorem().paragraph(), false);
        final JuryCriteriaRequest juryCriteriaRequest1 = new JuryCriteriaRequest(faker.lorem().characters());
        final JuryCriteriaRequest juryCriteriaRequest2 = new JuryCriteriaRequest(faker.lorem().paragraph());
        final JuryCriteriaRequest juryCriteriaRequest3 = new JuryCriteriaRequest(faker.pokemon().name());
        updateCommitteeRequest1.name(faker.rickAndMorty().location())
                .applicationStartDate(faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().future(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .status(CommitteeStatus.DRAFT)
                .votePerJury(3)
                .projectQuestions(
                        List.of(projectQuestionRequest1,
                                projectQuestionRequest2)
                )
                .juryCriteria(
                        List.of(
                                juryCriteriaRequest1,
                                juryCriteriaRequest2
                        )
                )
                .juryMemberIds(
                        List.of(anthoId, olivierId)
                );

        // When
        client.put()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body((BodyInserters.fromValue(updateCommitteeRequest1)))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(204);

        // When
        final CommitteeResponse committeeResponse1 = client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteeResponse.class)
                .returnResult().getResponseBody();

        assertEquals(2, committeeResponse1.getProjectQuestions().size());

        assertThat(committeeResponse1.getProjectQuestions().get(0).getId()).isNotNull();
        assertThat(committeeResponse1.getProjectQuestions().get(0).getQuestion()).isEqualTo(projectQuestionRequest1.getQuestion());
        assertThat(committeeResponse1.getProjectQuestions().get(0).getRequired()).isEqualTo(projectQuestionRequest1.getRequired());

        assertThat(committeeResponse1.getProjectQuestions().get(1).getId()).isNotNull();
        assertThat(committeeResponse1.getProjectQuestions().get(1).getQuestion()).isEqualTo(projectQuestionRequest2.getQuestion());
        assertThat(committeeResponse1.getProjectQuestions().get(1).getRequired()).isEqualTo(projectQuestionRequest2.getRequired());

        assertEquals(2, committeeResponse1.getJuryCriteria().size());

        assertThat(committeeResponse1.getJuryCriteria().get(0).getId()).isNotNull();
        assertThat(committeeResponse1.getJuryCriteria().get(0).getCriteria()).isEqualTo(juryCriteriaRequest1.getCriteria());

        assertThat(committeeResponse1.getJuryCriteria().get(1).getId()).isNotNull();
        assertThat(committeeResponse1.getJuryCriteria().get(1).getCriteria()).isEqualTo(juryCriteriaRequest2.getCriteria());

        assertEquals(updateCommitteeRequest1.getName(), committeeResponse1.getName());
        assertEquals(updateCommitteeRequest1.getApplicationStartDate().toInstant(), committeeResponse1.getApplicationStartDate().toInstant());
        assertEquals(updateCommitteeRequest1.getApplicationEndDate().toInstant(), committeeResponse1.getApplicationEndDate().toInstant());
        assertEquals(updateCommitteeRequest1.getStatus().name(), committeeResponse1.getStatus().name());
        assertNull(committeeResponse1.getSponsor());
        assertEquals(2, committeeResponse1.getJuries().size());
        assertTrue(committeeResponse1.getJuries().stream().map(UserLinkResponse::getUserId).toList().contains(olivierId));
        assertTrue(committeeResponse1.getJuries().stream().map(UserLinkResponse::getUserId).toList().contains(anthoId));


        final UpdateCommitteeRequest updateCommitteeRequest2 = new UpdateCommitteeRequest().name(faker.rickAndMorty().location())
                .applicationStartDate(faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .applicationEndDate(faker.date().future(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .status(CommitteeStatus.DRAFT)
                .votePerJury(5)
                .sponsorId(cocaColax)
                .projectQuestions(
                        List.of(projectQuestionRequest3,
                                projectQuestionRequest2)
                )
                .juryCriteria(
                        List.of(
                                juryCriteriaRequest1,
                                juryCriteriaRequest3
                        )
                )
                .juryMemberIds(
                        List.of(pacoId, olivierId)
                );


        // When
        client.put()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body((BodyInserters.fromValue(updateCommitteeRequest2)))
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(204);

        // When
        final CommitteeResponse committeeResponse2 = client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteeResponse.class)
                .returnResult().getResponseBody();

        assertEquals(2, committeeResponse1.getProjectQuestions().size());
        assertThat(committeeResponse2.getProjectQuestions()).allMatch(q -> q.getId() != null);
        assertEquals(projectQuestionRequest3.getQuestion(), committeeResponse2.getProjectQuestions().get(0).getQuestion());
        assertEquals(projectQuestionRequest3.getRequired(), committeeResponse2.getProjectQuestions().get(0).getRequired());
        assertEquals(projectQuestionRequest2.getQuestion(), committeeResponse2.getProjectQuestions().get(1).getQuestion());
        assertEquals(projectQuestionRequest2.getRequired(), committeeResponse2.getProjectQuestions().get(1).getRequired());

        assertEquals(2, committeeResponse1.getJuryCriteria().size());
        assertThat(committeeResponse2.getJuryCriteria()).allMatch(c -> c.getId() != null);
        assertEquals(juryCriteriaRequest1.getCriteria(), committeeResponse2.getJuryCriteria().get(0).getCriteria());
        assertEquals(juryCriteriaRequest3.getCriteria(), committeeResponse2.getJuryCriteria().get(1).getCriteria());

        assertEquals(updateCommitteeRequest2.getName(), committeeResponse2.getName());
        assertEquals(updateCommitteeRequest2.getApplicationStartDate().toInstant(), committeeResponse2.getApplicationStartDate().toInstant());
        assertEquals(updateCommitteeRequest2.getApplicationEndDate().toInstant(), committeeResponse2.getApplicationEndDate().toInstant());
        assertEquals(updateCommitteeRequest2.getStatus().name(), committeeResponse2.getStatus().name());
        assertEquals(cocaColax, committeeResponse2.getSponsor().getId());
        assertEquals("Coca Colax", committeeResponse2.getSponsor().getName());
        assertEquals("https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg", committeeResponse2.getSponsor().getAvatarUrl());
        projectQuestionId1 = committeeResponse2.getProjectQuestions().get(1).getId();
        projectQuestion1 = committeeResponse2.getProjectQuestions().get(1).getQuestion();
        assertTrue(committeeResponse2.getJuries().stream().map(UserLinkResponse::getUserId).toList().contains(olivierId));
        assertTrue(committeeResponse2.getJuries().stream().map(UserLinkResponse::getUserId).toList().contains(pacoId));
        assertEquals(2, committeeResponse2.getJuries().size());
    }

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @Test
    @Order(4)
    void should_update_committee_status() {
        // When
        client.patch()
                .uri(getApiURI(COMMITTEES_STATUS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                              "status": "OPEN_TO_APPLICATIONS"
                            }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);

        final CommitteeEntity updatedCommitteeEntity = committeeRepository.findById(committeeId).orElseThrow();
        assertEquals("OPEN_TO_APPLICATIONS", updatedCommitteeEntity.getStatus().name());
    }

    @Test
    @Order(5)
    void should_return_applications() {
        // Given
        projectLeadRepository.save(new ProjectLeadEntity(bretzel, pierreAppId));
        final Committee.ProjectAnswer projectAnswer = new Committee.ProjectAnswer(ProjectQuestion.Id.of(projectQuestionId1), faker.lorem().paragraph());
        committeeFacadePort.createUpdateApplicationForCommittee(Committee.Id.of(committeeId), new Committee.Application(pierreAppId, bretzel, List.of(
                projectAnswer
        )));

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "applications": [
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "userId": "fc92397c-3431-4a84-8054-845376b630a0",
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "score": null,
                              "allocation": null
                            }
                          ]
                        }
                        """)
                .jsonPath("$.projectQuestions.size()").isEqualTo(2);

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS_BY_IDS.formatted(committeeId, bretzel)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel",
                            "name": "Bretzel",
                            "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                          },
                          "allocation": null,
                          "projectQuestions": [
                            {
                              "question": "%s",
                              "required": true,
                              "answer": "%s"
                            }
                          ]
                        }
                        """.formatted(projectQuestion1, projectAnswer.answer()))
                .jsonPath("$.projectQuestions.size()").isEqualTo(1);
    }

    @Test
    @Order(6)
    void should_assign_juries_to_projects() {
        // Given

        // When
        client.patch()
                .uri(getApiURI(COMMITTEES_STATUS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                            {
                              "status": "OPEN_TO_VOTES"
                            }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .consumeWith(System.out::println)
                .json("""
                        {
                          "applications": [
                            {
                              "project": {
                                "id": "7d04163c-4187-4313-8066-61504d34fc56",
                                "slug": "bretzel",
                                "name": "Bretzel",
                                "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                              },
                              "applicant": {
                                "githubUserId": 16590657,
                                "userId": "fc92397c-3431-4a84-8054-845376b630a0",
                                "login": "PierreOucif",
                                "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4"
                              },
                              "score": null,
                              "allocation": null
                            }
                          ]
                        }
                        """)
                .jsonPath("$.projectQuestions.size()").isEqualTo(2)
                .jsonPath("$.juryAssignments").isNotEmpty();
    }

    @Test
    @Order(7)
    void should_return_application_with_jury_votes() {
        // When
        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS_BY_IDS.formatted(committeeId, bretzel)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "project": {
                            "id": "7d04163c-4187-4313-8066-61504d34fc56",
                            "slug": "bretzel",
                            "name": "Bretzel",
                            "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png"
                          },
                          "allocation": null
                        }
                        """)
                .jsonPath("$.projectQuestions.size()").isEqualTo(1)
                .jsonPath("$.juryVotes").isNotEmpty();
    }
}
