package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@TagProject
public class TechnologiesApiIT extends AbstractMarketplaceApiIT {
    @Test
    void should_get_all_technologies() {

        // When
        client.get()
                .uri(getApiURI(GET_ALL_TECHNOLOGIES))
                .accept(MediaType.APPLICATION_JSON)
                // Then
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "technologies": [
                            "TypeScript",
                            "JavaScript",
                            "Rust",
                            "Solidity",
                            "CSS",
                            "Python",
                            "Cairo",
                            "HTML",
                            "SCSS",
                            "Jupyter Notebook",
                            "Ruby",
                            "Dart",
                            "MDX",
                            "Shell",
                            "Scheme",
                            "C++",
                            "CMake",
                            "Dockerfile",
                            "Haskell",
                            "COBOL",
                            "Makefile",
                            "PLpgSQL",
                            "Swift",
                            "Kotlin",
                            "Jinja",
                            "C",
                            "PHP",
                            "Batchfile",
                            "Nix",
                            "Objective-C",
                            "Procfile"
                          ]
                        }
                                                
                        """);
    }
}
