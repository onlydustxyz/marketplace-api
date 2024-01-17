package onlydust.com.marketplace.api.bootstrap.it.bo;

import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class BackOfficeCurrencyApiIT extends AbstractMarketplaceBackOfficeApiIT {
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
                .jsonPath("$.blockchain").isEqualTo("ETHEREUM")
                .jsonPath("$.address").isEqualTo("0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48")
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
                .jsonPath("$.blockchain").isEqualTo("OPTIMISM")
                .jsonPath("$.address").isEqualTo("0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85")
                .jsonPath("$.name").isEqualTo("USD Coin")
                .jsonPath("$.code").isEqualTo("USDC")
                .jsonPath("$.logoUrl").isEqualTo("https://s2.coinmarketcap.com/static/img/coins/64x64/3408.png")
                .jsonPath("$.decimals").isEqualTo(6)
                .jsonPath("$.description").isEqualTo("USDC (USDC) is a cryptocurrency and operates on the Ethereum platform.")
        ;
    }
}
