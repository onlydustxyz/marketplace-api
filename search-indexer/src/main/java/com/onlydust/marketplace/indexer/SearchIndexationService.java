package com.onlydust.marketplace.indexer;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchContributorRepository;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SearchIndexationService {
    private final ElasticSearchAdapter elasticSearchAdapter;
    private final ReadSearchProjectRepository readSearchProjectRepository;
    private final ReadSearchContributorRepository readSearchContributorRepository;

    public void indexAllProjects() {
        LOGGER.info("Starting full indexation of projects");
        final var projects = readSearchProjectRepository.findAll();
        elasticSearchAdapter.indexAllProjects(projects);
        LOGGER.info("Finished indexing {} projects", projects.size());
    }

    public void indexAllContributors() {
        LOGGER.info("Starting full indexation of contributors");
        final var contributors = readSearchContributorRepository.findAll();
        elasticSearchAdapter.indexAllContributors(contributors);
        LOGGER.info("Finished indexing {} contributors", contributors.size());
    }

}