package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagAccounting;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.model.Program;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.MutableObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Month;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static onlydust.com.marketplace.api.helper.CurrencyHelper.*;
import static onlydust.com.marketplace.api.helper.DateHelper.at;
import static onlydust.com.marketplace.api.helper.JSONPathAssertion.jsonObjectEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.BodyInserters.fromResource;

@TagAccounting
public class ProgramsApiIT extends AbstractMarketplaceApiIT {
    @Autowired
    ProjectFacadePort projectFacadePort;

    @Autowired
    ImageStoragePort imageStoragePort;

    UserAuthHelper.AuthenticatedUser caller;

    @BeforeEach
    void setUp() {
        caller = userAuthHelper.authenticateAntho();
    }

    @Nested
    class GivenNoProgram {
        @Test
        void should_not_be_able_to_create_program_when_not_sponsor_lead() {
            final var programLead = userAuthHelper.create();

            client.post()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"))))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.create().jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Awesome program",
                              "leadIds": ["%s"]
                            }
                            """.formatted(programLead.user().getId()))
                    .exchange()
                    .expectStatus()
                    .isForbidden();
        }

        @Test
        void should_not_create_program_with_unexisting_leads() {
            // Given
            addSponsorFor(caller, UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"));

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"))))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Awesome program",
                              "leadIds": ["%s"]
                            }
                            """.formatted(UUID.randomUUID()))
                    .exchange()
                    .expectStatus()
                    .is5xxServerError();
        }

        @Test
        void should_create_program_with_required_fields_only() {
            // Given
            addSponsorFor(caller, UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"));
            final var programLead = userAuthHelper.create();

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"))))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Awesome program",
                              "leadIds": ["%s"]
                            }
                            """.formatted(programLead.user().getId()))
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty();
        }

        @Test
        void should_create_program_with_optional_fields() {
            // Given
            addSponsorFor(caller, UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"));
            final var programLead = userAuthHelper.create();
            final var programId = new MutableObject<String>();

            // When
            client.post()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"))))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Foo program",
                              "url": "https://foo.bar",
                              "logoUrl": "https://foo.bar/logo.png",
                              "leadIds": ["%s"]
                            }
                            """.formatted(programLead.user().getId()))
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.id").value(programId::setValue);

            // Then
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(programId.getValue())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + programLead.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(programId.getValue())
                    .jsonPath("$.name").isEqualTo("Foo program")
                    .jsonPath("$.url").isEqualTo("https://foo.bar")
                    .jsonPath("$.logoUrl").isEqualTo("https://foo.bar/logo.png");
        }

        @Test
        void should_update_program() {
            // Given
            addSponsorFor(caller, UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"));
            final var programLead1 = userAuthHelper.create();
            final var programLead2 = userAuthHelper.create();
            final var programLead3 = userAuthHelper.create();
            final var programId = new MutableObject<String>();

            client.post()
                    .uri(getApiURI(SPONSOR_PROGRAMS.formatted(UUID.fromString("58a0a05c-c81e-447c-910f-629817a987b8"))))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Foo program",
                              "url": "https://foo.bar",
                              "logoUrl": "https://foo.bar/logo.png",
                              "leadIds": ["%s"]
                            }
                            """.formatted(programLead1.user().getId()))
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.id").value(programId::setValue);

            // When
            client.put()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(programId.getValue())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "name": "Updated program",
                              "url": "https://updated.bar",
                              "logoUrl": "https://updated.bar/logo.png",
                              "leadIds": ["%s", "%s"]
                            }
                            """.formatted(programLead2.user().getId(), programLead3.user().getId()))
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful();

            // Then
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(programId.getValue())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + programLead2.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(programId.getValue())
                    .jsonPath("$.name").isEqualTo("Updated program")
                    .jsonPath("$.url").isEqualTo("https://updated.bar")
                    .jsonPath("$.logoUrl").isEqualTo("https://updated.bar/logo.png");
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(programId.getValue())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + programLead3.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk();
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(programId.getValue())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + programLead1.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }

    @Nested
    class GivenMyProgram {
        private static Sponsor sponsor;
        private static Program program;
        UserAuthHelper.AuthenticatedUser sponsorLead;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        synchronized void setUp() {
            sponsorLead = userAuthHelper.authenticateOlivier();

            if (setupDone.compareAndExchange(false, true)) return;

            sponsor = sponsorHelper.create(sponsorLead);
            program = programHelper.create(sponsor.id(), caller);
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
                    .jsonPath("$.leads.length()").isEqualTo(1)
                    .jsonPath("$.leads[0].id").isEqualTo(caller.user().getId().toString())
                    .jsonPath("$.totalAvailable.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalAvailable.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalGranted.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalGranted.totalPerCurrency").isEmpty()
                    .jsonPath("$.totalRewarded.totalUsdEquivalent").isEqualTo(0)
                    .jsonPath("$.totalRewarded.totalPerCurrency").isEmpty()
                    .jsonPath("$.projectCount").isEqualTo(0)
                    .jsonPath("$.contributorCount").isEqualTo(0)
                    .jsonPath("$.rewardCount").isEqualTo(0)
            ;

            // When
            client.get()
                    .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + sponsorLead.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
            ;
        }

        @Test
        void should_get_program_monthly_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .json("""
                            {
                              "stats": []
                            }
                            """);
        }

        @Test
        void should_get_program_transactions() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id())))
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
                              "transactions": []
                            }
                            """);
        }

        @Test
        void should_get_program_transactions_in_csv() {
            // When
            final var csv = client.get()
                    .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .header(HttpHeaders.ACCEPT, "text/csv")
                    .exchange()
                    // Then
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .returnResult().getResponseBody();

            final var lines = new String(csv).split("\\R");
            assertThat(lines.length).isEqualTo(1);
            assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,project_id,sponsor_id,amount,currency,usd_amount");
        }

        @Test
        void should_get_program_projects() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id())))
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
                              "projects": []
                            }
                            """);
        }

        @Test
        void should_get_program_sponsors() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_SPONSORS.formatted(program.id())))
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
                              "sponsors": []
                            }
                            """);
        }

        @Test
        void should_upload_logo() throws MalformedURLException {
            when(imageStoragePort.storeImage(any(InputStream.class)))
                    .thenReturn(new URL("https://s3.amazon.com/logo.jpeg"));

            client.post()
                    .uri(getApiURI(PROGRAMS_LOGOS))
                    .header("Authorization", "Bearer " + caller.jwt())
                    .body(fromResource(new FileSystemResource(getClass().getResource("/images/logo-sample.jpeg").getFile())))
                    .exchange()
                    // Then
                    .expectStatus()
                    .is2xxSuccessful()
                    .expectBody()
                    .jsonPath("$.url").isEqualTo("https://s3.amazon.com/logo.jpeg");
        }

        @Nested
        class GivenSomeTransactions {
            private static Project project1;
            private static ProjectId project2Id;
            private static final AtomicBoolean setupDone = new AtomicBoolean();
            private static UserAuthHelper.AuthenticatedUser projectLead;

            @BeforeEach
            void setUp() {
                if (setupDone.compareAndExchange(false, true)) return;

                projectLead = userAuthHelper.create();
                final var project1Id = projectHelper.create(projectLead, "p1").getLeft();
                project1 = projectHelper.get(project1Id);
                project2Id = projectHelper.create(projectLead, "p2").getLeft();
                final var anotherProgram = programHelper.create(sponsor.id());
                final var recipient = userAuthHelper.create();
                final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());

                at("2024-01-01T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_200, USDC);
                    accountingHelper.allocate(sponsor.id(), program.id(), 2_200, USDC);
                });

                at("2024-01-15T00:00:00Z", () -> {
                    accountingHelper.unallocate(program.id(), sponsor.id(), 700, USDC);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 12, ETH);
                    accountingHelper.allocate(sponsor.id(), program.id(), 12, ETH);
                });

                at("2024-02-03T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 2_000, USDC);
                    accountingHelper.allocate(sponsor.id(), anotherProgram.id(), 1_500, USDC);
                });

                at("2024-03-12T00:00:00Z", () -> {
                    accountingHelper.createSponsorAccount(sponsor.id(), 1, BTC);
                    accountingHelper.allocate(sponsor.id(), anotherProgram.id(), 1, BTC);
                });

                at("2024-04-23T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), project1Id, 500, USDC);
                    accountingHelper.grant(program.id(), project1Id, 2, ETH);
                });

                at("2024-04-24T00:00:00Z", () -> accountingHelper.grant(program.id(), project2Id, 200, USDC));

                at("2024-05-23T00:00:00Z", () -> {
                    accountingHelper.grant(program.id(), project2Id, 3, ETH);
                    accountingHelper.grant(anotherProgram.id(), project1Id, 500, USDC);
                    accountingHelper.grant(anotherProgram.id(), project1Id, 1, BTC);
                });

                at("2024-06-23T00:00:00Z", () -> {
                    accountingHelper.grant(anotherProgram.id(), project2Id, 400, USDC);
                    accountingHelper.ungrant(project1Id, program.id(), 200, USDC);
                });

                final var reward1 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 400, USDC));
                final var reward2 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                final var reward3 = at("2024-07-11T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, BTC));

                final var reward4 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 100, USDC));
                final var reward5 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project2Id, projectLead, recipientId, 2, ETH));

                final var reward6 = at("2024-08-02T00:00:00Z", () -> rewardHelper.create(project1Id, projectLead, recipientId, 1, ETH));
                at("2024-08-03T00:00:00Z", () -> rewardHelper.cancel(project1Id, projectLead, reward6));

                at("2024-08-15T00:00:00Z", () -> accountingHelper.pay(reward1, reward2, reward3, reward4, reward5));

                projectFacadePort.refreshStats();
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
                        .jsonPath("$.leads.length()").isEqualTo(1)
                        .jsonPath("$.leads[0].id").isEqualTo(caller.user().getId().toString())
                        .jsonPath("$.totalAvailable.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .jsonPath("$.totalGranted.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .jsonPath("$.totalRewarded.totalPerCurrency[0].currency.code").isEqualTo("ETH")
                        .json("""
                                {
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 13483.89,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 7,
                                        "prettyAmount": 7,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 12473.89,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 93
                                      },
                                      {
                                        "amount": 1000,
                                        "prettyAmount": 1000,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 1010.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 7
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 9414.92,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 5,
                                        "prettyAmount": 5,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 8909.92,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 95
                                      },
                                      {
                                        "amount": 500,
                                        "prettyAmount": 500,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 505.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 5
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 5749.95,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 3,
                                        "prettyAmount": 3,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 5345.95,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 93
                                      },
                                      {
                                        "amount": 400,
                                        "prettyAmount": 400,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 404.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 7
                                      }
                                    ]
                                  },
                                  "projectCount": 2,
                                  "contributorCount": 1,
                                  "rewardCount": 4
                                }
                                """)
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[?(@.date == '2024-01-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-01-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 1515.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 1500,
                                            "prettyAmount": 1500,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 1515.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 100
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 0
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 2
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-02-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-02-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 21383.81,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 12,
                                            "prettyAmount": 12,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 21383.81,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 100
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 0
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-03-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-03-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-04-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-04-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 4270.97,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 2,
                                            "prettyAmount": 2,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 3563.97,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 83
                                          },
                                          {
                                            "amount": 700,
                                            "prettyAmount": 700,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 707.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 17
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 3
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-05-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-05-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 5345.95,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 3,
                                            "prettyAmount": 3,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 5345.95,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 100
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 0
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-06-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-06-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": -202.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 0
                                          },
                                          {
                                            "amount": -200,
                                            "prettyAmount": -200,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": -202.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 100
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 1
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-07-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-07-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                        .jsonPath("$.stats[?(@.date == '2024-08-01')]").value(jsonObjectEquals("""
                                {
                                      "date": "2024-08-01",
                                      "totalDeposited": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalRewarded": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "totalPaid": {
                                        "totalUsdEquivalent": 0.00,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": null
                                          },
                                          {
                                            "amount": 0,
                                            "prettyAmount": 0,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 0.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": null
                                          }
                                        ]
                                      },
                                      "transactionCount": 0
                                    }
                                """))
                ;

                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of("showEmpty", "false")))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[?(@.date == '2024-03-01')]").doesNotExist()
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats.size()").isEqualTo(3)
                        .jsonPath("$.stats[0].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(3)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(1)
                ;

                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01",
                                "sortDirection", "DESC"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats.size()").isEqualTo(3)
                        .jsonPath("$.stats[0].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(1)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(1)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(3)
                ;
            }

            @Test
            void should_get_program_monthly_transactions_stats_filtered_by_search() {
                final var search = project1.getName();

                // When
                client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "search", search
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.stats[0].date").isEqualTo("2024-04-01")
                        .jsonPath("$.stats[0].transactionCount").isEqualTo(2)
                        .jsonPath("$.stats[1].date").isEqualTo("2024-05-01")
                        .jsonPath("$.stats[1].transactionCount").isEqualTo(0)
                        .jsonPath("$.stats[2].date").isEqualTo("2024-06-01")
                        .jsonPath("$.stats[2].transactionCount").isEqualTo(1)
                ;
            }

            @ParameterizedTest
            @EnumSource(value = FinancialTransactionType.class, names = {"GRANTED", "UNGRANTED", "ALLOCATED", "UNALLOCATED"})
            void should_get_program_monthly_transactions_stats_filtered_by_types(FinancialTransactionType type) {
                // When
                final var stats = client.get()
                        .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id()), Map.of(
                                "types", type.name(),
                                "fromDate", "2023-12-01",
                                "toDate", "2024-12-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody(BiFinancialsStatsListResponse.class)
                        .returnResult().getResponseBody().getStats();

                switch (type) {
                    case GRANTED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(3);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    }
                    case UNGRANTED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.APRIL).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MAY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JUNE).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                    }
                    case ALLOCATED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    }
                    case UNALLOCATED -> {
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.JANUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(1);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.FEBRUARY).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                        assertThat(stats.stream().filter(s -> s.getDate().getMonth() == Month.MARCH).findFirst().orElseThrow().getTransactionCount()).isEqualTo(0);
                    }
                }
            }

            @Test
            void should_get_program_transactions() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isEqualTo(HttpStatus.PARTIAL_CONTENT)
                        .expectBody()
                        .jsonPath("$.transactions[0].thirdParty.project.id").isEqualTo(project1.getId().toString())
                        .jsonPath("$.transactions[1].thirdParty.project.id").isEqualTo(project2Id.toString())
                        .jsonPath("$.transactions[2].thirdParty.project.id").isEqualTo(project2Id.toString())
                        .jsonPath("$.transactions[3].thirdParty.project.id").isEqualTo(project1.getId().toString())
                        .jsonPath("$.transactions[4].thirdParty.project.id").isEqualTo(project1.getId().toString())
                        .json("""
                                {
                                  "totalPageNumber": 2,
                                  "totalItemNumber": 8,
                                  "hasMore": true,
                                  "nextPageIndex": 1,
                                  "transactions": [
                                    {
                                      "date": "2024-06-23T00:00:00Z",
                                      "type": "UNGRANTED",
                                      "amount": {
                                        "amount": 200,
                                        "prettyAmount": 200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 202.00,
                                        "usdConversionRate": 1.010001
                                      }
                                    },
                                    {
                                      "date": "2024-05-23T00:00:00Z",
                                      "type": "GRANTED",
                                      "amount": {
                                        "amount": 3,
                                        "prettyAmount": 3,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 5345.95,
                                        "usdConversionRate": 1781.983987
                                      }
                                    },
                                    {
                                      "date": "2024-04-24T00:00:00Z",
                                      "type": "GRANTED",
                                      "amount": {
                                        "amount": 200,
                                        "prettyAmount": 200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 202.00,
                                        "usdConversionRate": 1.010001
                                      }
                                    },
                                    {
                                      "date": "2024-04-23T00:00:00Z",
                                      "type": "GRANTED",
                                      "amount": {
                                        "amount": 2,
                                        "prettyAmount": 2,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 3563.97,
                                        "usdConversionRate": 1781.983987
                                      }
                                    },
                                    {
                                      "date": "2024-04-23T00:00:00Z",
                                      "type": "GRANTED",
                                      "amount": {
                                        "amount": 500,
                                        "prettyAmount": 500,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 505.00,
                                        "usdConversionRate": 1.010001
                                      }
                                    }
                                  ]
                                }
                                """);
            }

            @Test
            void should_get_program_transactions_in_csv() {
                // When
                final var csv = client.get()
                        .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .header(HttpHeaders.ACCEPT, "text/csv")
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody(String.class)
                        .returnResult().getResponseBody();

                final var lines = csv.split("\\R");
                assertThat(lines.length).isEqualTo(9);
                assertThat(lines[0]).isEqualTo("id,timestamp,transaction_type,project_id,sponsor_id,amount,currency,usd_amount");
            }

            @Test
            void should_get_program_transactions_filtered_by_date() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "fromDate", "2024-04-01",
                                "toDate", "2024-06-01"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(4)
                        .jsonPath("$.transactions[?(@.date < '2024-04-01')]").doesNotExist()
                        .jsonPath("$.transactions[?(@.date > '2024-06-01')]").doesNotExist();
            }

            @Test
            void should_get_program_transactions_filtered_by_search() {
                // Given
                final var search = project1.getName();

                // When
                client.get()
                        .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "search", search
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(3)
                        .jsonPath("$.transactions[?(@.thirdParty.project.id != '%s')]".formatted(project1.getId())).doesNotExist();
            }

            @ParameterizedTest
            @EnumSource(value = FinancialTransactionType.class, names = {"GRANTED", "UNGRANTED", "ALLOCATED", "UNALLOCATED"})
            void should_get_program_transactions_filtered_by_types(FinancialTransactionType type) {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id()), Map.of(
                                "types", type.name()
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.transactions.size()").isEqualTo(switch (type) {
                            case GRANTED -> 4;
                            case UNGRANTED -> 1;
                            case ALLOCATED -> 2;
                            case UNALLOCATED -> 1;
                            default -> throw new IllegalStateException();
                        })
                        .jsonPath("$.transactions[?(@.type != '%s')]".formatted(type.name())).doesNotExist();
            }

            @Test
            void should_get_program_projects() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5"
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.projects.size()").isEqualTo(2)
                        .jsonPath("$.projects[0].shortDescription").isNotEmpty()
                        .jsonPath("$.projects[0].categories").isArray()
                        .jsonPath("$.projects[0].languages").isArray()
                        .jsonPath("$.projects[1].shortDescription").isNotEmpty()
                        .jsonPath("$.projects[1].categories").isArray()
                        .jsonPath("$.projects[1].languages").isArray()
                        .json("""
                                {
                                   "projects": [
                                     {
                                       "totalAvailable": {
                                         "totalUsdEquivalent": 1882.98,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 1,
                                             "prettyAmount": 1,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 1781.98,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 95
                                           },
                                           {
                                             "amount": 100,
                                             "prettyAmount": 100,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 101.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 5
                                           }
                                         ]
                                       },
                                       "totalGranted": {
                                         "totalUsdEquivalent": 5547.95,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 3,
                                             "prettyAmount": 3,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 5345.95,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 96
                                           },
                                           {
                                             "amount": 200,
                                             "prettyAmount": 200,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 202.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 4
                                           }
                                         ]
                                       },
                                       "totalRewarded": {
                                         "totalUsdEquivalent": 3664.97,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 2,
                                             "prettyAmount": 2,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 3563.97,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 97
                                           },
                                           {
                                             "amount": 100,
                                             "prettyAmount": 100,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 101.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 3
                                           }
                                         ]
                                       },
                                       "percentUsedBudget": 66,
                                       "averageRewardUsdAmount": 1832.48,
                                       "mergedPrCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       },
                                       "newContributorsCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       },
                                       "activeContributorsCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       }
                                     },
                                     {
                                       "totalAvailable": {
                                         "totalUsdEquivalent": 1781.98,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 1,
                                             "prettyAmount": 1,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 1781.98,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 100
                                           },
                                           {
                                             "amount": 0,
                                             "prettyAmount": 0,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 0.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 0
                                           }
                                         ]
                                       },
                                       "totalGranted": {
                                         "totalUsdEquivalent": 3866.97,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 2,
                                             "prettyAmount": 2,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 3563.97,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 92
                                           },
                                           {
                                             "amount": 300,
                                             "prettyAmount": 300,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 303.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 8
                                           }
                                         ]
                                       },
                                       "totalRewarded": {
                                         "totalUsdEquivalent": 2084.98,
                                         "totalPerCurrency": [
                                           {
                                             "amount": 1,
                                             "prettyAmount": 1,
                                             "currency": {
                                               "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                               "code": "ETH",
                                               "name": "Ether",
                                               "logoUrl": null,
                                               "decimals": 18
                                             },
                                             "usdEquivalent": 1781.98,
                                             "usdConversionRate": 1781.983987,
                                             "ratio": 85
                                           },
                                           {
                                             "amount": 300,
                                             "prettyAmount": 300,
                                             "currency": {
                                               "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                               "code": "USDC",
                                               "name": "USD Coin",
                                               "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                               "decimals": 6
                                             },
                                             "usdEquivalent": 303.00,
                                             "usdConversionRate": 1.010001,
                                             "ratio": 15
                                           }
                                         ]
                                       },
                                       "percentUsedBudget": 54,
                                       "averageRewardUsdAmount": 1092.99,
                                       "mergedPrCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       },
                                       "newContributorsCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       },
                                       "activeContributorsCount": {
                                         "value": 0,
                                         "trend": "STABLE"
                                       }
                                     }
                                   ]
                                 }
                                """);

                // When
                client.get()
                        .uri(getApiURI(PROGRAM_PROJECT.formatted(program.id(), project2Id)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                                 {
                                  "totalAvailable": {
                                    "totalUsdEquivalent": 1882.98,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 1,
                                        "prettyAmount": 1,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 1781.98,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 95
                                      },
                                      {
                                        "amount": 100,
                                        "prettyAmount": 100,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 101.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 5
                                      }
                                    ]
                                  },
                                  "totalGranted": {
                                    "totalUsdEquivalent": 5547.95,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 3,
                                        "prettyAmount": 3,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 5345.95,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 96
                                      },
                                      {
                                        "amount": 200,
                                        "prettyAmount": 200,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 202.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 4
                                      }
                                    ]
                                  },
                                  "totalRewarded": {
                                    "totalUsdEquivalent": 3664.97,
                                    "totalPerCurrency": [
                                      {
                                        "amount": 2,
                                        "prettyAmount": 2,
                                        "currency": {
                                          "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                          "code": "ETH",
                                          "name": "Ether",
                                          "logoUrl": null,
                                          "decimals": 18
                                        },
                                        "usdEquivalent": 3563.97,
                                        "usdConversionRate": 1781.983987,
                                        "ratio": 97
                                      },
                                      {
                                        "amount": 100,
                                        "prettyAmount": 100,
                                        "currency": {
                                          "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                          "code": "USDC",
                                          "name": "USD Coin",
                                          "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                          "decimals": 6
                                        },
                                        "usdEquivalent": 101.00,
                                        "usdConversionRate": 1.010001,
                                        "ratio": 3
                                      }
                                    ]
                                  }
                                }""");
            }

            @Test
            void should_search_program_projects_by_name() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "search", project1.getName().substring(0, 5)
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.projects.size()").isEqualTo(1)
                        .jsonPath("$.projects[0].id").isEqualTo(project1.getId().toString())
                ;
            }

            @Test
            void should_get_program_sponsors() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_SPONSORS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.sponsors[0].id").isEqualTo(sponsor.id().toString())
                        .json("""
                                {
                                  "totalPageNumber": 1,
                                  "totalItemNumber": 1,
                                  "hasMore": false,
                                  "nextPageIndex": 0,
                                  "sponsors": [
                                    {
                                      "leads": [
                                        {
                                          "githubUserId": 595505,
                                          "login": "ofux",
                                          "avatarUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5494259449694867225.webp",
                                          "id": "e461c019-ba23-4671-9b6c-3a5a18748af9"
                                        }
                                      ],
                                      "totalAvailable": {
                                        "totalUsdEquivalent": 13483.89,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 7,
                                            "prettyAmount": 7,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 12473.89,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 93
                                          },
                                          {
                                            "amount": 1000,
                                            "prettyAmount": 1000,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 1010.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 7
                                          }
                                        ]
                                      },
                                      "totalGranted": {
                                        "totalUsdEquivalent": 9414.92,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 5,
                                            "prettyAmount": 5,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 8909.92,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 95
                                          },
                                          {
                                            "amount": 500,
                                            "prettyAmount": 500,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 505.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 5
                                          }
                                        ]
                                      },
                                      "totalAllocated": {
                                        "totalUsdEquivalent": 22898.81,
                                        "totalPerCurrency": [
                                          {
                                            "amount": 12,
                                            "prettyAmount": 12,
                                            "currency": {
                                              "id": "71bdfcf4-74ee-486b-8cfe-5d841dd93d5c",
                                              "code": "ETH",
                                              "name": "Ether",
                                              "logoUrl": null,
                                              "decimals": 18
                                            },
                                            "usdEquivalent": 21383.81,
                                            "usdConversionRate": 1781.983987,
                                            "ratio": 93
                                          },
                                          {
                                            "amount": 1500,
                                            "prettyAmount": 1500,
                                            "currency": {
                                              "id": "562bbf65-8a71-4d30-ad63-520c0d68ba27",
                                              "code": "USDC",
                                              "name": "USD Coin",
                                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                                              "decimals": 6
                                            },
                                            "usdEquivalent": 1515.00,
                                            "usdConversionRate": 1.010001,
                                            "ratio": 7
                                          }
                                        ]
                                      }
                                    }
                                  ]
                                }
                                """);
            }

            @Test
            void should_search_program_sponsors_by_name() {
                // When
                client.get()
                        .uri(getApiURI(PROGRAM_SPONSORS.formatted(program.id()), Map.of(
                                "pageIndex", "0",
                                "pageSize", "5",
                                "search", sponsor.name().substring(0, 5)
                        )))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .json("""
                                {
                                  "totalPageNumber": 1,
                                  "totalItemNumber": 1,
                                  "hasMore": false,
                                  "nextPageIndex": 0
                                }
                                """);
            }

            @Test
            void should_grant_a_project() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.grant(program.id(), projectId, 1, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROGRAM_GRANT.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .bodyValue(new GrantRequest()
                                .projectId(projectId.value())
                                .amount(BigDecimal.ONE)
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isNoContent();

                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount").isEqualTo(8)
                ;

                client.get()
                        .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.projects[?(@.id == '%s')].totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(2)
                        .jsonPath("$.projects[?(@.id == '%s')].totalGranted.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(2)
                ;
            }

            @Test
            void should_ungrant_a_project() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.grant(program.id(), projectId, 1, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROJECT_UNGRANT.formatted(projectId)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(projectLead).jwt())
                        .bodyValue(new UngrantRequest()
                                .programId(program.id().value())
                                .amount(BigDecimal.ONE)
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isNoContent();

                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount").isEqualTo(10)
                ;

                client.get()
                        .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.projects[?(@.id == '%s')].totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(0)
                        .jsonPath("$.projects[?(@.id == '%s')].totalGranted.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(0)
                ;
            }

            @Test
            void should_fail_to_ungrant_more_than_available() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                final var anotherProgram = programHelper.create(sponsor.id(), caller);
                final var recipient = userAuthHelper.create();
                final var recipientId = GithubUserId.of(recipient.user().getGithubUserId());
                accountingHelper.createSponsorAccount(sponsor.id(), 1000, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), anotherProgram.id(), 500, ETH);
                accountingHelper.grant(program.id(), projectId, 10, ETH);
                accountingHelper.grant(anotherProgram.id(), projectId, 500, ETH);
                rewardHelper.create(projectId, projectLead, recipientId, 5, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROJECT_UNGRANT.formatted(projectId)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(projectLead).jwt())
                        .bodyValue(new UngrantRequest()
                                .programId(program.id().value())
                                .amount(BigDecimal.valueOf(6))
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isBadRequest();

                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount").isEqualTo(0)
                ;

                client.get()
                        .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.projects[?(@.id == '%s')].totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(5)
                        .jsonPath("$.projects[?(@.id == '%s')].totalGranted.totalPerCurrency[?(@.currency.code == 'ETH')].amount".formatted(projectId)).isEqualTo(10)
                ;
            }

            @Test
            void should_forbid_non_leads_to_ungrant() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.grant(program.id(), projectId, 1, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROJECT_UNGRANT.formatted(projectId)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .bodyValue(new UngrantRequest()
                                .programId(program.id().value())
                                .amount(BigDecimal.valueOf(1))
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isForbidden();
            }

            @Test
            void should_unallocate_from_program() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.grant(program.id(), projectId, 1, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROGRAM_UNALLOCATE.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .bodyValue(new UnallocateRequest()
                                .sponsorId(sponsor.id().value())
                                .amount(BigDecimal.ONE)
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isNoContent();

                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount").isEqualTo(8)
                ;
            }

            @Test
            void should_fail_to_unallocate_more_than_available() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var anotherSponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 1000, ETH);
                accountingHelper.createSponsorAccount(anotherSponsor.id(), 1000, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.allocate(anotherSponsor.id(), program.id(), 500, ETH);
                accountingHelper.grant(program.id(), projectId, 5, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROGRAM_UNALLOCATE.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .bodyValue(new UnallocateRequest()
                                .sponsorId(sponsor.id().value())
                                .amount(BigDecimal.valueOf(6))
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isBadRequest();

                client.get()
                        .uri(getApiURI(PROGRAM_BY_ID.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(caller).jwt())
                        .exchange()
                        // Then
                        .expectStatus()
                        .isOk()
                        .expectBody()
                        .jsonPath("$.totalAvailable.totalPerCurrency[?(@.currency.code == 'ETH')].amount").isEqualTo(505)
                ;
            }

            @Test
            void should_forbid_non_program_lead_to_unallocate_from_program() {
                // Given
                final var projectLead = userAuthHelper.create();
                final var projectId = projectHelper.create(projectLead).getLeft();
                final var sponsor = sponsorHelper.create(sponsorLead);
                final var program = programHelper.create(sponsor.id(), caller);
                accountingHelper.createSponsorAccount(sponsor.id(), 10, ETH);
                accountingHelper.allocate(sponsor.id(), program.id(), 10, ETH);
                accountingHelper.grant(program.id(), projectId, 1, ETH);

                // When
                client.post()
                        .uri(getApiURI(PROGRAM_UNALLOCATE.formatted(program.id())))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.signInUser(sponsorLead).jwt())
                        .bodyValue(new UnallocateRequest()
                                .sponsorId(sponsor.id().value())
                                .amount(BigDecimal.ONE)
                                .currencyId(ETH.value()))
                        .exchange()
                        // Then
                        .expectStatus()
                        .isForbidden();
            }
        }
    }

    @Nested
    class GivenNotMyProgram {
        private static Program program;
        private static final AtomicBoolean setupDone = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            if (setupDone.compareAndExchange(false, true)) return;

            final var sponsor = sponsorHelper.create();
            program = programHelper.create(sponsor.id());
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
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_getting_program_transactions_stats() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_STATS_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_getting_program_transactions() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_TRANSACTIONS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_getting_program_projects() {
            // When
            client.get()
                    .uri(getApiURI(PROGRAM_PROJECTS.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }

        @Test
        void should_be_unauthorized_to_grant_project() {
            // Given
            final var projectId = projectHelper.create(caller, "p0").getLeft();

            // When
            client.post()
                    .uri(getApiURI(PROGRAM_GRANT.formatted(program.id())))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + caller.jwt())
                    .bodyValue(new GrantRequest()
                            .projectId(projectId.value())
                            .amount(BigDecimal.TEN)
                            .currencyId(ETH.value()))
                    .exchange()
                    // Then
                    .expectStatus()
                    .isForbidden();
        }
    }
}
