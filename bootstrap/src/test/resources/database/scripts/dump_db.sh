#!/bin/sh

pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --schema='bi' --schema='bi_internal' --schema='migration' --schema='reco' --data-only -Fc -f /tmp/IT_data.dump
pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --schema='bi' --schema='bi_internal' --schema='migration' --schema='reco' --schema-only -e citext -e pgcrypto -e pg_trgm -e uuid-ossp -Fc -f /tmp/IT_schema.dump
