package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.BudgetAllocationRequest;
import onlydust.com.backoffice.api.contract.model.FundsRequest;
import onlydust.com.backoffice.api.contract.model.TransactionRequest;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapTransactionNetwork;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public ResponseEntity<Void> allocateBudgetToProject(UUID projectUuid, BudgetAllocationRequest request) {
        final var sponsorId = SponsorId.of(request.getSponsorId());
        final var amount = PositiveAmount.of(request.getAmount());
        final var currencyId = Currency.Id.of(request.getCurrencyId());
        final var projectId = ProjectId.of(projectUuid);

        accountingFacadePort.transfer(sponsorId, projectId, amount, currencyId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> fundsSponsor(UUID sponsorUuid, FundsRequest request) {
        final var sponsorId = SponsorId.of(sponsorUuid);
        final var amount = PositiveAmount.of(request.getAmount());
        final var currencyId = Currency.Id.of(request.getCurrencyId());

        switch (request.getType()) {
            case CREDIT -> accountingFacadePort.mint(sponsorId, amount, currencyId);
            case DEBIT -> accountingFacadePort.burn(sponsorId, amount, currencyId);
        }

        if (request.getReceipt() != null)
            registerTransactionForSponsor(sponsorUuid, new TransactionRequest()
                    .amount(request.getAmount())
                    .currencyId(request.getCurrencyId())
                    .lockedUntil(request.getLockedUntil())
                    .receipt(request.getReceipt())
                    .type(request.getType()
                    ));

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> refundSponsorFromProject(UUID projectUuid, BudgetAllocationRequest request) {
        final var sponsorId = SponsorId.of(request.getSponsorId());
        final var amount = PositiveAmount.of(request.getAmount());
        final var currencyId = Currency.Id.of(request.getCurrencyId());
        final var projectId = ProjectId.of(projectUuid);

        accountingFacadePort.refund(projectId, sponsorId, amount, currencyId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> registerTransactionForSponsor(UUID sponsorUuid, TransactionRequest request) {
        final var sponsorId = SponsorId.of(sponsorUuid);
        final var amount = PositiveAmount.of(request.getAmount());
        final var currencyId = Currency.Id.of(request.getCurrencyId());
        final var network = mapTransactionNetwork(request.getReceipt().getNetwork());

        switch (request.getType()) {
            case CREDIT -> accountingFacadePort.fund(sponsorId, amount, currencyId, network, request.getLockedUntil());
            case DEBIT -> accountingFacadePort.withdraw(sponsorId, amount, currencyId, network);
        }

        return ResponseEntity.noContent().build();
    }
}
