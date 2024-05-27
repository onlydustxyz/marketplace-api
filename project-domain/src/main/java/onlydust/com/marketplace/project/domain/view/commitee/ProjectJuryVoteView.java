package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;

import java.math.BigDecimal;
import java.util.List;

public record ProjectJuryVoteView(@NonNull RegisteredContributorLinkView user, @NonNull List<VoteView> voteViews) {

    public BigDecimal getTotalScore(){
        return null;
    }
}
