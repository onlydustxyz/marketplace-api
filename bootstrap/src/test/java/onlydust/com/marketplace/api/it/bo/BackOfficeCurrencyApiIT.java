package onlydust.com.marketplace.api.it.bo;

import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.CurrencyResponse;
import onlydust.com.backoffice.api.contract.model.CurrencyType;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.api.helper.AccountingHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TagBO
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCurrencyApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private AccountingHelper accountingHelper;

    @Autowired
    private HistoricalQuoteRepository historicalQuoteRepository;

    @Autowired
    private LatestQuoteRepository latestQuoteRepository;

    @Autowired
    private OldestQuoteRepository oldestQuoteRepository;

    @Autowired
    private ImageStoragePort imageStoragePort;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardStatusRepository rewardStatusRepository;

    @Autowired
    private AccountBookRepository accountBookRepository;

    @Autowired
    private AccountBookEventRepository accountBookEventRepository;

    @Autowired
    private AllTransactionRepository allTransactionRepository;

    @Autowired
    private CachedAccountBookProvider accountBookProvider;

    @Autowired
    private SponsorAccountRepository sponsorAccountRepository;

    @Autowired
    private ProjectAllowanceRepository projectAllowanceRepository;
    UserAuthHelper.AuthenticatedBackofficeUser camille;

    @Test
    @Order(0)
    void cleanup() {
        accountingHelper.clearAllQuotes();
        rewardStatusRepository.deleteAll();
        rewardRepository.deleteAll();
        accountBookEventRepository.deleteAll();
        allTransactionRepository.deleteAll();
        depositRepository.deleteAll();
        accountBookRepository.deleteAll();
        sponsorAccountRepository.deleteAll();
        projectAllowanceRepository.deleteAll();
        currencyRepository.deleteAll();
        accountBookProvider.evictAll();
    }

    @BeforeEach
    void setUp() {
        camille = userAuthHelper.authenticateCamille();
    }

    @Test
    @Order(1)
    void should_add_usd_support_when_no_currency() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "FIAT",
                            "code": "USD",
                            "decimals": 2
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
        ;
    }

    @Test
    @Order(2)
    void should_add_erc20_support_on_ethereum() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "ETHEREUM",
                            "address": "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.tokens[?(@.blockchain=='ETHEREUM')].address").isEqualTo("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
                .jsonPath("$.description").isEqualTo("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.")
        ;
    }

    @Test
    @Order(2)
    void should_add_erc20_support_on_optimism() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "OPTIMISM",
                            "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.tokens[?(@.blockchain=='OPTIMISM')].address").isEqualTo("0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
                .jsonPath("$.description").isEqualTo("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.")
        ;
    }

    @Test
    @Order(3)
    void should_reject_erc20_support_from_invalid_contract() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "ETHEREUM",
                            "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isNotFound()
        ;
    }

    @Test
    @Order(4)
    void should_add_erc20_support_on_starknet() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "STARKNET",
                            "address": "0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.tokens[?(@.blockchain=='STARKNET')].address").isEqualTo("0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
                .jsonPath("$.description").isEqualTo("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.")
        ;
    }

    @Test
    @Order(4)
    void should_add_coin_support_on_aptos() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "APTOS",
                            "address": "0xf22bede237a07e121b56d91a491eb7bcdfd1f5907926a9e58338f964a01b17fa::asset::USDC"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.tokens[?(@.blockchain=='APTOS')].address").isEqualTo("0xf22bede237a07e121b56d91a491eb7bcdfd1f5907926a9e58338f964a01b17fa::asset" +
                                                                                  "::USDC")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
        ;
    }

    @Test
    @Order(4)
    void should_add_asset_support_on_stellar() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "STELLAR",
                            "address": "CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.tokens[?(@.blockchain=='STELLAR')].address").isEqualTo("CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
        ;
    }

    @Test
    @Order(5)
    void should_add_native_cryptocurrency_support() {
        client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "code": "ETH",
                            "decimals": "18"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.standard").doesNotExist()
                .jsonPath("$.blockchain").doesNotExist()
                .jsonPath("$.address").doesNotExist()
                .jsonPath("$.name").isEqualTo("Ethereum")
                .jsonPath("$.code").isEqualTo("ETH")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png")
                .jsonPath("$.decimals").isEqualTo(18)
                .jsonPath("$.description").isEqualTo("Ethereum (ETH) is a cryptocurrency")
        ;
    }

    @SneakyThrows
    @Test
    @Order(6)
    void should_add_iso_currency_support() {
        when(imageStoragePort.storeImage(any(URI.class))).then(invocation -> {
            final var uri = invocation.getArgument(0, URI.class);
            return new URL("%s://s3.%s%s".formatted(uri.getScheme(), uri.getHost(), uri.getPath()));
        });

        final var response = client
                .post()
                .uri(getApiURI(CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "type": "FIAT",
                            "code": "EUR",
                            "logoUrl": "https://euro.io",
                            "description": "Euro is the European currency"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CurrencyResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(CurrencyType.FIAT);
        assertThat(response.getName()).isEqualTo("Euro");
        assertThat(response.getCode()).isEqualTo("EUR");
        assertThat(response.getLogoUrl()).isEqualTo(URI.create("https://s3.euro.io"));
        assertThat(response.getDecimals()).isEqualTo(2);
        assertThat(response.getDescription()).isEqualTo("Euro is the European currency");

        final var currencyId = response.getId();

        client
                .patch()
                .uri(getApiURI(CURRENCY.formatted(currencyId)))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "name": "Euro2",
                            "description": "Euro is the official currency of the European Union",
                            "logoUrl": "https://upload.wikimedia.org/Flag_of_Europe.svg.png",
                            "decimals": 3
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("FIAT")
                .jsonPath("$.name").isEqualTo("Euro2")
                .jsonPath("$.code").isEqualTo("EUR")
                .jsonPath("$.logoUrl").isEqualTo("https://s3.upload.wikimedia.org/Flag_of_Europe.svg.png")
                .jsonPath("$.decimals").isEqualTo(3)
                .jsonPath("$.description").isEqualTo("Euro is the official currency of the European Union")
        ;

        client
                .patch()
                .uri(getApiURI(CURRENCY.formatted(currencyId)))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + camille.jwt())
                .bodyValue("""
                        {
                            "countryRestrictions": ["USA", "BRA"]
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.countryRestrictions[0]").isEqualTo("BRA")
                .jsonPath("$.countryRestrictions[1]").isEqualTo("USA")
        ;
    }

    @SneakyThrows
    @Test
    @Order(90)
    void should_refresh_currency_quotes() {
        // Given
        final var quotes = historicalQuoteRepository.findAll().stream().map(q -> q.toBuilder().price(BigDecimal.ZERO).build()).toList();
        historicalQuoteRepository.saveAll(quotes);

        // When
        currencyFacadePort.refreshQuotes();

        // Then
        final var historicalQuotes = historicalQuoteRepository.findAll();
        assertThat(historicalQuotes).isNotEmpty();
        assertThat(historicalQuotes).allMatch(q -> q.getPrice().compareTo(BigDecimal.ZERO) > 0);

        final var latestQuotes = latestQuoteRepository.findAll();
        assertThat(latestQuotes).hasSize(4);
        assertThat(latestQuotes).allMatch(q -> q.price().compareTo(BigDecimal.ZERO) > 0);

        final var oldestQuotes = oldestQuoteRepository.findAll();
        assertThat(oldestQuotes).hasSize(4);
        assertThat(oldestQuotes).allMatch(q -> q.price().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Order(91)
    void should_list_all_supported_currencies() {
        client
                .get()
                .uri(getApiURI(CURRENCIES))
                .header("Authorization", "Bearer " + camille.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.currencies[*].id").isNotEmpty()
                .json("""
                        {
                          "currencies": [
                            {
                              "code": "ETH",
                              "name": "Ethereum",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
                              "decimals": 18,
                              "type": "CRYPTO",
                              "tokens": [],
                              "supportedOn": [
                                "ETHEREUM"
                              ],
                              "description": "Ethereum (ETH) is a cryptocurrency",
                              "countryRestrictions": []
                            },
                            {
                              "code": "EUR",
                              "name": "Euro2",
                              "logoUrl": "https://s3.upload.wikimedia.org/Flag_of_Europe.svg.png",
                              "decimals": 3,
                              "type": "FIAT",
                              "tokens": [],
                              "supportedOn": [
                                "SEPA"
                              ],
                              "description": "Euro is the official currency of the European Union",
                              "countryRestrictions": [
                                "BRA",
                                "USA"
                              ]
                            },
                            {
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "decimals": 2,
                              "type": "FIAT",
                              "tokens": [],
                              "supportedOn": [
                                "SEPA"
                              ],
                              "description": null,
                              "countryRestrictions": []
                            },
                            {
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "decimals": 6,
                              "type": "CRYPTO",
                              "tokens": [
                                {
                                  "blockchain": "STARKNET",
                                  "address": "0x053c91253bc9682c04929ca02ed00b3e423f6710d2ee7e0d5ebb06f3ecf368a8",
                                  "decimals": 6,
                                  "symbol": "USDC",
                                  "name": "USD Coin"
                                },
                                {
                                  "blockchain": "STELLAR",
                                  "address": "CCW67TSZV3SSS2HXMBQ5JFGCKJNXKZM7UQUWUZPUTHXSTZLEO7SJMI75",
                                  "decimals": 7,
                                  "symbol": "USDC",
                                  "name": "USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"
                                },
                                {
                                  "blockchain": "APTOS",
                                  "address": "0xf22bede237a07e121b56d91a491eb7bcdfd1f5907926a9e58338f964a01b17fa::asset::USDC",
                                  "decimals": 6,
                                  "symbol": "USDC",
                                  "name": "USD Coin"
                                },
                                {
                                  "blockchain": "ETHEREUM",
                                  "address": "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48",
                                  "decimals": 6,
                                  "symbol": "USDC",
                                  "name": "USD Coin"
                                },
                                {
                                  "blockchain": "OPTIMISM",
                                  "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85",
                                  "decimals": 6,
                                  "symbol": "USDC",
                                  "name": "USD Coin"
                                }
                              ],
                              "supportedOn": [
                                "STARKNET",
                                "STELLAR",
                                "APTOS",
                                "ETHEREUM",
                                "OPTIMISM"
                              ],
                              "description": "USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.",
                              "countryRestrictions": []
                            }
                          ]
                        }
                        """)
        ;
    }
}
