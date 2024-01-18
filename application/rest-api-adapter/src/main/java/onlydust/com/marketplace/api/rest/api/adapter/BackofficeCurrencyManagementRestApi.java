package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeCurrencyManagementApi;
import onlydust.com.backoffice.api.contract.model.CurrencyRequest;
import onlydust.com.backoffice.api.contract.model.CurrencyResponse;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapBlockchain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapCurrencyResponse;

@RestController
@Tags(@Tag(name = "Backoffice"))
@AllArgsConstructor
public class BackofficeCurrencyManagementRestApi implements BackofficeCurrencyManagementApi {
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public ResponseEntity<CurrencyResponse> createCurrency(CurrencyRequest request) {
        final var currency = switch (request.getType()) {
            case CRYPTO -> request.getStandard() == null
                    ? currencyFacadePort.addNativeCryptocurrencySupport(Currency.Code.of(request.getCode()), request.getDecimals())
                    : switch (request.getStandard()) {
                case ERC20 -> currencyFacadePort.addERC20Support(mapBlockchain(request.getBlockchain()), Ethereum.contractAddress(request.getAddress()));
                default -> throw OnlyDustException.badRequest("Standard %s is not supported for type %s".formatted(request.getStandard(), request.getType()));
            };

            default -> throw OnlyDustException.badRequest("Currency type %s is not supported".formatted(request.getType()));
        };

        return ResponseEntity.ok(mapCurrencyResponse(currency));
    }
}
