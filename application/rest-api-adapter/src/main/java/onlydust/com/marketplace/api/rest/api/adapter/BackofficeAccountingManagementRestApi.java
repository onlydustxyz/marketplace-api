package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapAccountToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapReceiptToTransaction;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@RestController
@Tags(@Tag(name = "BackofficeAccountingManagement"))
@AllArgsConstructor
public class BackofficeAccountingManagementRestApi implements BackofficeAccountingManagementApi {
    private final AccountingFacadePort accountingFacadePort;


    @Override
    public ResponseEntity<AccountResponse> createSponsorAccount(UUID sponsorId, CreateAccountRequest createAccountRequest) {
        final var sponsorAccountStatement = accountingFacadePort.createSponsorAccount(SponsorId.of(sponsorId),
                Currency.Id.of(createAccountRequest.getCurrencyId()),
                PositiveAmount.of(createAccountRequest.getAllowance()),
                createAccountRequest.getLockedUntil());

        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountListResponse> getSponsorAccounts(UUID sponsorId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<AccountResponse> registerTransactionReceipt(UUID accountId, RegisterTransactionReceiptRequest registerTransactionReceiptRequest) {
        final var receipt = registerTransactionReceiptRequest.getReceipt();
        final var sponsorAccountStatement = accountingFacadePort.fund(SponsorAccount.Id.of(accountId), mapReceiptToTransaction(receipt));
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountResponse> removeTransactionReceipt(UUID accountId, String reference) {
        final var sponsorAccountStatement = accountingFacadePort.deleteTransaction(SponsorAccount.Id.of(accountId), reference);
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountResponse> updateAccountAllowance(UUID accountId, UpdateAccountAllowanceRequest updateAccountAllowanceRequest) {
        final var sponsorAccountStatement = accountingFacadePort.increaseAllowance(SponsorAccount.Id.of(accountId),
                Amount.of(updateAccountAllowanceRequest.getAllowance()));
        return ResponseEntity.ok(mapAccountToResponse(sponsorAccountStatement));
    }

    @Override
    public ResponseEntity<AccountResponse> updateAccountAttributes(UUID accountId, UpdateAccountRequest updateAccountRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Void> allocateBudgetToProject(UUID projectId, ProjectBudgetAllocationRequest request) {
        final var sponsorAccount = accountingFacadePort.getSponsorAccount(SponsorAccount.Id.of(request.getSponsorAccountId()))
                .orElseThrow(() -> badRequest("Sponsor account %s not found".formatted(request.getSponsorAccountId())));

        accountingFacadePort.transfer(SponsorAccount.Id.of(request.getSponsorAccountId()),
                ProjectId.of(projectId),
                PositiveAmount.of(request.getAmount()),
                sponsorAccount.currency().id());

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unallocateBudgetFromProject(UUID projectId, ProjectBudgetAllocationRequest request) {
        final var sponsorAccount = accountingFacadePort.getSponsorAccount(SponsorAccount.Id.of(request.getSponsorAccountId()))
                .orElseThrow(() -> badRequest("Sponsor account %s not found".formatted(request.getSponsorAccountId())));

        accountingFacadePort.refund(ProjectId.of(projectId),
                SponsorAccount.Id.of(request.getSponsorAccountId()),
                PositiveAmount.of(request.getAmount()),
                sponsorAccount.currency().id());

        return ResponseEntity.noContent().build();
    }

}
