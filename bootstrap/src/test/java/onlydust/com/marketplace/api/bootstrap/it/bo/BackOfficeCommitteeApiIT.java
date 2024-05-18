package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.CommitteePageResponse;
import onlydust.com.backoffice.api.contract.model.CommitteeResponse;
import onlydust.com.backoffice.api.contract.model.CreateCommitteeRequest;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;

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
                                .status(CommitteeEntity.Status.OPEN_TO_APPLICATIONS)
                                .startDate(faker.date().past(5, TimeUnit.DAYS))
                                .endDate(faker.date().future(5, TimeUnit.DAYS))
                                .build(), CommitteeEntity.builder()
                                .id(UUID.randomUUID())
                                .name(faker.gameOfThrones().character())
                                .status(CommitteeEntity.Status.OPEN_TO_APPLICATIONS)
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
}
