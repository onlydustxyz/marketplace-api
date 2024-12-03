package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.OnlyDustWallets;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.api.contract.ReadCurrencyApi;
import onlydust.com.marketplace.api.contract.model.OnlyDustWalletResponse;
import onlydust.com.marketplace.api.contract.model.SupportedCurrencyListResponse;
import onlydust.com.marketplace.api.contract.model.SupportedCurrencyResponse;
import onlydust.com.marketplace.api.read.cache.Cache;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.read.cache.Cache.M;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadCurrencyApiPostgresAdapter implements ReadCurrencyApi {

    private final Cache cache;
    private final CurrencyFacadePort currencyFacadePort;
    private final OnlyDustWallets onlyDustWallets;

    @Override
    public ResponseEntity<SupportedCurrencyListResponse> listCurrencies() {
        final var currencies = currencyFacadePort.listCurrencies();

        return ResponseEntity.ok()
                .cacheControl(cache.forEverybody(M))
                .body(new SupportedCurrencyListResponse().currencies(currencies.stream()
                        .map(c -> new SupportedCurrencyResponse()
                                .id(c.id().value())
                                .name(c.name())
                                .code(c.code().toString())
                                .decimals(c.decimals())
                                .logoUrl(c.logoUri().orElse(null))
                                .networks(c.supportedNetworks().stream()
                                        .sorted(comparing(Network::name))
                                        .map(BillingProfileMapper::map)
                                        .toList())
                                .onlyDustWallets(c.supportedNetworks().stream()
                                        .filter(n -> onlyDustWallets.get(n).isPresent())
                                        .sorted(comparing(Network::name))
                                        .map(n -> new OnlyDustWalletResponse()
                                                .network(BillingProfileMapper.map(n))
                                                .address(onlyDustWallets.get(n).orElse(null))
                                        )
                                        .toList())
                        )
                        .sorted(comparing(SupportedCurrencyResponse::getCode))
                        .toList()));
    }
}
