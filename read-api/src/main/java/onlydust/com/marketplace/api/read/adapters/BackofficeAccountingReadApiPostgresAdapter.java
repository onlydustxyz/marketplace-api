package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingReadApi;
import onlydust.com.backoffice.api.contract.model.AccountListResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentDetailsResponse;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.read.entities.reward.RewardReadEntity;
import onlydust.com.marketplace.api.read.repositories.BatchPaymentReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorAccountReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.read.mapper.NetworkMapper.map;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeAccountingReadApiPostgresAdapter implements BackofficeAccountingReadApi {
    private final SponsorAccountReadRepository sponsorAccountReadRepository;
    private final AccountingFacadePort accountingFacadePort;
    private final BatchPaymentReadRepository batchPaymentReadRepository;

    @Override
    public ResponseEntity<BatchPaymentDetailsResponse> getBatchPayment(UUID batchPaymentId) {
        final var bp = batchPaymentReadRepository.findById(batchPaymentId)
                .orElseThrow(() -> notFound("Batch payment %s not found".formatted(batchPaymentId)));

        final var response = new BatchPaymentDetailsResponse()
                .id(bp.id())
                .createdAt(bp.createdAt())
                .status(bp.status())
                .csv(bp.csv())
                .network(map(bp.network()))
                .transactionHash(bp.transactionHash())
                .rewardCount((long) bp.rewards().size())
                .rewards(bp.rewards().stream().map(RewardReadEntity::toShortResponse).toList())
                .totalsPerCurrency(bp.totalsPerCurrency())
                .totalUsdEquivalent(bp.totalUsdEquivalent());

        return ok(response);
    }

    @Override
    public ResponseEntity<AccountListResponse> getSponsorAccounts(UUID sponsorId) {
        final var accounts = sponsorAccountReadRepository.findAllBySponsorId(sponsorId, Sort.by("currency.code"));

        final var response = new AccountListResponse()
                .accounts(accounts.stream().map(account -> {
                            final var sponsorAccountStatement = accountingFacadePort.getSponsorAccountStatement(SponsorAccount.Id.of(account.id()))
                                    .orElseThrow(() -> internalServerError("Sponsor account %s not found".formatted(account.id())));
                            return account.toDto(sponsorAccountStatement);
                        })
                        .toList());

        return ok(response);
    }
}
