package onlydust.com.marketplace.api.it.bo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.LanguageRepository;
import onlydust.com.marketplace.api.suites.tags.TagBO;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;

@TagBO
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
                            "slug": "alphabetic",
                            "fileExtensions": ["a", "B", "c"],
                            "transparentLogoUrl": "https://example.com/logo-transparent.png",
                            "color": "#FF0000"
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
                            "slug": "alphabetic",
                            "fileExtensions": ["a", "b", "c"],
                            "logoUrl": null,
                            "transparentLogoUrl": "https://example.com/logo-transparent.png",
                            "bannerUrl": null,
                            "color": "#FF0000"
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
                              "slug": "alphabetic",
                              "fileExtensions": [
                                "a",
                                "b",
                                "c"
                              ],
                              "logoUrl": null,
                              "transparentLogoUrl": "https://example.com/logo-transparent.png",
                              "bannerUrl": null,
                              "color": "#FF0000"
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
                            "slug": "alphabeticcc",
                            "fileExtensions": ["b", "C", "d"],
                            "logoUrl": "https://example.com/logo.png",
                            "transparentLogoUrl": "https://example.com/logo-transparent-updated.png",
                            "bannerUrl": "https://example.com/banner.png",
                            "color": "#00FF00"
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
                            "slug": "alphabeticcc",
                            "fileExtensions": ["b", "c", "d"],
                            "logoUrl": "https://example.com/logo.png",
                            "transparentLogoUrl": "https://example.com/logo-transparent-updated.png",
                            "bannerUrl": "https://example.com/banner.png",
                            "color": "#00FF00"
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
                              "slug": "alphabeticcc",
                              "fileExtensions": [
                                "b",
                                "c",
                                "d"
                              ],
                              "logoUrl": "https://example.com/logo.png",
                              "transparentLogoUrl": "https://example.com/logo-transparent-updated.png",
                              "bannerUrl": "https://example.com/banner.png",
                              "color": "#00FF00"
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
                            "slug": "foo",
                            "fileExtensions": [],
                            "logoUrl": "https://example.com/foo-logo.png",
                            "transparentLogoUrl": "https://example.com/foo-logo-transparent.png",
                            "bannerUrl": "https://example.com/foo-banner.png",
                            "color": "#123456"
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
                            "slug": "foo",
                            "fileExtensions": [],
                            "logoUrl": "https://example.com/foo-logo.png",
                            "transparentLogoUrl": "https://example.com/foo-logo-transparent.png",
                            "bannerUrl": "https://example.com/foo-banner.png",
                            "color": "#123456"
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
                              "slug": "alphabeticcc",
                              "fileExtensions": [
                                "b",
                                "c",
                                "d"
                              ],
                              "logoUrl": "https://example.com/logo.png",
                              "transparentLogoUrl": "https://example.com/logo-transparent-updated.png",
                              "bannerUrl": "https://example.com/banner.png",
                              "color": "#00FF00"
                            },{
                              "name": "Foo",
                              "slug": "foo",
                              "fileExtensions": [],
                              "logoUrl": "https://example.com/foo-logo.png",
                              "transparentLogoUrl": "https://example.com/foo-logo-transparent.png",
                              "bannerUrl": "https://example.com/foo-banner.png",
                              "color": "#123456"
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
                .jsonPath("$.knownExtensions[?(@.extension == 'b')].language.slug").isEqualTo("alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'c')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'c')].language.name").isEqualTo("Alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'c')].language.slug").isEqualTo("alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'd')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'd')].language.name").isEqualTo("Alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'd')].language.slug").isEqualTo("alphabeticcc")
                .jsonPath("$.knownExtensions[?(@.extension == 'rs')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'rs')].language").isEqualTo(null)
                .jsonPath("$.knownExtensions[?(@.extension == 'java')]").exists()
                .jsonPath("$.knownExtensions[?(@.extension == 'java')].language").isEqualTo(null);
    }
}
