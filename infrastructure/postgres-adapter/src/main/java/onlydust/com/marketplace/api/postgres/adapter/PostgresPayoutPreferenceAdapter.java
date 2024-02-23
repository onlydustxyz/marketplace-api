package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutPreferenceViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
public class PostgresPayoutPreferenceAdapter implements PayoutPreferenceStoragePort {

    private final PayoutPreferenceRepository payoutPreferenceRepository;
    private final PayoutPreferenceViewRepository payoutPreferenceViewRepository;
    private final RewardRepository rewardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PayoutPreferenceView> findAllByUserId(UserId userId) {
        final List<PayoutPreferenceViewEntity> allForUser = payoutPreferenceViewRepository.findAllForUser(userId.value());
        return allForUser
                .stream()
                .map(PayoutPreferenceViewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReceivedSomeRewardsOnProject(UserId userId, ProjectId projectId) {
        return rewardRepository.countAllByRecipientIdAndProjectId(userId.value(), projectId.value()) >= 1;
    }

    @Override
    @Transactional
    public void save(ProjectId projectId, BillingProfile.Id billingProfileId, UserId userId) {
        payoutPreferenceRepository.save(PayoutPreferenceEntity.fromDomain(projectId, billingProfileId, userId));
    }
}
