package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.contract.model.ProgramsPageResponse;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Set;
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

    @Nested
    class GivenNoPrograms {
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
    }

    @Nested
    class GivenMyProgram {
        Sponsor program;
        Set<Sponsor> programs;

        @BeforeEach
        void setUp() {
            programs = IntStream.range(0, 13).mapToObj(i -> programHelper.create(caller)).collect(toSet());
            program = programs.iterator().next();
        }

        @Test
        void should_get_my_programs() {
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
            assertThat(response.getPrograms()).allMatch(p -> programs.stream().anyMatch(p1 -> p1.id().equals(p.getId()) && p1.name().equals(p.getName())));
        }

        @Test
        void should_get_program_by_id() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(program.id().toString())
                    .jsonPath("$.name").isEqualTo(program.name())
                    .jsonPath("$.totalAvailable.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalAvailable.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalGranted.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalGranted.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalRewarded.totalUsdEquivalent").doesNotExist()
                    .jsonPath("$.totalRewarded.totalPerCurrency").isEmpty()
            ;
        }
    }

    @Nested
    class GivenNotMyProgram {
        Sponsor program;

        @BeforeEach
        void setUp() {
            program = programHelper.create();
        }

        @Test
        void should_be_unauthorized_getting_program_by_id() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isUnauthorized();
        }
    }
}
