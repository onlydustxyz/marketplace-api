package onlydust.com.marketplace.api.github_api;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.github_api.adapters.GithubSearchApiAdapter;
import onlydust.com.marketplace.api.github_api.dto.GithubOrgaSearchResponseDTO;
import onlydust.com.marketplace.api.github_api.properties.GithubPaginationProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubSearchApiAdapterTest {

    private static final Faker faker = new Faker();


    @Test
    void should_return_organization_for_user_for_one_page() {
        // Given
        final GithubHttpClient httpClient = mock(GithubHttpClient.class);
        final GithubSearchApiAdapter githubSearchApiAdapter = new GithubSearchApiAdapter(httpClient,
                GithubPaginationProperties.builder().pageSize(2).build());
        final String githubPAT = faker.rickAndMorty().character();
        final GithubOrgaSearchResponseDTO orga1 = new GithubOrgaSearchResponseDTO();
        orga1.setId(1L);
        orga1.setUrl("https://api.github.com/orgs/foo1");

        // When
        when(httpClient.get("/user/orgs?per_page=2&page=1", GithubOrgaSearchResponseDTO[].class, githubPAT))
                .thenReturn(Optional.of(new GithubOrgaSearchResponseDTO[]{
                        orga1
                }));
        final List<GithubAccount> githubAccounts =
                githubSearchApiAdapter.searchOrganizationsByGithubPersonalToken(githubPAT);

        // Then
        assertEquals(1, githubAccounts.size());
        assertEquals(orga1.getId(), githubAccounts.get(0).getId());
        assertEquals(orga1.getUrl(), "https://api.github.com/orgs/foo1");
    }

    @Test
    void should_return_organization_for_user_for_two_pages() {
        // Given
        final GithubHttpClient httpClient = mock(GithubHttpClient.class);
        final GithubSearchApiAdapter githubSearchApiAdapter = new GithubSearchApiAdapter(httpClient,
                GithubPaginationProperties.builder().pageSize(2).build());
        final String githubPAT = faker.rickAndMorty().character();
        final GithubOrgaSearchResponseDTO orga1 = new GithubOrgaSearchResponseDTO();
        orga1.setId(1L);
        orga1.setUrl("https://api.github.com/orgs/foo1");
        final GithubOrgaSearchResponseDTO orga2 = new GithubOrgaSearchResponseDTO();
        orga2.setId(2L);
        orga2.setUrl("https://api.github.com/orgs/foo2");
        final GithubOrgaSearchResponseDTO orga3 = new GithubOrgaSearchResponseDTO();
        orga3.setId(3L);
        orga3.setUrl("https://api.github.com/orgs/foo3");


        // When
        when(httpClient.get("/user/orgs?per_page=2&page=1", GithubOrgaSearchResponseDTO[].class, githubPAT))
                .thenReturn(Optional.of(new GithubOrgaSearchResponseDTO[]{
                        orga1, orga2
                }));
        when(httpClient.get("/user/orgs?per_page=2&page=2", GithubOrgaSearchResponseDTO[].class, githubPAT))
                .thenReturn(Optional.of(new GithubOrgaSearchResponseDTO[]{
                        orga3
                }));
        final List<GithubAccount> githubAccounts =
                githubSearchApiAdapter.searchOrganizationsByGithubPersonalToken(githubPAT);

        // Then
        assertEquals(3, githubAccounts.size());
        assertEquals(orga1.getId(), githubAccounts.get(0).getId());
        assertEquals(orga2.getId(), githubAccounts.get(1).getId());
        assertEquals(orga3.getId(), githubAccounts.get(2).getId());
        assertEquals(orga1.getUrl(), "https://api.github.com/orgs/foo1");
        assertEquals(orga2.getUrl(), "https://api.github.com/orgs/foo2");
        assertEquals(orga3.getUrl(), "https://api.github.com/orgs/foo3");
    }


}
