#!/bin/sh

# Truncate all tables
if ! psql -d marketplace_db -U test -f /scripts/clean.sql; then
  echo "Failed to truncate tables"
  exit 1
fi

# Restore the data
# To create the dump, run the following command in docker:
#   > pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --data-only -Fc -f /tmp/IT_data.dump
if ! pg_restore -d marketplace_db -U test -j 6 --disable-triggers /tmp/IT_data.dump; then
  echo "Failed to restore data"
  exit 1
fi

# Apply custom modifications for tests
if ! psql -d marketplace_db -U test -f /scripts/change_data_for_testing.sql; then
  echo "Failed to apply custom modifications for tests"
  exit 1
fi
