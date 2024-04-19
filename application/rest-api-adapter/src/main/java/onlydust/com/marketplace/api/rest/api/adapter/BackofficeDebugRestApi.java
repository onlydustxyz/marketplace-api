package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeDebugApi;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "BackofficeDebug"))
@AllArgsConstructor
public class BackofficeDebugRestApi implements BackofficeDebugApi {

    private final AccountBookEventStorage accountBookEventStorage;
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    @Transactional
    public ResponseEntity<Void> checkAccountingEvents() {
        final var currencies = currencyFacadePort.listCurrencies();
        final var cachedAccountBookProvider = new CachedAccountBookProvider(accountBookEventStorage);
        currencies.forEach(cachedAccountBookProvider::get);
        return ResponseEntity.noContent().build();
    }
}
