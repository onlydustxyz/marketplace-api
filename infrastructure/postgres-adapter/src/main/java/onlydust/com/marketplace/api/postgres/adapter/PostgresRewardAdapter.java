package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.BatchPayment;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.view.PayableRewardWithPayoutInfoView;
import onlydust.com.marketplace.accounting.domain.view.RewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.InvoiceRewardViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.PayableRewardWithPayoutInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.mapper.ProjectMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.InvoiceRewardViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.PayableRewardWithPayoutInfoViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ShortProjectViewEntityRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.UuidWrapper;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.model.Reward;
import onlydust.com.marketplace.project.domain.port.output.RewardStoragePort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresRewardAdapter implements RewardStoragePort, AccountingRewardStoragePort {

    private final ShortProjectViewEntityRepository shortProjectViewEntityRepository;
    private final InvoiceRewardViewRepository invoiceRewardViewRepository;
    private final PayableRewardWithPayoutInfoViewRepository payableRewardWithPayoutInfoViewRepository;

    @Override
    public void save(Reward reward) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public void delete(UUID rewardId) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public Optional<Reward> get(UUID rewardId) {
        throw OnlyDustException.internalServerError("Not implemented for v1");
    }

    @Override
    public List<Project> listProjectsByRecipient(Long githubUserId) {
        return shortProjectViewEntityRepository.listProjectsByRewardRecipient(githubUserId)
                .stream()
                .map(ProjectMapper::mapShortProjectViewToProject)
                .toList();
    }

    @Override
    public List<RewardView> searchRewards(List<Invoice.Status> statuses, List<Invoice.Id> invoiceIds) {
        return invoiceRewardViewRepository.findAllByInvoiceStatusesAndInvoiceIds(
                        statuses != null ? statuses.stream().map(Invoice.Status::toString).toList() : null,
                        invoiceIds != null ? invoiceIds.stream().map(Invoice.Id::value).toList() : null
                )
                .stream()
                .map(InvoiceRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    public List<RewardView> getInvoiceRewards(@NonNull Invoice.Id invoiceId) {
        return invoiceRewardViewRepository.findAllByInvoiceId(invoiceId.value())
                .stream()
                .map(InvoiceRewardViewEntity::toDomain)
                .toList();
    }

    @Override
    public List<PayableRewardWithPayoutInfoView> findPayableRewardsWithPayoutInfo(List<Invoice.Id> invoiceIds) {
        return payableRewardWithPayoutInfoViewRepository.findAllByInvoiceIds(invoiceIds.stream().map(UuidWrapper::value).toList())
                .stream().map(PayableRewardWithPayoutInfoViewEntity::toDomain)
                .toList();
    }

    @Override
    public void createBatchPayment(BatchPayment batchPayment) {

    }
}
