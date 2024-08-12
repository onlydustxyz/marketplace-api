package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import lombok.Data;
import onlydust.com.backoffice.api.contract.BackofficeDebugApi;
import onlydust.com.backoffice.api.contract.model.ReplaceAndResetUserRequest;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookEventStorage;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.user.domain.port.input.AppUserFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@RestController
@Tags(@Tag(name = "BackofficeDebug"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeDebugRestApi implements BackofficeDebugApi {

    private final AccountBookEventStorage accountBookEventStorage;
    private final AccountBookStorage accountBookStorage;
    private final CurrencyFacadePort currencyFacadePort;
    private final AppUserFacadePort appUserFacadePort;
    private final DebugProperties debugProperties;

    @Override
    @Transactional
    public ResponseEntity<Void> checkAccountingEvents() {
        final var currencies = currencyFacadePort.listCurrencies();
        final var cachedAccountBookProvider = new CachedAccountBookProvider(accountBookEventStorage, accountBookStorage, null);
        currencies.forEach(cachedAccountBookProvider::get);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetAndReplaceUser(ReplaceAndResetUserRequest replaceAndResetUserRequest) {
        if (!debugProperties.environment.equals("develop")) {
            throw internalServerError("User can be reset only on the develop environment");
        }
        appUserFacadePort.resetAndReplaceUser(replaceAndResetUserRequest.getUserId(), replaceAndResetUserRequest.getNewGithubLogin(),
                replaceAndResetUserRequest.getGithubOAuthAppId(), replaceAndResetUserRequest.getGithubOAuthAppSecret());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class DebugProperties {
        String environment;
    }
}
