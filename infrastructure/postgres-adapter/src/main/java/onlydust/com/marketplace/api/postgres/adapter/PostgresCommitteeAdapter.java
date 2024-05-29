package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectInfosQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeBudgetAllocationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.*;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeApplicationDetailsView;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;
    private final ProjectInfosViewRepository projectInfosViewRepository;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteRepository committeeJuryVoteRepository;
    private final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository;
    private final CommitteeBudgetAllocationRepository committeeBudgetAllocationRepository;

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
    public Optional<Committee> findById(Committee.Id committeeId) {
        return committeeRepository.findById(committeeId.value()).map(CommitteeEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeRepository.updateStatus(committeeId.value(), status.name());
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
            final ProjectInfosQueryEntity projectInfos = projectInfosViewRepository.findByProjectIdWithoutMetrics(projectId)
                    .orElseThrow(() -> notFound("Project %s not found".formatted(projectId)));
            return Optional.of(
                    new CommitteeApplicationDetailsView(
                            projectAnswerViews, new ProjectShortView(projectInfos.id(), projectInfos.slug(), projectInfos.name(),
                            projectInfos.logoUrl(), projectInfos.shortDescription(), ProjectVisibility.valueOf(projectInfos.visibility())), true,
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

    @Override
    public List<JuryAssignment> findJuryAssignments(Committee.Id committeeId) {
        return committeeJuryVoteRepository.findAllByCommitteeId(committeeId.value()).stream()
                .collect(groupingBy(CommitteeJuryVoteEntity::getProjectId,
                        groupingBy(CommitteeJuryVoteEntity::getUserId,
                                groupingBy(vote -> JuryCriteria.Id.of(vote.getCriteriaId()),
                                        mapping(CommitteeJuryVoteEntity::getScore,
                                                reducing(null, (a, b) -> b))))))
                .entrySet().stream()
                .flatMap(byProject -> byProject.getValue().entrySet().stream()
                        .map(byJury -> JuryAssignment.withVotes(byJury.getKey(), committeeId, byProject.getKey(), byJury.getValue()))
                ).toList();
    }

    @Override
    public void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<UUID, BigDecimal> projectAllocations) {
        committeeBudgetAllocationRepository.saveAll(projectAllocations.entrySet().stream()
                .map(entry -> CommitteeBudgetAllocationEntity.fromDomain(committeeId, currencyId, entry.getKey(), entry.getValue()))
                .collect(toList()));
    }
}
