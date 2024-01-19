package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeCurrencyManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapBlockchain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapCurrencyResponse;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@RestController
@Tags(@Tag(name = "Backoffice"))
@AllArgsConstructor
public class BackofficeCurrencyManagementRestApi implements BackofficeCurrencyManagementApi {
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public ResponseEntity<CurrencyResponse> createCurrency(CurrencyCreateRequest request) {
        final var currency = switch (request.getType()) {
            case CRYPTO -> request.getStandard() == null
                    ? currencyFacadePort.addNativeCryptocurrencySupport(Currency.Code.of(request.getCode()), request.getDecimals())
                    : switch (request.getStandard()) {
                case ERC20 -> currencyFacadePort.addERC20Support(mapBlockchain(request.getBlockchain()), Ethereum.contractAddress(request.getAddress()));
                default -> throw badRequest("Standard %s is not supported for type %s".formatted(request.getStandard(), request.getType()));
            };

            case FIAT -> switch (Optional.ofNullable(request.getStandard()).orElse(CurrencyStandard.ISO4217)) {
                case ISO4217 -> currencyFacadePort.addIsoCurrencySupport(Currency.Code.of(request.getCode()));
                default -> throw badRequest("Standard %s is not supported for type %s".formatted(request.getStandard(), request.getType()));
            };
        };

        return ResponseEntity.ok(mapCurrencyResponse(currency));
    }

    @Override
    public ResponseEntity<CurrencyResponse> updateCurrency(UUID id, CurrencyUpdateRequest request) {
        final var currency = currencyFacadePort.updateCurrency(
                Currency.Id.of(id),
                request.getName(),
                request.getDescription(),
                request.getLogoUrl(),
                request.getDecimals()
        );

        return ResponseEntity.ok(mapCurrencyResponse(currency));
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final URL imageUrl = currencyFacadePort.uploadLogo(imageInputStream);
        final var response = new UploadImageResponse().url(imageUrl.toString());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CurrencyListResponse> listCurrencies() {
        final var currencies = currencyFacadePort.listCurrencies();

        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream().map(BackOfficeMapper::mapCurrencyResponse).toList()));
    }
}
