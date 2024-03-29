package onlydust.com.marketplace.api.webhook.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Builder
@Data
public class RewardsPaidEmailDTO {
    @NonNull
    String recipientEmail;
    @NonNull
    String recipientName;
    @NonNull
    String rewardNames;

    public static RewardsPaidEmailDTO from(@NotNull final String email, @NotNull final List<RewardDetailsView> rewardViews) {
        return RewardsPaidEmailDTO.builder()
                .recipientEmail(email)
                .recipientName(rewardViews.get(0).invoice().createdBy().name())
                .rewardNames(String.join("<br>", rewardViews.stream()
                        .map(r -> String.join(" - ", r.id().pretty(), r.project().name(), r.money().currency().code().toString(),
                                r.money().amount().toString()))
                        .toList()))
                .build();
    }
}
