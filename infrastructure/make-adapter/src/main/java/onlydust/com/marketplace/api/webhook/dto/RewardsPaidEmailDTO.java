package onlydust.com.marketplace.api.webhook.dto;


import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
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

    public static RewardsPaidEmailDTO from(@NotNull final String email, @NotNull final List<RewardView> rewardViews) {
        return RewardsPaidEmailDTO.builder()
                .recipientEmail(email)
                .recipientName(isNull(rewardViews.get(0).billingProfileAdmin().adminName()) ? rewardViews.get(0).billingProfileAdmin().adminGithubLogin() :
                        rewardViews.get(0).billingProfileAdmin().adminName())
                .rewardNames(String.join("<br>", rewardViews.stream()
                        .map(r -> String.join(" - ", RewardId.of(r.id()).pretty(), r.projectName(), r.money().currencyCode(), r.money().amount().toString()))
                        .toList()))
                .build();
    }
}
