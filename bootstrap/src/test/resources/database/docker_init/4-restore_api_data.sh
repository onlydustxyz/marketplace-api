#!/bin/bash

# To create the dump, run the following command in docker:
#   > pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --schema-only -e citext -e pgcrypto -Fc -f /tmp/IT_schema.dump
if ! pg_restore -d marketplace_db -U test -j 6 /tmp/IT_schema.dump; then
    echo "Failed to load schema dump"
    exit 1
fi
