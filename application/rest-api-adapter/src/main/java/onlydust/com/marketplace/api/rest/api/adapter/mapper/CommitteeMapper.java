package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationView;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;

import java.util.*;

import static java.util.Objects.isNull;

public interface CommitteeMapper {

    static Committee.Application committeeApplicationRequestToDomain(final CommitteeApplicationRequest committeeApplicationRequest,
                                                                     final UUID userId, final UUID projectId) {
        final List<Committee.ProjectAnswer> projectAnswers = new ArrayList<>();
        for (CommitteeProjectAnswerRequest answer : committeeApplicationRequest.getAnswers()) {
            projectAnswers.add(new Committee.ProjectAnswer(ProjectQuestion.Id.of(answer.getQuestionId()), answer.getAnswer()));
        }
        return new Committee.Application(userId, projectId, projectAnswers);
    }

    static CommitteeApplicationResponse committeeApplicationViewToResponse(CommitteeApplicationView committeeApplicationView) {
        final CommitteeApplicationResponse committeeApplicationResponse = new CommitteeApplicationResponse();
        committeeApplicationResponse.setApplicationStartDate(committeeApplicationView.applicationStartDate());
        committeeApplicationResponse.setApplicationEndDate(committeeApplicationView.applicationEndDate());
        committeeApplicationResponse.setProjectQuestions(committeeApplicationView.answers().stream()
                .sorted(Comparator.comparing(ProjectAnswerView::question))
                .map(projectAnswer -> new CommitteeProjectQuestionResponse()
                        .question(projectAnswer.question())
                        .required(projectAnswer.required())
                        .id(projectAnswer.questionId().value())
                        .answer(projectAnswer.answer())
                ).toList());
        committeeApplicationResponse.hasStartedApplication(committeeApplicationView.hasStartedApplication());
        committeeApplicationResponse.setStatus(statusToResponse(committeeApplicationView.status()));
        committeeApplicationResponse.setProjectInfos(isNull(committeeApplicationView.projectInfosView()) ? null : new CommitteeProjectInfosResponse()
                .id(committeeApplicationView.projectInfosView().projectId())
                .name(committeeApplicationView.projectInfosView().name())
                .slug(committeeApplicationView.projectInfosView().slug())
                .logoUrl(isNull(committeeApplicationView.projectInfosView().logoUri()) ? null : committeeApplicationView.projectInfosView().logoUri().toString())
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
