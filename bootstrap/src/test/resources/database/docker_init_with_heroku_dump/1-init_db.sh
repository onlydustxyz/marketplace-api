#!/bin/bash

#psql -d marketplace_db -U test -c "DROP SCHEMA IF EXISTS public"
psql -d marketplace_db -U test -c "CREATE SCHEMA IF NOT EXISTS heroku_ext"
psql -d marketplace_db -U test -c "GRANT ALL ON SCHEMA heroku_ext TO public"
psql -d marketplace_db -U test -c "GRANT USAGE ON SCHEMA heroku_ext TO public"
#psql -d marketplace_db -U test -c "ALTER database marketplace_db SET search_path TO heroku_ext,public"

if ! pg_restore --no-acl --no-owner --disable-triggers -j 6 -d marketplace_db -U test /tmp/heroku.dump; then
    echo "Failed to load schema dump"
    exit 1
fi

#psql -d marketplace_db -U test -c "ALTER database marketplace_db SET search_path TO public,heroku_ext"
