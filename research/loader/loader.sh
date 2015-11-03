#~/bin/bash
set -e

research="$(dirname "$PWD")"
schema=$PWD/schema.sql
log=$research/data/20151102_log.csv

psql postgres << EOF
    DROP DATABASE explore;
    CREATE DATABASE explore;
    \c explore
    CREATE EXTENSION postgis;
    \i ${schema}
    \copy log FROM ${log} CSV HEADER
    DELETE FROM log WHERE lat IS NULL;
    UPDATE log SET name = NULL WHERE name = 'null';
    \q
EOF
