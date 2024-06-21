package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeAccountingReadApi;
import onlydust.com.backoffice.api.contract.model.AccountListResponse;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.read.repositories.SponsorAccountReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeAccountingReadApiPostgresAdapter implements BackofficeAccountingReadApi {
    private final SponsorAccountReadRepository sponsorAccountReadRepository;
    private final AccountingFacadePort accountingFacadePort;

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
