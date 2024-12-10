package com.onlydust.marketplace.indexer.postgres.entity;

import com.onlydust.marketplace.indexer.elasticsearch.ElasticSearchDocument;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class SearchContributorEntity implements ElasticSearchDocument<Long> {
    @Id
    private Long githubId;
    private String githubLogin;
    private String bio;
    private Integer contributionCount;
    private Integer projectCount;
    private Integer pullRequestCount;
    private Integer issueCount;
    private String htmlUrl;

    @Override
    public Long getDocumentId() {
        return githubId;
    }
}
