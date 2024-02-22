#!/bin/sh

pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --data-only -Fc -f /tmp/IT_data.dump
pg_dump -d marketplace_db -U test --schema='public' --schema='iam' --schema='rfd' --schema='accounting' --schema-only -e citext -e pgcrypto -Fc -f /tmp/IT_schema.dump
