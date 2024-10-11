package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadContributionsApi;
import onlydust.com.marketplace.api.contract.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadContributionsApiPostgresAdapter implements ReadContributionsApi {

    private static ContributionActivityPageResponse getNotAssignedStub() {
        return new ContributionActivityPageResponse()
                .hasMore(false)
                .nextPageIndex(0)
                .totalItemNumber(3)
                .totalPageNumber(1)
                .contributions(List.of(
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.ISSUE)
                                .githubTitle("Fix UI Bug on transaction history page")
                                .id("6af85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.NOT_ASSIGNED)
                                .githubStatus(GithubStatus.OPEN)
                                .githubId(12345L)
                                .githubNumber(1234L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .applicants(List.of(new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                .githubUserId(1L)
                                                .login("Antho")
                                                .isRegistered(true)
                                                .bio("I'm the best 1")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                .githubUserId(2L)
                                                .login("acomminos")
                                                .isRegistered(false)
                                                .bio("I'm the best 2")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                .githubUserId(3L)
                                                .login("bobeagan")
                                                .isRegistered(true)
                                                .bio("I'm the best 3")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                .githubUserId(4L)
                                                .login("alexbeno")
                                                .isRegistered(true)
                                                .bio("I'm the best 4")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))))
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5)),
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.ISSUE)
                                .githubTitle("Fix UI Bug on transaction history page")
                                .id("aaf85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.NOT_ASSIGNED)
                                .githubStatus(GithubStatus.OPEN)
                                .githubId(12345L)
                                .githubNumber(1234L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .applicants(List.of(new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                .githubUserId(1L)
                                                .login("Antho")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                .githubUserId(2L)
                                                .login("acomminos")
                                                .isRegistered(false)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                .githubUserId(3L)
                                                .login("bobeagan")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                .githubUserId(4L)
                                                .login("alexbeno")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))))
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5)),
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.ISSUE)
                                .githubTitle("Fix UI Bug on transaction history page")
                                .id("baf85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.NOT_ASSIGNED)
                                .githubStatus(GithubStatus.OPEN)
                                .githubId(12345L)
                                .githubNumber(1234L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .applicants(List.of(new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                .githubUserId(1L)
                                                .login("Antho")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                .githubUserId(2L)
                                                .login("acomminos")
                                                .isRegistered(false)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                .githubUserId(3L)
                                                .login("bobeagan")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                .githubUserId(4L)
                                                .login("alexbeno")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))))
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                ));
    }

    private static ContributionActivityPageResponse getInProgressStub() {
        return new ContributionActivityPageResponse()
                .hasMore(false)
                .nextPageIndex(0)
                .totalItemNumber(1)
                .totalPageNumber(1)
                .contributions(List.of(
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.ISSUE)
                                .githubTitle("Fix UI Bug on transaction history page in progress")
                                .id("caf85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.IN_PROGRESS)
                                .githubId(12345L)
                                .githubNumber(1234L)
                                .githubStatus(GithubStatus.OPEN)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .contributors(List.of(new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                        .githubUserId(2L)
                                                        .login("acomminos")
                                                        .isRegistered(false)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                        .githubUserId(4L)
                                                        .login("alexbeno")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                        .githubUserId(3L)
                                                        .login("bobeagan")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo")))
                                        )
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                ));
    }

    private static ContributionActivityPageResponse getToReviewStub() {
        return new ContributionActivityPageResponse()
                .hasMore(false)
                .nextPageIndex(0)
                .totalItemNumber(1)
                .totalPageNumber(1)
                .contributions(List.of(
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.PULL_REQUEST)
                                .githubTitle("PR to review on Fix UI Bug on transaction history page")
                                .id("eaf85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.TO_REVIEW)
                                .githubNumber(456L)
                                .githubStatus(GithubStatus.COMMENTED)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .contributors(List.of(new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                        .githubUserId(2L)
                                                        .login("acomminos")
                                                        .isRegistered(false)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                        .githubUserId(4L)
                                                        .login("alexbeno")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                        .githubUserId(3L)
                                                        .login("bobeagan")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo")))
                                        )
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                ));
    }

    private static ContributionActivityPageResponse getDoneStub() {
        return new ContributionActivityPageResponse()
                .hasMore(false)
                .nextPageIndex(0)
                .totalItemNumber(2)
                .totalPageNumber(1)
                .contributions(List.of(
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.PULL_REQUEST)
                                .githubTitle("PR done on Fix UI Bug on transaction history page #1")
                                .id("1af85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.DONE)
                                .githubStatus(GithubStatus.MERGED)
                                .githubNumber(456L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .contributors(List.of(new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                        .githubUserId(2L)
                                                        .login("acomminos")
                                                        .isRegistered(false)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                        .githubUserId(4L)
                                                        .login("alexbeno")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                        .githubUserId(3L)
                                                        .login("bobeagan")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo")))
                                        )
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                .completedAt(ZonedDateTime.now().minusDays(1).plusHours(5))
                                .totalRewardedAmount(new RewardTotalAmountsResponse().totalAmount(BigDecimal.valueOf(12345.8854)))
                                .linkedIssues(List.of(
                                        new ContributionResponse()
                                                .type(ContributionType.ISSUE)
                                                .githubTitle("Name of the issue #1")
                                                .githubNumber(111L)
                                                .githubStatus(GithubStatus.COMPLETED),
                                        new ContributionResponse()
                                                .type(ContributionType.ISSUE)
                                                .githubTitle("Name of the issue #2")
                                                .githubNumber(211L)
                                                .githubStatus(GithubStatus.COMPLETED),
                                        new ContributionResponse()
                                                .type(ContributionType.ISSUE)
                                                .githubTitle("Name of the issue #3")
                                                .githubNumber(311L)
                                                .githubStatus(GithubStatus.COMPLETED)
                                )),
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.PULL_REQUEST)
                                .githubTitle("PR done on Fix UI Bug on transaction history page #1")
                                .id("62f85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.DONE)
                                .githubStatus(GithubStatus.MERGED)
                                .githubNumber(456L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .contributors(List.of(new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                .githubUserId(1L)
                                                .login("Antho")
                                                .isRegistered(true)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))),
                                        new ContributorOverviewResponse()
                                                .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                .githubUserId(2L)
                                                .login("acomminos")
                                                .isRegistered(false)
                                                .bio("I'm the best")
                                                .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me/foo"))))
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                .completedAt(ZonedDateTime.now().minusDays(1))
                                .totalRewardedAmount(new RewardTotalAmountsResponse().totalAmount(BigDecimal.valueOf(12345.8854)))
                                .linkedIssues(List.of(
                                        new ContributionResponse()
                                                .type(ContributionType.ISSUE)
                                                .githubTitle("Name of the issue #1")
                                                .githubNumber(112L)
                                                .githubStatus(GithubStatus.COMPLETED)
                                ))
                ));
    }

    private static ContributionActivityPageResponse getArchivedStub() {
        return new ContributionActivityPageResponse()
                .hasMore(false)
                .nextPageIndex(0)
                .totalItemNumber(1)
                .totalPageNumber(1)
                .contributions(List.of(
                        new ContributionActivityPageItemResponse()
                                .type(ContributionType.PULL_REQUEST)
                                .githubTitle("PR archived on Fix UI Bug on transaction history page")
                                .id("64f85286-2b4f-447c-a265-56ace336cce7")
                                .activityStatus(ContributionActivityStatus.ARCHIVED)
                                .githubStatus(GithubStatus.CLOSED)
                                .githubNumber(456L)
                                .githubLabels(List.of(new GithubLabel("GFI"), new GithubLabel("GFI2"), new GithubLabel("GFI3")))
                                .contributors(List.of(new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/628035?v=4")
                                                        .githubUserId(2L)
                                                        .login("acomminos")
                                                        .isRegistered(false)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/17259618?v=4")
                                                        .githubUserId(4L)
                                                        .login("alexbeno")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo"))),
                                                new ContributorOverviewResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/100226?v=4")
                                                        .githubUserId(3L)
                                                        .login("bobeagan")
                                                        .isRegistered(true)
                                                        .bio("I'm the best")
                                                        .contacts(List.of(new ContactInformation().channel(ContactInformationChannel.TELEGRAM).contact("t.me" +
                                                                                                                                                       "/foo")))
                                        )
                                )
                                .createdAt(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                .completedAt(ZonedDateTime.now().minusDays(1))
                                .linkedIssues(List.of(
                                        new ContributionResponse()
                                                .type(ContributionType.ISSUE)
                                                .githubTitle("Name of the issue #3")
                                                .githubNumber(113L)
                                                .githubStatus(GithubStatus.COMPLETED)
                                ))
                ));
    }

    @Override
    public ResponseEntity<ContributionActivityPageItemResponse> getContributionById(String contributionId) {
        return ResponseEntity.ok(getContributionsPageResponse(ContributionActivityStatus.DONE).getContributions().get(0));
    }

    @Override
    public ResponseEntity<ContributionEventListResponse> getContributionEvents(String contributionId) {
        return ResponseEntity.ok(new ContributionEventListResponse()
                .events(List.of(
                                new ContributionEventResponse()
                                        .timestamp(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                        .assigneeAdded(new ContributionEventResponseAssigneeAdded()
                                                .assignee(new GithubUserResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                )),
                                new ContributionEventResponse()
                                        .timestamp(ZonedDateTime.now().minusDays(2).minusHours(1).minusMinutes(5))
                                        .opened(new ContributionEventResponseOpened()
                                                .by(new GithubUserResponse()
                                                        .avatarUrl("https://avatars.githubusercontent.com/u/43467246?v=4")
                                                        .githubUserId(1L)
                                                        .login("Antho")
                                                ))
                        )
                ));
    }

    @Override
    public ResponseEntity<ContributionActivityPageResponse> getContributions(ContributionsQueryParams queryParams) {
        return ResponseEntity.ok(getContributionsPageResponse(queryParams.getStatuses().stream().findFirst().orElse(ContributionActivityStatus.IN_PROGRESS)));
    }

    private ContributionActivityPageResponse getContributionsPageResponse(final ContributionActivityStatus contributionActivityStatus) {
        return switch (contributionActivityStatus) {
            case NOT_ASSIGNED -> getNotAssignedStub();
            case TO_REVIEW -> getToReviewStub();
            case IN_PROGRESS -> getInProgressStub();
            case DONE -> getDoneStub();
            case ARCHIVED -> getArchivedStub();
        };
    }

}
