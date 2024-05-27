package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record ProjectJuryVoteView(@NonNull RegisteredContributorLinkView user, @NonNull List<VoteView> voteViews) {

    public BigDecimal getTotalScore() {
        return voteViews.stream().map(VoteView::score).anyMatch(Objects::isNull) ? null :
                BigDecimal.valueOf(voteViews.stream().map(VoteView::score).reduce(Integer::sum).get())
                        .divide(BigDecimal.valueOf(voteViews.size()), 3, BigDecimal.ROUND_HALF_UP);
    }
}
