#!/bin/sh

FIRST_RUN=$1

# Truncate all tables
if ! psql -d marketplace_db -U test -f /scripts/clean.sql; then
  echo "Failed to truncate tables"
  exit 1
fi

# Restore the data
if ! pg_restore -d marketplace_db -U test -j 6 --disable-triggers /tmp/IT_data.dump; then
  echo "Failed to restore data"
  exit 1
fi

if [ "$FIRST_RUN" = "true" ]; then
  # Refresh materialized views
  if ! psql -d marketplace_db -U test -f /scripts/refresh_materialized_views.sql; then
    echo "Failed to refresh materialized views"
    exit 1
  fi
fi
