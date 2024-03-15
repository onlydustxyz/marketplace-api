package onlydust.com.marketplace.api.webhook.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Objects.isNull;

@Builder
@Data
public class RewardsPaidEmailDTO {
    @NonNull
    String recipientEmail;
    @NonNull
    String recipientName;
    @NonNull
    String rewardNames;

    public static RewardsPaidEmailDTO from(@NotNull final String email, @NotNull final List<BackofficeRewardView> rewardViews) {
        return RewardsPaidEmailDTO.builder()
                .recipientEmail(email)
                .recipientName(isNull(rewardViews.get(0).billingProfileAdmin().admins().get(0).firstName()) ?
                        rewardViews.get(0).billingProfileAdmin().admins().get(0).login() :
                        rewardViews.get(0).billingProfileAdmin().admins().get(0).firstName())
                .rewardNames(String.join("<br>", rewardViews.stream()
                        .map(r -> String.join(" - ", r.id().pretty(), r.project().name(), r.money().currency().code().toString(),
                                r.money().amount().toString()))
                        .toList()))
                .build();
    }
}
