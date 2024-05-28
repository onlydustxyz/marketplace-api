package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.commitee.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
                    .projectCount(committeeLinkView.projectCount())
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
                .votePerJury(updateCommitteeRequest.getVotePerJury())
                .build();
        committee.projectQuestions().addAll(updateCommitteeRequest.getProjectQuestions().stream()
                .map(BackOfficeCommitteeMapper::getProjectQuestion)
                .toList());
        committee.juryCriteria().addAll(updateCommitteeRequest.getJuryCriteria().stream().map(BackOfficeCommitteeMapper::getJuryCriteria).toList());
        committee.juryIds().addAll(updateCommitteeRequest.getJuryMemberIds());
        return committee;
    }

    private static JuryCriteria getJuryCriteria(final JuryCriteriaRequest juryCriteriaRequest) {
        return isNull(juryCriteriaRequest.getId()) ? new JuryCriteria(juryCriteriaRequest.getCriteria()) :
                new JuryCriteria(JuryCriteria.Id.of(juryCriteriaRequest.getId()), juryCriteriaRequest.getCriteria());
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
                                .name(committeeView.sponsor().name()))
                .juries(isNull(committeeView.juries()) ? null : committeeView.juries().stream()
                        .map(registeredContributorLinkView -> new UserLinkResponse()
                                .avatarUrl(registeredContributorLinkView.getAvatarUrl())
                                .githubUserId(registeredContributorLinkView.getGithubUserId())
                                .login(registeredContributorLinkView.getLogin())
                                .userId(registeredContributorLinkView.getId())
                        ).toList())
                .juryCount(isNull(committeeView.juries()) ? 0 : committeeView.juries().size())
                .juryCriteria(isNull(committeeView.juryCriteria()) ? null : committeeView.juryCriteria().stream()
                        .map(juryCriteria -> new JuryCriteriaResponse().criteria(juryCriteria.criteria()).id(juryCriteria.id().value())).toList())
                .votePerJury(committeeView.votePerJury())
                .completedAssignments(isNull(committeeView.juryAssignments()) ? 0 :
                        committeeView.juryAssignments().stream().map(JuryAssignmentView::completedAssigment).reduce(Integer::sum).orElse(0))
                .totalAssignments(isNull(committeeView.juryAssignments()) ? 0 : committeeView.juryAssignments().stream()
                        .map(JuryAssignmentView::totalAssigned).reduce(Integer::sum).orElse(0))
                .juryAssignments(isNull(committeeView.juryAssignments()) ? null : committeeView.juryAssignments().stream()
                        .map(juryAssignmentView -> new JuryAssignmentResponse()
                                .completedAssignments(juryAssignmentView.completedAssigment())
                                .totalAssignment(juryAssignmentView.totalAssigned())
                                .user(new UserLinkResponse()
                                        .githubUserId(juryAssignmentView.user().getGithubUserId())
                                        .userId(juryAssignmentView.user().getId())
                                        .login(juryAssignmentView.user().getLogin())
                                        .avatarUrl(juryAssignmentView.user().getAvatarUrl())
                                )
                                .projectsAssigned(
                                        juryAssignmentView.projectsAssigned().stream()
                                                .map(projectShortView -> new ProjectLinkResponse()
                                                        .id(projectShortView.id())
                                                        .slug(projectShortView.slug())
                                                        .name(projectShortView.name())
                                                        .logoUrl(isNull(projectShortView.logoUrl()) ? null : projectShortView.logoUrl()))
                                                .toList()
                                )
                        )
                        .toList())
                ;
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
        committeeProjectApplicationResponse.setTotalScore(isNull(view.projectJuryVoteViews()) || view.projectJuryVoteViews().isEmpty() ? null :
                view.projectJuryVoteViews().stream()
                        .filter(projectJuryVoteView -> nonNull(projectJuryVoteView.getTotalScore()))
                        .map(ProjectJuryVoteView::getTotalScore)
                        .reduce(BigDecimal::add).map(bigDecimal -> bigDecimal.divide(BigDecimal.valueOf(view.projectJuryVoteViews().stream()
                                .filter(projectJuryVoteView -> nonNull(projectJuryVoteView.getTotalScore()))
                                .count()),1, RoundingMode.HALF_UP)).orElse(null));
        committeeProjectApplicationResponse.setJuryVotes(view.projectJuryVoteViews().stream().map(projectJuryVoteView ->
                new JuryVoteResponse()
                        .totalScore(projectJuryVoteView.getTotalScore())
                        .jury(new UserLinkResponse()
                                .userId(projectJuryVoteView.user().getId())
                                .login(projectJuryVoteView.user().getLogin())
                                .githubUserId(projectJuryVoteView.user().getGithubUserId())
                                .avatarUrl(projectJuryVoteView.user().getAvatarUrl())
                        )
                        .answers(projectJuryVoteView.voteViews().stream()
                                .map(voteView -> new ScoredAnswerResponse()
                                        .criteria(voteView.criteria())
                                        .score(voteView.score())
                                ).toList())
        ).toList());
        return committeeProjectApplicationResponse;
    }
}
