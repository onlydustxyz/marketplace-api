package onlydust.com.marketplace.api.bootstrap.it.api;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagProject;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.CustomIgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.IgnoredContributionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomIgnoredContributionsRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.IgnoredContributionsRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;


@TagProject
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectIgnoreContributionsIT extends AbstractMarketplaceApiIT {


    @Autowired
    IgnoredContributionsRepository ignoredContributionsRepository;
    @Autowired
    CustomIgnoredContributionsRepository customIgnoredContributionsRepository;

    @Test
    @Order(1)
    public void should_ignore_contributions() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToIgnore": [
                            "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91",
                            "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        final var ignoredContributions = ignoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(ignoredContributions).containsExactlyInAnyOrder(
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId,
                        "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91")),
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId,
                        "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"))
        );

        final var customIgnoredContributions = customIgnoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(customIgnoredContributions).containsExactlyInAnyOrder(
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"
                        ),
                        true
                ),
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                        ),
                        true
                )
        );
    }

    @Test
    @Order(2)
    public void should_unignore_contribution() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToUnignore": [
                            "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        final var ignoredContributions = ignoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(ignoredContributions).containsExactlyInAnyOrder(
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId,
                        "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"))
        );

        final var customIgnoredContributions = customIgnoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(customIgnoredContributions).containsExactlyInAnyOrder(
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"
                        ),
                        true
                ),
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                        ),
                        false
                )
        );
    }

    @Test
    @Order(3)
    public void should_ignore_and_unignore_contributions_all_together() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToIgnore": [
                            "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                          ],
                          "contributionsToUnignore": [
                            "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        final var ignoredContributions = ignoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(ignoredContributions).containsExactlyInAnyOrder(
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId,
                        "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"))
        );

        final var customIgnoredContributions = customIgnoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(customIgnoredContributions).containsExactlyInAnyOrder(
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"
                        ),
                        false
                ),
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                        ),
                        true
                )
        );
    }

    @Test
    @Order(4)
    public void should_do_nothing_when_lists_are_empty() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticatePierre().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToIgnore": [
                          ],
                          "contributionsToUnignore": [
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        // Then
        final var ignoredContributions = ignoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(ignoredContributions).containsExactlyInAnyOrder(
                new IgnoredContributionEntity(new IgnoredContributionEntity.Id(projectId,
                        "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"))
        );

        final var customIgnoredContributions = customIgnoredContributionsRepository.findAllByProjectId(projectId);
        assertThat(customIgnoredContributions).containsExactlyInAnyOrder(
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "070e5317dfaa3eff83f7467824718cd048a1ed1c6338856b6fc4bc255c1a1a91"
                        ),
                        false
                ),
                new CustomIgnoredContributionEntity(
                        new CustomIgnoredContributionEntity.Id(projectId,
                                "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                        ),
                        true
                )
        );
    }

    @Test
    @Order(10)
    public void should_return_403_when_caller_is_not_leader() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAuthHelper.authenticateOlivier().jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToIgnore": [
                            "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isForbidden();
    }

    @Test
    @Order(11)
    public void should_return_401_when_caller_is_not_authenticated() {
        // Given
        UUID projectId = UUID.fromString("f39b827f-df73-498c-8853-99bc3f562723"); // Yolo croute

        // When
        client.patch()
                .uri(getApiURI(format(PROJECTS_IGNORED_CONTRIBUTIONS_PUT, projectId)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "contributionsToIgnore": [
                            "1c1c1d320997eeba0fabfc25b583fb763f6649867b997a49dad16d5c52eebd13"
                          ]
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .isUnauthorized();
    }

}
