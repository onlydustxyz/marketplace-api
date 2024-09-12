package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.RequestRewardCommand;
import onlydust.com.marketplace.project.domain.port.input.RewardFacadePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.IntStream;

@Service
public class RewardHelper {
    @Autowired
    private RewardFacadePort rewardFacadePort;
    private final Faker faker = new Faker();

    public RewardId create(ProjectId projectId, UserAuthHelper.AuthenticatedUser lead, GithubUserId recipientId, long amount, Currency.Id currencyId) {
        return rewardFacadePort.createReward(UserId.of(lead.user().getId()),
                RequestRewardCommand.builder()
                        .amount(BigDecimal.valueOf(amount))
                        .currencyId(CurrencyView.Id.of(currencyId.value()))
                        .recipientId(recipientId.value())
                        .items(IntStream.range(0, 2).mapToObj(this::createRequestRewardCommandItem).toList())
                        .projectId(projectId)
                        .build());
    }

    private RequestRewardCommand.Item createRequestRewardCommandItem(int i) {
        return RequestRewardCommand.Item.builder()
                .id(String.valueOf(faker.random().nextLong()))
                .number((long) i)
                .repoId(faker.random().nextLong())
                .type(RequestRewardCommand.Item.Type.pullRequest)
                .build();
    }

    public void cancel(ProjectId projectId, UserAuthHelper.AuthenticatedUser lead, RewardId rewardId) {
        rewardFacadePort.cancelReward(UserId.of(lead.user().getId()), projectId, rewardId);
    }
}
