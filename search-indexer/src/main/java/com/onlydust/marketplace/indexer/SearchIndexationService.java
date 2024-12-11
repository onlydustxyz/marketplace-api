package com.onlydust.marketplace.indexer;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchAdapter;
import com.onlydust.marketplace.indexer.postgres.entity.SearchContributorEntity;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchContributorRepository;
import com.onlydust.marketplace.indexer.postgres.repository.ReadSearchProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class SearchIndexationService {
    private static final int PAGE_SIZE = 1000;
    private final ElasticSearchAdapter elasticSearchAdapter;
    private final ReadSearchProjectRepository readSearchProjectRepository;
    private final ReadSearchContributorRepository readSearchContributorRepository;

    public void indexAllProjects() {
        LOGGER.info("Starting full indexation of projects");
        final var projects = readSearchProjectRepository.findAll();
        elasticSearchAdapter.indexAllProjects(projects);
        LOGGER.info("{} projects where indexed", projects.size());
    }

    public void indexAllContributors() {
        LOGGER.info("Starting full indexation of contributors");
        int offset = 0;
        int total = 0;
        var contributors = indexContributors(offset);
        elasticSearchAdapter.indexAllContributors(contributors);
        total += contributors.size();
        while (contributors.size() == PAGE_SIZE) {
            offset += PAGE_SIZE;
            contributors = indexContributors(offset);
            total += contributors.size();
        }
        LOGGER.info("{} contributors where indexed", total);
    }

    private List<SearchContributorEntity> indexContributors(int offset) {
        LOGGER.info("Starting to index contributors from {} to {}", offset, offset + PAGE_SIZE);
        List<SearchContributorEntity> contributors = readSearchContributorRepository.findAll(offset, PAGE_SIZE);
        elasticSearchAdapter.indexAllContributors(contributors);
        LOGGER.info("Contributors from {} to {} where indexed", offset, offset + PAGE_SIZE);
        return contributors;
    }

}