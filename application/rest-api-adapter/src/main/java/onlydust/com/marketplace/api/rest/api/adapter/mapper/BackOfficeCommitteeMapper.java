package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;

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
        committee.juryIds().addAll(updateCommitteeRequest.getJuryMemberIds().stream().map(UserId::of).toList());
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
}
