package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeCurrencyManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapBlockchain;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapCurrencyResponse;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@RestController
@Tags(@Tag(name = "BackofficeCurrencyManagement"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeCurrencyManagementRestApi implements BackofficeCurrencyManagementApi {
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public ResponseEntity<CurrencyResponse> createCurrency(CurrencyCreateRequest request) {
        check(request);
        final var currency = switch (request.getType()) {
            case CRYPTO -> {
                if (request.getStandard() == null)
                    yield currencyFacadePort.addNativeCryptocurrencySupport(Currency.Code.of(request.getCode()), request.getDecimals());

                final var blockchain = mapBlockchain(request.getBlockchain());
                yield switch (blockchain) {
                    case ETHEREUM, OPTIMISM -> currencyFacadePort.addERC20Support(blockchain, Ethereum.contractAddress(request.getAddress()));
                    case STARKNET -> currencyFacadePort.addERC20Support(blockchain, StarkNet.contractAddress(request.getAddress()));
                    case APTOS -> currencyFacadePort.addERC20Support(blockchain, Aptos.coinType(request.getAddress()));
                    case STELLAR -> throw new UnsupportedOperationException("Stellar is not supported yet");
                };
            }

            case FIAT -> currencyFacadePort.addIsoCurrencySupport(Currency.Code.of(request.getCode()), request.getDescription(), request.getLogoUrl());
        };

        return ResponseEntity.ok(mapCurrencyResponse(currency));
    }

    private void check(CurrencyCreateRequest request) {
        required(request.getType(), "type");

        switch (request.getType()) {
            case CRYPTO -> {
                if (request.getStandard() == null) {
                    required(request.getDecimals(), "decimals");
                } else if (Objects.requireNonNull(request.getStandard()) == CurrencyStandard.ERC20) {
                    required(request.getAddress(), "address");
                    required(request.getBlockchain(), "blockchain");
                } else {
                    throw badRequest("Standard %s is not supported for type %s".formatted(request.getStandard(), request.getType()));
                }
            }
            case FIAT -> {
                required(request.getCode(), "code");
                if (Optional.ofNullable(request.getStandard()).orElse(CurrencyStandard.ISO4217) != CurrencyStandard.ISO4217) {
                    throw badRequest("Standard %s is not supported for type %s".formatted(request.getStandard(), request.getType()));
                }
            }
        }
    }

    private void required(Object value, String name) {
        if (value == null) throw badRequest("'%s' is required".formatted(name));
    }

    @Override
    public ResponseEntity<CurrencyResponse> updateCurrency(UUID id, CurrencyUpdateRequest request) {
        final var currency = currencyFacadePort.updateCurrency(
                Currency.Id.of(id),
                request.getName(),
                request.getDescription(),
                request.getLogoUrl(),
                request.getDecimals(),
                request.getCountryRestrictions()
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

        return ResponseEntity.ok(new CurrencyListResponse().currencies(currencies.stream()
                .map(BackOfficeMapper::mapCurrencyResponse)
                .sorted(comparing(CurrencyResponse::getCode))
                .toList()));
    }
}
