package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadSearchApi;
import onlydust.com.marketplace.api.contract.model.SearchPostRequest;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import onlydust.com.marketplace.api.contract.model.SuggestPostRequest;
import onlydust.com.marketplace.api.contract.model.SuggestResponse;
import onlydust.com.marketplace.api.read.repositories.elasticsearch.ProjectFacet;
import onlydust.com.marketplace.api.read.repositories.elasticsearch.SearchRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadSearchApiElasticSearchAdapter implements ReadSearchApi {

    private final SearchRepository searchRepository;

    @Override
    public ResponseEntity<SearchResponse> search(SearchPostRequest searchPostRequest) {
        final Map<ProjectFacet, List<String>> facets = new HashMap<>();
        if (nonNull(searchPostRequest.getCategories())){
            facets.put(ProjectFacet.CATEGORIES, searchPostRequest.getCategories());
        }
        if (nonNull(searchPostRequest.getLanguages())){
            facets.put(ProjectFacet.LANGUAGES, searchPostRequest.getCategories());
        }
        if (nonNull(searchPostRequest.getEcosystems())){
            facets.put(ProjectFacet.ECOSYSTEMS, searchPostRequest.getCategories());
        }
        final SearchResponse searchResponse = searchRepository.searchAll(
                searchPostRequest.getKeyword(),
                searchPostRequest.getType(),
                facets,
                searchPostRequest.getPageIndex() * searchPostRequest.getPageSize(),
                searchPostRequest.getPageSize());
        return ResponseEntity.ok(searchResponse);
    }

    @Override
    public ResponseEntity<SuggestResponse> suggest(SuggestPostRequest suggestPostRequest) {
        final Map<ProjectFacet, List<String>> facets = new HashMap<>();
        if (nonNull(suggestPostRequest.getCategories())){
            facets.put(ProjectFacet.CATEGORIES, suggestPostRequest.getCategories());
        }
        if (nonNull(suggestPostRequest.getLanguages())){
            facets.put(ProjectFacet.LANGUAGES, suggestPostRequest.getCategories());
        }
        if (nonNull(suggestPostRequest.getEcosystems())){
            facets.put(ProjectFacet.ECOSYSTEMS, suggestPostRequest.getCategories());
        }
        final SuggestResponse SuggestResponse = searchRepository.suggest(suggestPostRequest.getKeyword(), suggestPostRequest.getType(), facets);
        return ResponseEntity.ok(SuggestResponse);
    }
}
