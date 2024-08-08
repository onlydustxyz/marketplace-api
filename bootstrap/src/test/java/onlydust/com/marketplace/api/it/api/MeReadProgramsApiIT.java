package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.api.contract.model.ProgramsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@TagMe
public class MeReadProgramsApiIT extends AbstractMarketplaceApiIT {
    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.create();
    }

    @Test
    void should_get_my_programs_with_no_result() {
        // When
        client.get()
                .uri(getApiURI(ME_PROGRAMS))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "programs": []
                        }
                        """);
    }

    @Test
    void should_get_my_programs() {
        // Given
        final var programs = IntStream.range(0, 13).mapToObj(i -> programHelper.create(caller)).collect(toSet());

        // When
        final var response = client.get()
                .uri(getApiURI(ME_PROGRAMS))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                .exchange()
                // Then
                .expectStatus()
                .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                .expectBody(ProgramsPageResponse.class)
                .returnResult().getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getTotalItemNumber()).isEqualTo(13);
        assertThat(response.getTotalPageNumber()).isEqualTo(3);
        assertThat(response.getHasMore()).isTrue();
        assertThat(response.getNextPageIndex()).isEqualTo(1);
        assertThat(response.getPrograms()).hasSize(5);
        assertThat(response.getPrograms().get(0).getName().compareTo(response.getPrograms().get(4).getName())).isLessThan(0);
        assertThat(response.getPrograms()).allMatch(p -> programs.contains(SponsorId.of(p.getId())));
    }
}
