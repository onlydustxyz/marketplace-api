package onlydust.com.marketplace.api.bootstrap.it.api;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.contract.model.CommitteeApplicationRequest;
import onlydust.com.marketplace.api.contract.model.CommitteeApplicationResponse;
import onlydust.com.marketplace.api.contract.model.CommitteeProjectAnswerRequest;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectLeadEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommitteeApiIT extends AbstractMarketplaceApiIT {

    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    @Autowired
    CommitteeFacadePort committeeFacadePort;
    static Committee.Id committeeId;
    static ProjectQuestion.Id projectQuestionId1;
    static ProjectQuestion.Id projectQuestionId2;

    @Test
    @Order(1)
    void should_get_not_existing_application() {
        // Given
        Committee committee = committeeFacadePort.createCommittee("Mr. Needful",
                ZonedDateTime.parse("2024-05-19T02:58:44.399Z"),
                ZonedDateTime.parse("2024-05-25T20:06:27.482Z"));
        committeeId = committee.id();
        committee = committee.toBuilder().status(Committee.Status.DRAFT).build();
        final ProjectQuestion q1 = new ProjectQuestion("Q1", false);
        final ProjectQuestion q2 = new ProjectQuestion("Q2", true);
        projectQuestionId1 = q1.id();
        projectQuestionId2 = q2.id();
        committee.projectQuestions().addAll(
                List.of(
                        q1,
                        q2
                )
        );
        committeeFacadePort.update(committee);

        // Cannot get committee with status DRAFT
        client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                .exchange()
                .expectStatus()
                .isNotFound();

        committeeFacadePort.updateStatus(committeeId, Committee.Status.OPEN_TO_APPLICATIONS);
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS.formatted(committeeId)))
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();

        // When
        final CommitteeApplicationResponse committeeApplicationResponse = client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS.formatted(committeeId)))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteeApplicationResponse.class)
                .returnResult().getResponseBody();

        assertEquals(Committee.Status.OPEN_TO_APPLICATIONS.name(), committeeApplicationResponse.getStatus().name());
        assertEquals("Q1", committeeApplicationResponse.getProjectQuestions().get(0).getQuestion());
        assertEquals(false, committeeApplicationResponse.getProjectQuestions().get(0).getRequired());
        assertEquals(null, committeeApplicationResponse.getProjectQuestions().get(0).getAnswer());
        assertEquals(q1.id().value(), committeeApplicationResponse.getProjectQuestions().get(0).getId());
        assertEquals("Q2", committeeApplicationResponse.getProjectQuestions().get(1).getQuestion());
        assertEquals(true, committeeApplicationResponse.getProjectQuestions().get(1).getRequired());
        assertEquals(null, committeeApplicationResponse.getProjectQuestions().get(1).getAnswer());
        assertEquals(q2.id().value(), committeeApplicationResponse.getProjectQuestions().get(1).getId());
        assertFalse(committeeApplicationResponse.getHasStartedApplication());
    }

    @Autowired
    ProjectLeadRepository projectLeadRepository;
    @Autowired
    CustomerIOProperties customerIOProperties;

    @Test
    @Order(2)
    void should_put_application() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        projectLeadRepository.save(new ProjectLeadEntity(bretzel, pierre.user().getId()));
        final CommitteeApplicationRequest committeeApplicationRequest = new CommitteeApplicationRequest();
        final CommitteeProjectAnswerRequest answerRequest1 = new CommitteeProjectAnswerRequest()
                .answer(faker.pokemon().name())
                .questionId(projectQuestionId1.value());
        final CommitteeProjectAnswerRequest answerRequest2 = new CommitteeProjectAnswerRequest()
                .answer(faker.pokemon().name())
                .questionId(projectQuestionId2.value());
        committeeApplicationRequest.setAnswers(List.of(
                answerRequest1,
                answerRequest2
        ));

        // When
        client.put()
                .uri(getApiURI(PUT_COMMITTEES_APPLICATIONS.formatted(committeeId.value(), bretzel)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(committeeApplicationRequest))
                // Then
                .exchange()
                .expectStatus()
                .isUnauthorized();

        // When
        client.put()
                .uri(getApiURI(PUT_COMMITTEES_APPLICATIONS.formatted(committeeId.value(), bretzel)))
                .header("Authorization", "Bearer " + pierre.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(committeeApplicationRequest))
                // Then
                .exchange()
                .expectStatus()
                .isEqualTo(204);

        projectMailOutboxJob.run();
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getNewCommitteeApplicationEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(pierre.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.projectName", equalTo("Bretzel")))
                        .withRequestBody(matchingJsonPath("$.message_data.projectId", equalTo(bretzel.toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.committeeId", equalTo(committeeId.value().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.committeeName", equalTo("Mr. Needful")))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo("PierreOucif")))
                        .withRequestBody(matchingJsonPath("$.message_data.applicationEndDate", containing(" UTC")))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(pierre.user().getGithubEmail())))
                        .withRequestBody(matchingJsonPath("$.from", equalTo(customerIOProperties.getOnlyDustAdminEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("Your application to committee %s".formatted("Mr. Needful")))));

        // When
        final CommitteeApplicationResponse committeeApplicationResponse = client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS.formatted(committeeId), Map.of("projectId", bretzel.toString())))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(CommitteeApplicationResponse.class)
                .returnResult().getResponseBody();

        assertEquals(Committee.Status.OPEN_TO_APPLICATIONS.name(), committeeApplicationResponse.getStatus().name());
        assertEquals(answerRequest1.getQuestionId(), committeeApplicationResponse.getProjectQuestions().get(0).getId());
        assertEquals(answerRequest1.getAnswer(), committeeApplicationResponse.getProjectQuestions().get(0).getAnswer());
        assertEquals(answerRequest2.getQuestionId(), committeeApplicationResponse.getProjectQuestions().get(1).getId());
        assertEquals(answerRequest2.getAnswer(), committeeApplicationResponse.getProjectQuestions().get(1).getAnswer());
        assertTrue(committeeApplicationResponse.getHasStartedApplication());

        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS.formatted(committeeId), Map.of("projectId", bretzel.toString())))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                          {
                           "projectInfos": {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "projectLeads": [
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
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0"
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
                                "amountSentInUsd": 0.0
                              }
                            }
                        }
                        """);
    }

    @Test
    @Order(3)
    void should_get_application_no_matter_the_status() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        committeeFacadePort.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_APPLICATIONS.formatted(committeeId), Map.of("projectId", bretzel.toString())))
                .header("Authorization", "Bearer " + pierre.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                          {
                           "projectInfos": {
                              "id": "7d04163c-4187-4313-8066-61504d34fc56",
                              "slug": "bretzel",
                              "name": "Bretzel",
                              "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                              "projectLeads": [
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
                                },
                                {
                                  "githubUserId": 16590657,
                                  "login": "PierreOucif",
                                  "avatarUrl": "https://avatars.githubusercontent.com/u/16590657?v=4",
                                  "id": "fc92397c-3431-4a84-8054-845376b630a0"
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
                                "amountSentInUsd": 0.0
                              }
                            }
                        }
                        """);
    }


    @Test
    @Order(4)
    void should_get_committee() {
        // Given
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();
        committeeFacadePort.updateStatus(committeeId, Committee.Status.OPEN_TO_VOTES);

        // When
        client.get()
                .uri(getApiURI(COMMITTEES_BY_ID.formatted(committeeId)))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "name": "Mr. Needful",
                          "applicationStartDate": "2024-05-19T02:58:44.399Z",
                          "applicationEndDate": "2024-05-25T20:06:27.482Z",
                          "status": "OPEN_TO_VOTES",
                          "sponsor": null
                        }
                        """);
    }


}
