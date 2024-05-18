package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.backoffice.api.contract.model.CommitteeResponse;
import onlydust.com.backoffice.api.contract.model.CreateCommitteeRequest;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeApiIT extends AbstractMarketplaceBackOfficeApiIT {

    @Test
    @Order(1)
    void should_create_committee() {
        // Given
        final var pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_MARKETING_ADMIN));
        final CreateCommitteeRequest createCommitteeRequest = new CreateCommitteeRequest();
        createCommitteeRequest.setName(faker.rickAndMorty().character());
        createCommitteeRequest.setStartDate(faker.date().past(2, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));
        createCommitteeRequest.setEndDate(faker.date().future(3, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()));

        // When
        final CommitteeResponse committeeResponse = client.post()
                .uri(getApiURI(POST_COMMITTEES))
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
}
