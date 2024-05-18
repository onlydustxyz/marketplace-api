package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public interface CommitteeMapper {

    static Committee.Application committeeApplicationRequestToDomain(final CommitteeApplicationRequest committeeApplicationRequest,
                                                                     final UUID userId, final UUID projectId) {
        final List<Committee.ProjectAnswer> projectAnswers = new ArrayList<>();
        for (CommitteeProjectAnswerRequest answer : committeeApplicationRequest.getAnswers()) {
            projectAnswers.add(new Committee.ProjectAnswer(new Committee.ProjectQuestion(answer.getQuestion(), answer.getRequired()), answer.getAnswer()));
        }
        return new Committee.Application(userId, projectId, projectAnswers);
    }

    static CommitteeApplicationResponse committeeApplicationViewToResponse(CommitteeApplicationView committeeApplicationView) {
        final CommitteeApplicationResponse committeeApplicationResponse = new CommitteeApplicationResponse();
        committeeApplicationResponse.setProjectQuestions(committeeApplicationView.answers().stream().map(projectAnswer -> new CommitteeProjectQuestionResponse()
                .question(projectAnswer.projectQuestion().question())
                .required(projectAnswer.projectQuestion().required())
                .answer(projectAnswer.answer())
        ).toList());
        committeeApplicationResponse.setStatus(statusToResponse(committeeApplicationView.status()));
        committeeApplicationResponse.setProjectInfos(isNull(committeeApplicationView.projectInfosView()) ? null : new CommitteeProjectInfosResponse()
                .shortDescription(committeeApplicationView.projectInfosView().shortDescription())
                .projectLeads(committeeApplicationView.projectInfosView().projectLeads().stream()
                        .map(projectLeaderLinkView -> new RegisteredUserResponse()
                                .id(projectLeaderLinkView.getId())
                                .githubUserId(projectLeaderLinkView.getGithubUserId())
                                .avatarUrl(projectLeaderLinkView.getAvatarUrl())
                                .login(projectLeaderLinkView.getLogin())
                        ).toList())
                .longDescription(committeeApplicationView.projectInfosView().longDescription())
                .last3monthsMetrics(
                        new ProjectLast3MonthsMetricsResponse()
                                .activeContributors(committeeApplicationView.projectInfosView().activeContributors())
                                .amountSentInUsd(committeeApplicationView.projectInfosView().amountSentInUsd())
                                .contributorsRewarded(committeeApplicationView.projectInfosView().contributorsRewarded())
                                .contributionsCompleted(committeeApplicationView.projectInfosView().contributionsCompleted())
                                .newContributors(committeeApplicationView.projectInfosView().newContributors())
                                .openIssues(committeeApplicationView.projectInfosView().openIssue())
                )
        );
        return committeeApplicationResponse;
    }

    static CommitteeStatus statusToResponse(final Committee.Status status) {
        return switch (status) {
            case CLOSED -> CommitteeStatus.CLOSED;
            case DRAFT -> throw OnlyDustException.internalServerError("Cannot get DRAFT committee in the SaaS");
            case OPEN_TO_APPLICATIONS -> CommitteeStatus.OPEN_TO_APPLICATIONS;
            case OPEN_TO_VOTES -> CommitteeStatus.OPEN_TO_VOTES;
        };
    }
}
