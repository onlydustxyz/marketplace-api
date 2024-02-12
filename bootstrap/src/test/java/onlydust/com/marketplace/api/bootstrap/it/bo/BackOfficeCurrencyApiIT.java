package onlydust.com.marketplace.api.bootstrap.it.bo;

import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.model.CurrencyResponse;
import onlydust.com.backoffice.api.contract.model.CurrencyStandard;
import onlydust.com.backoffice.api.contract.model.CurrencyType;
import onlydust.com.marketplace.api.postgres.adapter.repository.HistoricalQuoteRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCurrencyApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private HistoricalQuoteRepository historicalQuoteRepository;

    @Test
    @Order(1)
    void should_add_erc20_support_on_ethereum() {
        client
                .post()
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
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
                .jsonPath("$.standard").isEqualTo("ERC20")
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
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
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
                .jsonPath("$.standard").isEqualTo("ERC20")
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
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
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
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
                .bodyValue("""
                        {
                            "type": "CRYPTO",
                            "standard": "ERC20",
                            "blockchain": "STARKNET",
                            "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("CRYPTO")
                .jsonPath("$.standard").isEqualTo("ERC20")
                .jsonPath("$.tokens[?(@.blockchain=='STARKNET')].address").isEqualTo("0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
                .jsonPath("$.description").isEqualTo("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.")
        ;
    }


    @Test
    @Order(5)
    void should_add_native_cryptocurrency_support() {
        client
                .post()
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
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
        final var response = client
                .post()
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
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
        assertThat(response.getStandard()).isEqualTo(CurrencyStandard.ISO4217);
        assertThat(response.getName()).isEqualTo("Euro");
        assertThat(response.getCode()).isEqualTo("EUR");
        assertThat(response.getLogoUrl()).isEqualTo(URI.create("https://euro.io"));
        assertThat(response.getDecimals()).isEqualTo(2);
        assertThat(response.getDescription()).isEqualTo("Euro is the European currency");

        final var currencyId = response.getId();

        client
                .put()
                .uri(getApiURI(PUT_CURRENCIES.formatted(currencyId)))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
                .bodyValue("""
                        {
                            "name": "Euro2",
                            "description": "Euro is the official currency of the European Union",
                            "logoUrl": "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Flag_of_Europe.svg/1200px-Flag_of_Europe.svg.png",
                            "decimals": 3
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("FIAT")
                .jsonPath("$.standard").isEqualTo("ISO4217")
                .jsonPath("$.name").isEqualTo("Euro2")
                .jsonPath("$.code").isEqualTo("EUR")
                .jsonPath("$.logoUrl").isEqualTo("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Flag_of_Europe.svg/1200px-Flag_of_Europe.svg.png")
                .jsonPath("$.decimals").isEqualTo(3)
                .jsonPath("$.description").isEqualTo("Euro is the official currency of the European Union")
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
        Thread.sleep(700);

        // Then
        assertThat(historicalQuoteRepository.findAll()).allMatch(q -> q.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Order(91)
    void should_list_all_supported_currencies() {
        client
                .get()
                .uri(getApiURI(GET_CURRENCIES))
                .header("Api-Key", apiKey())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.currencies[*].id").isNotEmpty()
                .json("""
                        {
                          "currencies": [
                            {
                              "id": "3f6e1c98-8659-493a-b941-943a803bd91f",
                              "code": "BTC",
                              "name": "Bitcoin",
                              "logoUrl": null,
                              "type": "CRYPTO",
                              "standard": null,
                              "tokens": [],
                              "decimals": 8,
                              "description": null
                            },
                            {
                              "code": "ETH",
                              "name": "Ethereum",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
                              "type": "CRYPTO",
                              "standard": null,
                              "tokens": [],
                              "decimals": 18,
                              "description": "Ethereum (ETH) is a cryptocurrency"
                            },
                            {
                              "code": "EUR",
                              "name": "Euro2",
                              "logoUrl": "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Flag_of_Europe.svg/1200px-Flag_of_Europe.svg.png",
                              "type": "FIAT",
                              "standard": "ISO4217",
                              "tokens": [],
                              "decimals": 3,
                              "description": "Euro is the official currency of the European Union"
                            },
                            {
                              "code": "STRK",
                              "name": "StarkNet Token",
                              "logoUrl": null,
                              "type": "CRYPTO",
                              "standard": "ERC20",
                              "tokens": [
                                {
                                  "blockchain": "ETHEREUM",
                                  "address": "0xCa14007Eff0dB1f8135f4C25B34De49AB0d42766",
                                  "decimals": 18,
                                  "symbol": "STRK",
                                  "name": "StarkNet Token"
                                }
                              ],
                              "decimals": 18,
                              "description": null
                            },
                            {
                              "id": "f35155b5-6107-4677-85ac-23f8c2a63193",
                              "code": "USD",
                              "name": "US Dollar",
                              "logoUrl": null,
                              "type": "FIAT",
                              "standard": "ISO4217",
                              "tokens": [],
                              "decimals": 2,
                              "description": null
                            },
                            {
                              "code": "USDC",
                              "name": "USD Coin",
                              "logoUrl": "https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png",
                              "type": "CRYPTO",
                              "standard": "ERC20",
                              "tokens": [
                                {
                                  "blockchain": "OPTIMISM",
                                  "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85",
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
                                  "blockchain": "STARKNET",
                                  "address": "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85",
                                  "decimals": 6,
                                  "symbol": "USDC",
                                  "name": "USD Coin"
                                }
                              ],
                              "decimals": 6,
                              "description": "USDC (USDC) is a cryptocurrency and operates on the Ethereum platform."
                            }
                          ]
                        }                        
                        """)
        ;
    }
}
