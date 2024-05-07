package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.LanguageRepository;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeLanguageApiIT extends AbstractMarketplaceBackOfficeApiIT {

    final static MutableObject<String> languageId1 = new MutableObject<>();

    @Autowired
    LanguageRepository languageRepository;

    UserAuthHelper.AuthenticatedBackofficeUser pierre;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER));
    }

    @Test
    @Order(1)
    void should_create_language() {
        languageRepository.deleteAll();

        client
                .post()
                .uri(getApiURI(LANGUAGES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + pierre.jwt())
                .bodyValue("""
                        {
                            "name": "Alphabetic",
                            "fileExtensions": ["a", "B", "c"]
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").value(languageId1::setValue)
                .json("""
                        {
                            "name": "Alphabetic",
                            "fileExtensions": ["a", "b", "c"],
                            "logoUrl": null,
                            "bannerUrl": null
                        }
                        """);

        assertThat(UUID.fromString(languageId1.getValue())).isNotNull();
    }

    @Test
    @Order(2)
    void should_get_languages() {
        client
                .get()
                .uri(getApiURI(LANGUAGES))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "languages": [
                            {
                              "name": "Alphabetic",
                              "fileExtensions": [
                                "a",
                                "b",
                                "c"
                              ],
                              "logoUrl": null,
                              "bannerUrl": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(3)
    void should_update_language() {
        client
                .put()
                .uri(getApiURI(LANGUAGES_BY_ID.formatted(languageId1.getValue())))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + pierre.jwt())
                .bodyValue("""
                        {
                            "name": "Alphabeticcc",
                            "fileExtensions": ["b", "C", "d"],
                            "logoUrl": "https://example.com/logo.png",
                            "bannerUrl": "https://example.com/banner.png"
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").value(languageId1::setValue)
                .json("""
                        {
                            "name": "Alphabeticcc",
                            "fileExtensions": ["b", "c", "d"],
                            "logoUrl": "https://example.com/logo.png",
                            "bannerUrl": "https://example.com/banner.png"
                        }
                        """);


        client
                .get()
                .uri(getApiURI(LANGUAGES))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "languages": [
                            {
                              "name": "Alphabeticcc",
                              "fileExtensions": [
                                "b",
                                "c",
                                "d"
                              ],
                              "logoUrl": "https://example.com/logo.png",
                              "bannerUrl": "https://example.com/banner.png"
                            }
                          ]
                        }
                        """);
    }


    @Test
    @Order(10)
    void should_create_another_language() {
        client
                .post()
                .uri(getApiURI(LANGUAGES))
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer " + pierre.jwt())
                .bodyValue("""
                        {
                            "name": "Foo",
                            "fileExtensions": []
                        }
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.id").value(languageId1::setValue)
                .json("""
                        {
                            "name": "Foo",
                            "fileExtensions": [],
                            "logoUrl": null,
                            "bannerUrl": null
                        }
                        """);


        client
                .get()
                .uri(getApiURI(LANGUAGES))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .json("""
                        {
                          "languages": [
                            {
                              "name": "Alphabeticcc",
                              "fileExtensions": [
                                "b",
                                "c",
                                "d"
                              ],
                              "logoUrl": "https://example.com/logo.png",
                              "bannerUrl": "https://example.com/banner.png"
                            },{
                              "name": "Foo",
                              "fileExtensions": [],
                              "logoUrl": null,
                              "bannerUrl": null
                            }
                          ]
                        }
                        """);
    }

    @Test
    @Order(20)
    void should_get_all_known_extensions() {
        client
                .get()
                .uri(getApiURI(LANGUAGES_EXTENSIONS))
                .header("Authorization", "Bearer " + pierre.jwt())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.knownExtensions[?(@.extension == 'a')]").doesNotExist()
                .jsonPath("$.knownExtensions[?(@.extension == 'b')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'b')].language.name").isEqualTo("Alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'c')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'c')].language.name").isEqualTo("Alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'd')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'd')].language.name").isEqualTo("Alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'rs')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'rs')].language").isEqualTo(null)
                .jsonPath("$.knownExtensions[?(@.extension == 'java')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'java')].language").isEqualTo(null);
    }
}
