#!/bin/sh


# Truncate all tables
if ! psql -d marketplace_db -U test -f /scripts/clean_indexer.sql; then
  echo "Failed to truncate tables"
  exit 1
fi

# Restore the data
if ! pg_restore -d marketplace_db -U test --data-only --disable-triggers -j 6 /tmp/IT_indexer.dump; then
    echo "Failed to load indexer dump"
    exit 1
fi
