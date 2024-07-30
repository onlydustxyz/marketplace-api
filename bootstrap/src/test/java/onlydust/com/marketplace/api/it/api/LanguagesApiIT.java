package onlydust.com.marketplace.api.it.api;


import onlydust.com.marketplace.api.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;

public class LanguagesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_get_all_languages() {
        // Given
        final UserAuthHelper.AuthenticatedUser authenticatedUser = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(getApiURI(LANGUAGES))
                .header("Authorization", "Bearer " + authenticatedUser.jwt())
                // Then
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "languages": [
                            {
                              "id": "ca600cac-0f45-44e9-a6e8-25e21b0c6887",
                              "slug": "rust",
                              "name": "Rust",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-rust.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-rust.png"
                            },
                            {
                              "id": "f57d0866-89f3-4613-aaa2-32f4f4ecc972",
                              "slug": "cairo",
                              "name": "Cairo",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-cairo.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-cairo.png"
                            },
                            {
                              "id": "69eba92e-104c-4d3e-8721-ad6a5fa5ea5a",
                              "slug": "noir",
                              "name": "Noir",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-noir.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-noir.png"
                            },
                            {
                              "id": "1109d0a2-1143-4915-a9c1-69e8be6c1bea",
                              "slug": "javascript",
                              "name": "Javascript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-javascript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-javascript.png"
                            },
                            {
                              "id": "e1842c39-fcfa-4289-9b5e-61bf50386a72",
                              "slug": "python",
                              "name": "Python",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-python.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-python.png"
                            },
                            {
                              "id": "c83881b3-5aef-4819-9596-fdbbbedf2b0b",
                              "slug": "go",
                              "name": "Go",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-go.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-go.png"
                            },
                            {
                              "id": "7ddd9417-4cf1-4c08-8040-9380dc6889e2",
                              "slug": "zig",
                              "name": "Zig",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-zig.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-zig.png"
                            },
                            {
                              "id": "6b3f8a21-8ae9-4f73-81df-06aeaddbaf42",
                              "slug": "java",
                              "name": "Java",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-java.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-java.png"
                            },
                            {
                              "id": "e0321e59-8633-46ee-bfbc-b1d84d845f83",
                              "slug": "ruby",
                              "name": "Ruby",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-ruby.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-ruby.png"
                            },
                            {
                              "id": "f90df4d9-b9f1-4800-b824-953abfbdd916",
                              "slug": "kotlin",
                              "name": "Kotlin",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-kotlin.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-kotlin.png"
                            },
                            {
                              "id": "d69b6d3e-f583-4c98-92d0-99a56f6f884a",
                              "slug": "solidity",
                              "name": "Solidity",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-solidity.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-solidity.png"
                            },
                            {
                              "id": "26faff45-8b1e-4b66-96df-b5499aac93c3",
                              "slug": "swift",
                              "name": "Swift",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-swift.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-swift.png"
                            },
                            {
                              "id": "75ce6b37-8610-4600-8d2d-753b50aeda1e",
                              "slug": "typescript",
                              "name": "Typescript",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-typescript.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-Typescript.png"
                            },
                            {
                              "id": "c607659b-7cca-45df-b478-33e427a96827",
                              "slug": "cpp",
                              "name": "C++",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-c-plus.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-c-plus.png"
                            },
                            {
                              "id": "34203c08-6749-4a20-b87d-442db82aa65c",
                              "slug": "csharp",
                              "name": "C#",
                              "logoUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-logo-c-sharp.png",
                              "bannerUrl": "https://od-metadata-assets-develop.s3.eu-west-1.amazonaws.com/languages-banner-c-sharp.png"
                            }
                          ]
                        }
                        """);
    }
}
