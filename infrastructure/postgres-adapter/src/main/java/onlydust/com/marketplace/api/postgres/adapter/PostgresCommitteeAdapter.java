package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeBudgetAllocationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeJuryVoteEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeBudgetAllocationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeLinkViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.JuryAssignment;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.commitee.CommitteeLinkView;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteRepository committeeJuryVoteRepository;
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

    @Override
    @Transactional
    public void saveJuryAssignments(List<JuryAssignment> juryAssignments) {
        committeeJuryVoteRepository.saveAll(juryAssignments.stream()
                .map(juryAssignment -> juryAssignment.getVotes().entrySet().stream().map(juryVote -> CommitteeJuryVoteEntity.builder()
                        .criteriaId(juryVote.getKey().value())
                        .score(juryVote.getValue().orElse(null))
                        .committeeId(juryAssignment.getCommitteeId().value())
                        .projectId(juryAssignment.getProjectId().value())
                        .userId(juryAssignment.getJuryId().value())
                        .build()
                ).collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    @Override
    public void saveJuryVotes(UserId juryId, Committee.Id committeeId, ProjectId projectId, Map<JuryCriteria.Id, Integer> votes) {
        committeeJuryVoteRepository.saveAll(votes.entrySet().stream()
                .map(juryVote -> CommitteeJuryVoteEntity.builder()
                        .criteriaId(juryVote.getKey().value())
                        .score(juryVote.getValue())
                        .committeeId(committeeId.value())
                        .projectId(projectId.value())
                        .userId(juryId.value())
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
                        .map(byJury -> JuryAssignment.withVotes(UserId.of(byJury.getKey()), committeeId, ProjectId.of(byProject.getKey()), byJury.getValue()))
                ).toList();
    }

    @Override
    public void saveAllocations(Committee.Id committeeId, UUID currencyId, Map<ProjectId, BigDecimal> projectAllocations) {
        committeeBudgetAllocationRepository.saveAll(projectAllocations.entrySet().stream()
                .map(entry -> CommitteeBudgetAllocationEntity.fromDomain(committeeId, currencyId, entry.getKey(), entry.getValue()))
                .collect(toList()));
    }
}
