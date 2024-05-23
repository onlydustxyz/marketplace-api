package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;

import java.util.Comparator;
import java.util.UUID;

import static java.util.Objects.isNull;

public interface BackOfficeCommitteeMapper {

    static CommitteeResponse toCommitteeResponse(Committee committee) {
        final CommitteeResponse committeeResponse = new CommitteeResponse();
        committeeResponse.setName(committee.name());
        committeeResponse.setId(committee.id().value());
        committeeResponse.setApplicationStartDate(committee.applicationStartDate());
        committeeResponse.setApplicationEndDate(committee.applicationEndDate());
        return committeeResponse;
    }

    static CommitteePageResponse toCommitteePageResponse(Page<CommitteeLinkView> page, int pageIndex) {
        final CommitteePageResponse committeePageResponse = new CommitteePageResponse();
        committeePageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        committeePageResponse.setTotalPageNumber(page.getTotalPageNumber());
        committeePageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        committeePageResponse.setTotalItemNumber(page.getTotalItemNumber());
        for (CommitteeLinkView committeeLinkView : page.getContent()) {
            committeePageResponse.addCommitteesItem(new CommitteeLinkResponse()
                    .id(committeeLinkView.id().value())
                    .name(committeeLinkView.name())
                    .applicationStartDate(committeeLinkView.startDate())
                    .applicationEndDate(committeeLinkView.endDate())
                    .status(statusToResponse(committeeLinkView.status()))
            );
        }
        return committeePageResponse;
    }

    static CommitteeStatus statusToResponse(final Committee.Status status) {
        return switch (status) {
            case OPEN_TO_APPLICATIONS -> CommitteeStatus.OPEN_TO_APPLICATIONS;
            case OPEN_TO_VOTES -> CommitteeStatus.OPEN_TO_VOTES;
            case DRAFT -> CommitteeStatus.DRAFT;
            case CLOSED -> CommitteeStatus.CLOSED;
        };
    }

    static Committee.Status statusToDomain(final CommitteeStatus status) {
        return switch (status) {
            case CLOSED -> Committee.Status.CLOSED;
            case OPEN_TO_APPLICATIONS -> Committee.Status.OPEN_TO_APPLICATIONS;
            case OPEN_TO_VOTES -> Committee.Status.OPEN_TO_VOTES;
            case DRAFT -> Committee.Status.DRAFT;
        };
    }

    static Committee updateCommitteeRequestToDomain(final UUID committeeId, final UpdateCommitteeRequest updateCommitteeRequest) {
        final Committee committee = Committee.builder()
                .id(Committee.Id.of(committeeId))
                .status(statusToDomain(updateCommitteeRequest.getStatus()))
                .name(updateCommitteeRequest.getName())
                .applicationStartDate(updateCommitteeRequest.getApplicationStartDate())
                .applicationEndDate(updateCommitteeRequest.getApplicationEndDate())
                .sponsorId(updateCommitteeRequest.getSponsorId())
                .build();
        committee.projectQuestions().addAll(updateCommitteeRequest.getProjectQuestions().stream()
                .map(BackOfficeCommitteeMapper::getProjectQuestion)
                .toList());
        return committee;
    }

    private static ProjectQuestion getProjectQuestion(ProjectQuestionRequest projectQuestionRequest) {
        return isNull(projectQuestionRequest.getId()) ? new ProjectQuestion(projectQuestionRequest.getQuestion(),
                projectQuestionRequest.getRequired()) : new ProjectQuestion(ProjectQuestion.Id.of(projectQuestionRequest.getId()),
                projectQuestionRequest.getQuestion(),
                projectQuestionRequest.getRequired());
    }

    static CommitteeResponse toCommitteeResponse(final CommitteeView committeeView) {
        return new CommitteeResponse()
                .id(committeeView.id().value())
                .name(committeeView.name())
                .applicationStartDate(committeeView.applicationStartDate())
                .applicationEndDate(committeeView.applicationEndDate())
                .status(statusToResponse(committeeView.status()))
                .projectQuestions(committeeView.projectQuestions().stream()
                        .sorted(Comparator.comparing(ProjectQuestion::question))
                        .map(projectQuestion -> new ProjectQuestionResponse()
                                .id(projectQuestion.id().value())
                                .question(projectQuestion.question())
                                .required(projectQuestion.required()))
                        .toList())
                .applications(isNull(committeeView.committeeApplicationLinks()) ? null : committeeView.committeeApplicationLinks().stream()
                        .map(committeeApplicationLinkView -> new ApplicationResponse()
                                .applicant(new UserLinkResponse()
                                        .avatarUrl(committeeApplicationLinkView.applicant().getAvatarUrl())
                                        .login(committeeApplicationLinkView.applicant().getLogin())
                                        .userId(committeeApplicationLinkView.applicant().getId())
                                        .githubUserId(committeeApplicationLinkView.applicant().getGithubUserId())
                                )
                                .project(new ProjectLinkResponse()
                                        .id(committeeApplicationLinkView.projectShortView().id())
                                        .slug(committeeApplicationLinkView.projectShortView().slug())
                                        .name(committeeApplicationLinkView.projectShortView().name())
                                        .logoUrl(isNull(committeeApplicationLinkView.projectShortView().logoUrl()) ? null :
                                                committeeApplicationLinkView.projectShortView().logoUrl())
                                )
                        ).toList())
                .sponsor(isNull(committeeView.sponsor()) ? null :
                        new SponsorLinkResponse()
                                .id(committeeView.sponsor().id())
                                .avatarUrl(committeeView.sponsor().logoUrl())
                                .name(committeeView.sponsor().name()));
    }

    static CommitteeProjectApplicationResponse committeeApplicationDetailsToResponse(final CommitteeApplicationDetailsView view) {
        final CommitteeProjectApplicationResponse committeeProjectApplicationResponse = new CommitteeProjectApplicationResponse();
        committeeProjectApplicationResponse.setProject(new ProjectLinkResponse()
                .id(view.projectShortView().id())
                .slug(view.projectShortView().slug())
                .name(view.projectShortView().name())
                .logoUrl(isNull(view.projectShortView().logoUrl()) ? null : view.projectShortView().logoUrl())
        );
        committeeProjectApplicationResponse.setProjectQuestions(view.answers().stream()
                .map(answer -> new ProjectAnswerResponse()
                        .answer(answer.answer())
                        .question(answer.question())
                        .required(answer.required())
                )
                .toList()
        );
        return committeeProjectApplicationResponse;
    }
}
