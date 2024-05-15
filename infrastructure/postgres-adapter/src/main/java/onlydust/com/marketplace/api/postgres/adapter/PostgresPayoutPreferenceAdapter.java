package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayoutPreferenceView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayoutPreferenceQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ShortBillingProfileQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.PayoutPreferenceEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayoutPreferenceViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortBillingProfileViewRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class PostgresPayoutPreferenceAdapter implements PayoutPreferenceStoragePort {

    private final PayoutPreferenceRepository payoutPreferenceRepository;
    private final PayoutPreferenceViewRepository payoutPreferenceViewRepository;
    private final RewardRepository rewardRepository;
    private final ShortBillingProfileViewRepository shortBillingProfileViewRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PayoutPreferenceView> findAllByUserId(UserId userId) {
        final Map<BillingProfile.Id, ShortBillingProfileView> billingProfiles =
                shortBillingProfileViewRepository.findBillingProfilesForUserId(userId.value(), List.of())
                        .stream()
                        .map(ShortBillingProfileQueryEntity::toView)
                        .collect(Collectors.toMap(ShortBillingProfileView::getId, Function.identity()));
        final List<PayoutPreferenceQueryEntity> allForUser = payoutPreferenceViewRepository.findAllForUser(userId.value());
        return allForUser
                .stream()
                .map(preference -> preference.toDomain(
                                isNull(preference.getBillingProfileId())
                                        ? null
                                        : billingProfiles.get(BillingProfile.Id.of(preference.getBillingProfileId()))
                        )
                )
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
        payoutPreferenceRepository.saveAndFlush(PayoutPreferenceEntity.fromDomain(projectId, billingProfileId, userId));
    }
}
