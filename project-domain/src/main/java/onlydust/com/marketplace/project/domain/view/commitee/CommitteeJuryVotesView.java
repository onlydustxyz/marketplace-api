package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.ProjectInfosView;

import java.util.List;

public record CommitteeJuryVotesView(@NonNull Committee.Status status, @NonNull List<VoteView> votes,
                                     ProjectInfosView projectInfosView, @NonNull Boolean hasStartedToVote) {
}
