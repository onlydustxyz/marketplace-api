package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadSearchApi;
import onlydust.com.marketplace.api.contract.model.SearchPostRequest;
import onlydust.com.marketplace.api.contract.model.SearchResponse;
import onlydust.com.marketplace.api.contract.model.SuggestPostRequest;
import onlydust.com.marketplace.api.contract.model.SuggestResponse;
import onlydust.com.marketplace.api.read.repositories.elasticsearch.SearchRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadSearchApiElasticSearchAdapter implements ReadSearchApi {

    private final SearchRepository searchRepository;

    @Override
    public ResponseEntity<SearchResponse> search(SearchPostRequest searchPostRequest) {
        final SearchResponse searchResponse = searchRepository.searchAll(
                searchPostRequest.getKeyword(),
                searchPostRequest.getType(),
                null,
                searchPostRequest.getPageIndex() * searchPostRequest.getPageSize(),
                searchPostRequest.getPageSize());
        return ResponseEntity.ok(searchResponse);
    }

    @Override
    public ResponseEntity<SuggestResponse> suggest(SuggestPostRequest suggestPostRequest) {
        final SuggestResponse SuggestResponse = searchRepository.suggest(suggestPostRequest.getKeyword(), suggestPostRequest.getType());
        return ResponseEntity.ok(SuggestResponse);
    }
}
