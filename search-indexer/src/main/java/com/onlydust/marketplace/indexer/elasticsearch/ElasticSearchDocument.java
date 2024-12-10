package com.onlydust.marketplace.indexer.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ElasticSearchDocument<Id> {

    @JsonIgnore
    Id getDocumentId();
}
