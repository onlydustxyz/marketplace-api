package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import onlydust.com.marketplace.api.postgres.adapter.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeCurrencyApiIT extends AbstractMarketplaceBackOfficeApiIT {
    @Autowired
    private QuoteRepository quoteRepository;

    @Test
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
    void should_add_iso_currency_support() {
        final var response = client
                .post()
                .uri(getApiURI(POST_CURRENCIES))
                .contentType(APPLICATION_JSON)
                .header("Api-Key", apiKey())
                .bodyValue("""
                        {
                            "type": "FIAT",
                            "code": "EUR"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        response
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.type").isEqualTo("FIAT")
                .jsonPath("$.standard").isEqualTo("ISO4217")
                .jsonPath("$.blockchain").doesNotExist()
                .jsonPath("$.address").doesNotExist()
                .jsonPath("$.name").isEqualTo("Euro")
                .jsonPath("$.code").isEqualTo("EUR")
                .jsonPath("$.logoUrl").doesNotExist()
                .jsonPath("$.decimals").isEqualTo(2)
                .jsonPath("$.description").doesNotExist()
        ;

        final var currencyId = new ObjectMapper().readTree(response.returnResult().getResponseBody()).get("id").asText();

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
    void should_refresh_currency_quotes() {
        // Given
        final var quotes = quoteRepository.findAll().stream().map(q -> q.toBuilder().price(BigDecimal.ZERO).build()).toList();
        quoteRepository.saveAll(quotes);

        // When
        Thread.sleep(700);

        // Then
        final var xxx = quoteRepository.findAll();
        assertThat(quoteRepository.findAll()).allMatch(q -> q.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
