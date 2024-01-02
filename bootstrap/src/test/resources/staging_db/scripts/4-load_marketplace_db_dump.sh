bash -c "psql -d marketplace_db -U test -f /tmp/od_staging_indexer_exp_dump.sql"
bash -c "psql -d marketplace_db -U test -f /tmp/IT_dump.sql"
