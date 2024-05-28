package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectInfosQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoCommitteeQueryRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeView;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;
    private final BoCommitteeQueryRepository boCommitteeQueryRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteRepository committeeJuryVoteRepository;
    private final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository;

    @Override
    @Transactional
    public Committee save(Committee committee) {
        return committeeRepository.save(CommitteeEntity.fromDomain(committee)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize) {
        final var committees = committeeLinkViewRepository.findAllBy(PageRequest.of(pageIndex, pageSize));
        return Page.<CommitteeLinkView>builder()
                .content(committees.getContent().stream().map(CommitteeLinkViewEntity::toLink).toList())
                .totalPageNumber(committees.getTotalPages())
                .totalItemNumber((int) committees.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommitteeView> findViewById(Committee.Id committeeId) {
        return boCommitteeQueryRepository.findById(committeeId.value()).map(BoCommitteeQueryEntity::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Committee> findById(Committee.Id committeeId) {
        return committeeRepository.findById(committeeId.value()).map(CommitteeEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeRepository.updateStatus(committeeId.value(), status.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAnswerView> getApplicationAnswers(Committee.Id committeeId, UUID projectId) {
        return getProjectAnswerViews(committeeId, projectId);
    }

    private @NotNull List<ProjectAnswerView> getProjectAnswerViews(Committee.Id committeeId, UUID projectId) {
        return committeeProjectAnswerViewRepository.findByCommitteeIdAndAndProjectId(committeeId.value(), projectId).stream()
                .map(committeeProjectAnswerView -> new ProjectAnswerView(ProjectQuestion.Id.of(committeeProjectAnswerView.getQuestionId()),
                        committeeProjectAnswerView.getProjectQuestion().getQuestion(), committeeProjectAnswerView.getProjectQuestion().getRequired(),
                        committeeProjectAnswerView.getAnswer()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommitteeApplicationDetailsView> findByCommitteeIdAndProjectId(Committee.Id committeeId, UUID projectId) {
        final List<ProjectAnswerView> projectAnswerViews = getProjectAnswerViews(committeeId, projectId);
        if (projectAnswerViews.isEmpty()) {
            return Optional.empty();
        } else {
            final ProjectInfosQueryEntity projectInfos = projectInfosViewRepository.findByProjectIdWithoutMetrics(projectId);
            return Optional.of(
                    new CommitteeApplicationDetailsView(
                            projectAnswerViews, new ProjectShortView(projectInfos.getId(), projectInfos.getSlug(), projectInfos.getName(),
                            projectInfos.getLogoUrl(), projectInfos.getShortDescription(), ProjectVisibility.valueOf(projectInfos.getVisibility())), true,
                            CommitteeJuryVoteViewEntity.toDomain(committeeJuryVoteViewRepository.findAllByCommitteeIdAndProjectId(committeeId.value(),
                                    projectId))
                    )
            );
        }
    }

    @Override
    @Transactional
    public void saveJuryAssignments(List<JuryAssignment> juryAssignments) {
        committeeJuryVoteRepository.saveAll(juryAssignments.stream()
                .map(juryAssignment -> juryAssignment.getVotes().entrySet().stream().map(juryVote -> CommitteeJuryVoteEntity.builder()
                        .criteriaId(juryVote.getKey().value())
                        .score(juryVote.getValue().orElse(null))
                        .committeeId(juryAssignment.getCommitteeId().value())
                        .projectId(juryAssignment.getProjectId())
                        .userId(juryAssignment.getJuryId())
                        .build()
                ).collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    @Override
    public void saveJuryVotes(UUID juryId, Committee.Id committeeId, UUID projectId, Map<JuryCriteria.Id, Integer> votes) {
        committeeJuryVoteRepository.saveAll(votes.entrySet().stream()
                .map(juryVote -> CommitteeJuryVoteEntity.builder()
                        .criteriaId(juryVote.getKey().value())
                        .score(juryVote.getValue())
                        .committeeId(committeeId.value())
                        .projectId(projectId)
                        .userId(juryId)
                        .build()
                )
                .collect(Collectors.toSet()));
    }
}
