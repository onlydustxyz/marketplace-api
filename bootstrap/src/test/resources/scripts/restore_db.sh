#!/bin/sh

# Truncate all tables
psql -d marketplace_db -U test -f /scripts/clean.sql

# Restore the data
# To create the dump, run the following command in docker:
#   > pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --data-only -Fc -f /tmp/IT_data.dump
pg_restore -d marketplace_db -U test --disable-triggers /tmp/IT.dump

# Apply custom modifications for tests
psql -d marketplace_db -U test -f /docker-entrypoint-initdb.d/6-change-data-for-testing.sql
