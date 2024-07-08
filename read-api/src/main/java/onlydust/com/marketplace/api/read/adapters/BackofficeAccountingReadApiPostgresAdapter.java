package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingReadApi;
import onlydust.com.backoffice.api.contract.model.AccountListResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentDetailsResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentPageResponse;
import onlydust.com.backoffice.api.contract.model.BatchPaymentStatus;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.read.entities.billing_profile.BatchPaymentReadEntity;
import onlydust.com.marketplace.api.read.repositories.BatchPaymentReadRepository;
import onlydust.com.marketplace.api.read.repositories.SponsorAccountReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

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
        final var payment = batchPaymentReadRepository.findById(batchPaymentId)
                .orElseThrow(() -> notFound("Batch payment %s not found".formatted(batchPaymentId)));

        return ok(payment.toDetailsResponse());
    }

    @Override
    public ResponseEntity<BatchPaymentPageResponse> getBatchPayments(Integer pageIndex, Integer pageSize, List<BatchPaymentStatus> statuses) {
        final var page = batchPaymentReadRepository.findAllByStatusIn(statuses, PageRequest.of(pageIndex, pageSize));

        final var response = new BatchPaymentPageResponse()
                .batchPayments(page.getContent().stream().map(BatchPaymentReadEntity::toResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(HttpStatus.PARTIAL_CONTENT).body(response) : ok(response);
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
