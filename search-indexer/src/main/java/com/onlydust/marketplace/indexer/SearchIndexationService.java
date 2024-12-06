package com.onlydust.marketplace.indexer;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.postgres.repository.ReadProjectIndexRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SearchIndexationService {
    private final ReadProjectIndexRepository readProjectIndexRepository;
    private final ElasticSearchAdapter elasticSearchAdapter;

    public void indexAllProjects() {
        LOGGER.info("Starting full indexation of projects");
        final var projects = readProjectIndexRepository.findAll();
        elasticSearchAdapter.bulkIndex(projects);
        LOGGER.info("Finished indexing {} projects", projects.size());
    }
}