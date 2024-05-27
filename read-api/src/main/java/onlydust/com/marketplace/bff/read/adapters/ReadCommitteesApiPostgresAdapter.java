package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadCommitteesApi;
import onlydust.com.marketplace.api.contract.model.CommitteeAssignmentLinkResponse;
import onlydust.com.marketplace.api.contract.model.MyCommitteeAssignmentsResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLinkViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeLinkViewRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.mapper.CommitteeMapper;
import onlydust.com.marketplace.bff.read.mapper.ProjectMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadCommitteesApiPostgresAdapter implements ReadCommitteesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final CommitteeLinkViewRepository committeeLinkViewRepository;
    private final CommitteeJuryVoteViewRepository committeeJuryVoteViewRepository;

    @Override
    public ResponseEntity<MyCommitteeAssignmentsResponse> getCommitteeAssignments(UUID committeeId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var userVotesForCommittee = committeeJuryVoteViewRepository.findAllByCommitteeIdAndUserId(committeeId, authenticatedUser.getId());
        if (userVotesForCommittee.isEmpty()) {
            throw OnlyDustException.notFound("No assignement found for user %s on committee %s".formatted(authenticatedUser.getId(), committeeId));
        }
        final var committee = committeeLinkViewRepository.findById(committeeId)
                .orElseThrow(() -> OnlyDustException.notFound("Committee %s not found".formatted(committeeId)));

        final Map<ProjectLinkViewEntity, Double> averageScorePerProjects = userVotesForCommittee.stream()
                .filter(vote -> vote.getScore() != null)
                .collect(Collectors.groupingBy(CommitteeJuryVoteViewEntity::getProject, Collectors.averagingInt(CommitteeJuryVoteViewEntity::getScore)));

        return ResponseEntity.ok(new MyCommitteeAssignmentsResponse()
                .name(committee.getName())
                .status(CommitteeMapper.map(committee.getStatus()))
                .projectAssignments(userVotesForCommittee.stream()
                        .map(vote -> new CommitteeAssignmentLinkResponse()
                                .project(ProjectMapper.map(vote.getProject()))
                                .score(Optional.ofNullable(averageScorePerProjects.get(vote.getProject())).map(BigDecimal::valueOf).orElse(null)))
                        .toList()));
    }
}
