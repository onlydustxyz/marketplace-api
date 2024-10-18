package onlydust.com.marketplace.api.helper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RewardHelper {
    @Autowired
    private RewardFacadePort rewardFacadePort;
    @Autowired
    private RewardStoragePort rewardStoragePort;

    public RewardId create(ProjectId projectId, UserAuthHelper.AuthenticatedUser lead, GithubUserId recipientId, long amount, Currency.Id currencyId) {
        return rewardFacadePort.createReward(UserId.of(lead.user().getId()),
                RequestRewardCommand.builder()
                        .amount(BigDecimal.valueOf(amount))
                        .currencyId(CurrencyView.Id.of(currencyId.value()))
                        .recipientId(recipientId.value())
                        .items(List.of(createRequestRewardCommandItem()))
                        .projectId(projectId)
                        .build());
    }

    private RequestRewardCommand.Item createRequestRewardCommandItem() {
        return RequestRewardCommand.Item.builder()
                .id("1974448961")
                .number(77L)
                .repoId(86943508L)
                .type(RequestRewardCommand.Item.Type.issue)
                .build();
    }

    public void cancel(ProjectId projectId, UserAuthHelper.AuthenticatedUser lead, RewardId rewardId) {
        rewardFacadePort.cancelReward(UserId.of(lead.user().getId()), projectId, rewardId);
    }

    public Reward get(RewardId rewardId) {
        return rewardStoragePort.get(rewardId)
                .orElseThrow(() -> new RuntimeException("Reward not found"));
    }
}
