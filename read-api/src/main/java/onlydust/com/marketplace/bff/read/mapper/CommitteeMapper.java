package onlydust.com.marketplace.bff.read.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.CommitteeStatus;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeJuryVoteViewEntity;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CommitteeMapper {
    static CommitteeStatus map(final @NonNull Committee.Status status) {
        return switch (status) {
            case DRAFT -> throw OnlyDustException.internalServerError("Committee status DRAFT is not allowed here");
            case CLOSED -> CommitteeStatus.CLOSED;
            case OPEN_TO_VOTES -> CommitteeStatus.OPEN_TO_VOTES;
            case OPEN_TO_APPLICATIONS -> CommitteeStatus.OPEN_TO_APPLICATIONS;
        };
    }

    static Optional<BigDecimal> averageScoreOf(final @NonNull List<CommitteeJuryVoteViewEntity> votes) {
        return votes.stream()
                .filter(vote -> vote.getScore() != null)
                .mapToInt(CommitteeJuryVoteViewEntity::getScore)
                .average().stream()
                .mapToObj(BigDecimal::valueOf)
                .findFirst();
    }
}
