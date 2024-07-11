package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.RewardStatusDataEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.RewardStatusRepository;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
public class PostgresRewardStatusAdapter implements RewardStatusStorage {
    private final RewardStatusRepository rewardStatusRepository;

    @Override
    @Transactional
    public void persist(@NonNull RewardStatusData rewardStatusData) {
        rewardStatusRepository.save(RewardStatusDataEntity.of(rewardStatusData));
    }

    @Override
    @Transactional
    public void updateInvoiceReceivedAt(@NonNull RewardId rewardId, ZonedDateTime invoiceReceivedAt) {
        rewardStatusRepository.updateInvoiceReceivedAt(rewardId.value(), DateMapper.ofNullable(invoiceReceivedAt));
    }

    @Override
    @Transactional
    public void updatePaidAt(@NonNull RewardId rewardId, ZonedDateTime paidAt) {
        rewardStatusRepository.updatePaidAt(rewardId.value(), DateMapper.ofNullable(paidAt));
    }

    @Override
    @Transactional
    public void updateUsdAmount(@NonNull RewardId rewardId, ConvertedAmount usdAmount) {
        rewardStatusRepository.updateUsdAmount(rewardId.value(), usdAmount.convertedAmount().getValue(), usdAmount.conversionRate());
    }

    @Override
    @Transactional
    public void updateAccountingData(@NonNull RewardId rewardId, @NonNull Boolean sponsorHasEnoughFund, ZonedDateTime unlockDate,
                                     @NonNull Set<Network> networks,
                                     ConvertedAmount usdAmount) {
        rewardStatusRepository.updateAccountingData(rewardId.value(),
                sponsorHasEnoughFund,
                DateMapper.ofNullable(unlockDate),
                networks.stream().map(NetworkEnumEntity::of).toArray(NetworkEnumEntity[]::new),
                usdAmount.convertedAmount().getValue(),
                usdAmount.conversionRate());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RewardStatusData> get(final @NonNull RewardId rewardId) {
        return rewardStatusRepository.findById(rewardId.value())
                .map(RewardStatusDataEntity::toRewardStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardStatusData> get(@NonNull List<RewardId> rewardIds) {
        return rewardStatusRepository.findAllById(rewardIds.stream().map(RewardId::value).toList())
                .stream().map(RewardStatusDataEntity::toRewardStatus).toList();
    }

    @Override
    @Transactional
    public void delete(RewardId rewardId) {
        rewardStatusRepository.deleteById(rewardId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardStatusData> notRequested() {
        return rewardStatusRepository.findNotRequested()
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RewardStatusData> notRequested(BillingProfile.Id billingProfileId) {
        return rewardStatusRepository.findNotRequestedByBillingProfile(billingProfileId.value())
                .stream()
                .map(RewardStatusDataEntity::toRewardStatus)
                .toList();
    }
}
