#!/bin/bash

# To create the dump, run the following command in docker:
#   > pg_dump -d marketplace_db -U test --schema='indexer' --schema='indexer_exp' --schema='indexer_outbox' -Fc -f /tmp/IT_indexer.dump
if ! pg_restore -d marketplace_db -U test --disable-triggers -j 6 /tmp/IT_indexer.dump; then
    echo "Failed to load indexer dump"
    exit 1
fi
