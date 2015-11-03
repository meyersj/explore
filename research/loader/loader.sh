#~/bin/bash
set -e

db=explore
research="$(dirname "$PWD")"
schema=$PWD/schema.sql
log=$research/data/20151102_log.csv

psql postgres << EOF
    DROP DATABASE $db;
    CREATE DATABASE $db;
    \c $db
    CREATE EXTENSION postgis;
    \i ${schema}
    \copy log FROM ${log} CSV HEADER
    
    -- clean up data
    DELETE FROM log WHERE lat IS NULL;
    UPDATE log SET name = NULL WHERE name = 'null';
    
    -- convert lat-lon to geometry field
    SELECT AddGeometryColumn ('public','log','geom',4326,'POINT',2);
    UPDATE log
        SET geom = ST_GeomFromText('POINT(' || lon || ' ' || lat || ')', 4326);
    
    -- drop un-needed fields
    ALTER TABLE log DROP COLUMN lat;
    ALTER TABLE log DROP COLUMN lon;
    ALTER TABLE log DROP COLUMN accuracy;
    \q
EOF
