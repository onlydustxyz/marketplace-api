package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.bootstrap.helper.CurrencyHelper;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
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

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;
    private UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");
    private UUID haydenId = UUID.fromString("eaa1ddf3-fea5-4cef-825b-336f8e775e05");
    private UUID mehdiId = UUID.fromString("705e134d-e9e3-4ea3-85e2-a59a9628ecfc");
    private UUID cocaColax = UUID.fromString("44c6807c-48d1-4987-a0a6-ac63f958bdae");
    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
    static UUID committeeId;
    static UUID projectQuestionId1;
    static UUID projectQuestionId2;
    private UUID pierreAppId = UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0");


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
        createCommitteeRequest.setStartDate(faker.date().past(2, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));
        createCommitteeRequest.setEndDate(faker.date().future(3, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));

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
        assertEquals(createCommitteeRequest.getEndDate().toInstant(), committeeResponse.getEndDate().toInstant());
        assertEquals(createCommitteeRequest.getStartDate().toInstant(), committeeResponse.getStartDate().toInstant());
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
                                .status(CommitteeStatusEntity.OPEN_TO_APPLICATIONS)
                                .startDate(faker.date().past(5, TimeUnit.DAYS))
                                .endDate(faker.date().future(5, TimeUnit.DAYS))
                                .build(), CommitteeEntity.builder()
                                .id(UUID.randomUUID())
                                .name(faker.gameOfThrones().character())
                                .status(CommitteeStatusEntity.OPEN_TO_APPLICATIONS)
                                .startDate(faker.date().past(5, TimeUnit.DAYS))
                                .endDate(faker.date().future(5, TimeUnit.DAYS))
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
        final ProjectQuestionRequest projectQuestionRequest1 = new ProjectQuestionRequest("a" + faker.lorem().characters(), false);
        final ProjectQuestionRequest projectQuestionRequest2 = new ProjectQuestionRequest("b" + faker.lorem().paragraph(), true);
        final ProjectQuestionRequest projectQuestionRequest3 = new ProjectQuestionRequest("c" + faker.lorem().paragraph(), false);
        final JuryCriteriaRequest juryCriteriaRequest1 = new JuryCriteriaRequest(faker.lorem().characters());
        final JuryCriteriaRequest juryCriteriaRequest2 = new JuryCriteriaRequest(faker.lorem().paragraph());
        final JuryCriteriaRequest juryCriteriaRequest3 = new JuryCriteriaRequest(faker.pokemon().name());
        updateCommitteeRequest1.name(faker.rickAndMorty().location())
                .startDate(faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().future(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .allocationCurrencyId(CurrencyHelper.STRK.value())
                .status(CommitteeStatus.DRAFT)
                .maximumAllocationAmount(BigDecimal.valueOf(111.212))
                .minimumAllocationAmount(BigDecimal.valueOf(95.47))
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
                        List.of(anthoId, haydenId)
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
        for (ProjectQuestionResponse projectQuestion : committeeResponse1.getProjectQuestions()) {
            assertTrue(List.of(projectQuestionRequest1.getQuestion(), projectQuestionRequest2.getQuestion()).contains(projectQuestion.getQuestion()));
        }
        assertEquals(updateCommitteeRequest1.getName(), committeeResponse1.getName());
        assertEquals(updateCommitteeRequest1.getStartDate().toInstant(), committeeResponse1.getStartDate().toInstant());
        assertEquals(updateCommitteeRequest1.getEndDate().toInstant(), committeeResponse1.getEndDate().toInstant());
        assertEquals(updateCommitteeRequest1.getStatus().name(), committeeResponse1.getStatus().name());
        assertNull(committeeResponse1.getSponsor());


        final UpdateCommitteeRequest updateCommitteeRequest2 = new UpdateCommitteeRequest().name(faker.rickAndMorty().location())
                .startDate(faker.date().past(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .endDate(faker.date().future(10, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()))
                .allocationCurrencyId(CurrencyHelper.STRK.value())
                .status(CommitteeStatus.DRAFT)
                .maximumAllocationAmount(BigDecimal.valueOf(222.33))
                .minimumAllocationAmount(BigDecimal.valueOf(47.95))
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
                        List.of(mehdiId, haydenId)
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
        assertEquals(projectQuestionRequest2.getQuestion(), committeeResponse2.getProjectQuestions().get(0).getQuestion());
        assertEquals(projectQuestionRequest2.getRequired(), committeeResponse2.getProjectQuestions().get(0).getRequired());
        assertEquals(projectQuestionRequest3.getQuestion(), committeeResponse2.getProjectQuestions().get(1).getQuestion());
        assertEquals(projectQuestionRequest3.getRequired(), committeeResponse2.getProjectQuestions().get(1).getRequired());
        assertEquals(updateCommitteeRequest2.getName(), committeeResponse2.getName());
        assertEquals(updateCommitteeRequest2.getStartDate().toInstant(), committeeResponse2.getStartDate().toInstant());
        assertEquals(updateCommitteeRequest2.getEndDate().toInstant(), committeeResponse2.getEndDate().toInstant());
        assertEquals(updateCommitteeRequest2.getStatus().name(), committeeResponse2.getStatus().name());
        assertEquals(cocaColax, committeeResponse2.getSponsor().getId());
        assertEquals("Coca Colax", committeeResponse2.getSponsor().getName());
        assertEquals("https://onlydust-app-images.s3.eu-west-1.amazonaws.com/10299112926576087945.jpg", committeeResponse2.getSponsor().getAvatarUrl());
        projectQuestionId1 = committeeResponse2.getProjectQuestions().get(0).getId();
    }

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @Test
    @Order(4)
    void should_return_applications() {
        // Given
        final UUID committeeId = committeeRepository.findAll().get(0).getId();
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
                .json("{}");
    }

    @Test
    @Order(5)
    void should_update_committee_status() {
        // Given
        final CommitteeEntity committeeEntity = committeeRepository.findAll().get(1);

        // When
        client.patch()
                .uri(getApiURI(COMMITTEES_STATUS.formatted(committeeEntity.getId())))
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

        final CommitteeEntity updatedCommitteeEntity = committeeRepository.findById(committeeEntity.getId()).orElseThrow();
        assertEquals("OPEN_TO_VOTES", updatedCommitteeEntity.getStatus().name());
    }
}
