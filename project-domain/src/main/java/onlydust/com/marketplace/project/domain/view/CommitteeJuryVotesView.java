package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public record CommitteeJuryVotesView(@NonNull Committee.Status status, @NonNull List<JuryVoteView> votes,
                                     ProjectInfosView projectInfosView, @NonNull Boolean hasStartedToVote) {
}
