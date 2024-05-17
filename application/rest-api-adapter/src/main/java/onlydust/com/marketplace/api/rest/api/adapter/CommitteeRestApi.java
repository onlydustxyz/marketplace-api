package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.CommitteesApi;
import onlydust.com.marketplace.api.contract.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@Tags(@Tag(name = "Committees"))
@AllArgsConstructor
public class CommitteeRestApi implements CommitteesApi {

    @Override
    public ResponseEntity<CommitteeApplicationResponse> getApplication(UUID committeeId, UUID projectId) {
        final CommitteeApplicationResponse committeeApplicationResponse = new CommitteeApplicationResponse();
        committeeApplicationResponse.setProjectInfos(new CommitteeProjectInfosResponse()
                .projectLeads(List.of(
                        new RegisteredUserResponse().githubUserId(16590657L)
                                .id(UUID.fromString("fc92397c-3431-4a84-8054-845376b630a0"))
                                .avatarUrl("https://avatars.githubusercontent.com/u/16590657?v=4")
                                .login("PierreOucif"),
                        new RegisteredUserResponse().githubUserId(595505L)
                                .id(UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9"))
                                .avatarUrl("https://avatars.githubusercontent.com/u/595505?v=4")
                                .login("ofux"
                                )))
                .last3monthsMetrics(new ProjectLast3MonthsMetricsResponse()
                        .activeContributors(3)
                        .amountSentInUsd(BigDecimal.valueOf(11.2))
                        .contributionsCompleted(4)
                        .openIssues(8)
                        .newContributors(4)
                        .contributorsRewarded(1)
                )
                .longDescription("""
                        Bretzel is your best chance to match with your secret crush     \s
                        Ever liked someone but never dared to tell them?     \s
                             \s
                        Bretzel is your chance to match with your secret crush     \s
                        All you need is a LinkedIn profile.     \s
                             \s
                        1. Turn LinkedIn into a bretzel party: Switch the bretzel mode ON — you'll see bretzels next to everyone. Switch it OFF anytime.     \s
                        2. Give your bretzels under the radar: Give a bretzel to your crush, they will never know about it, unless they give you a bretzel too. Maybe they already have?     \s
                        3. Ooh la la, it's a match!  You just got bretzel’d! See all your matches in a dedicated space, and start chatting!""")
                .shortDescription("A project for people who love fruits")
        );
        committeeApplicationResponse.setStatus(CommitteeStatus.OPEN_TO_APPLICATIONS);
        committeeApplicationResponse.setProjectQuestions(List.of(
                new CommitteeProjectQuestionResponse()
                        .question("Question 1 ?")
                        .id(UUID.randomUUID())
                        .required(false),
                new CommitteeProjectQuestionResponse()
                        .question("Question 2 ?")
                        .id(UUID.randomUUID())
                        .required(true),
                new CommitteeProjectQuestionResponse()
                        .question("Question 3 ?")
                        .id(UUID.randomUUID())
                        .required(false)
        ));
        return CommitteesApi.super.getApplication(committeeId, projectId);
    }

    @Override
    public ResponseEntity<Void> createUpdateApplicationForCommittee(UUID committeeId, UUID projectId, CommitteeApplicationRequest committeeApplicationRequest) {
        return ResponseEntity.noContent().build();
    }
}
