#~/bin/bash
set -e

db=explore
research="$(dirname "$PWD")"
schema=$PWD/schema.sql
manuf_lookup=$research/analyze/manuf_lookup.sql
analyze=$research/analyze/analyze.sql
manuf=$research/data/manuf.sql
log1=$research/data/20151102_log.csv
log2=$research/data/20151103_log.csv


function create_db {
    psql postgres << EOF
        -- build database
        DROP DATABASE IF EXISTS ${db};
        CREATE DATABASE ${db};
        \c ${db}
        CREATE EXTENSION postgis;

        -- create schema and load data
        \i ${schema}
        \i ${manuf}
        \i ${manuf_lookup}
EOF
}

function load_logs {
    table=$1
    file=$2
    psql $db << EOF
        \copy ${table} FROM ${file} CSV HEADER
        \set log_table '\'' ${table} '\''

        -- clean up data
        DELETE FROM ${table} WHERE lat IS NULL;
        UPDATE ${table} SET name = NULL WHERE name = 'null';
        UPDATE ${table} SET bytes = substring(bytes FROM 0 FOR 63);    
        -- convert lat-lon to geometry field
        SELECT AddGeometryColumn ('public',:log_table,'geom',4326,'POINT',2);
        UPDATE ${table}
            SET geom = ST_GeomFromText('POINT(' || lon || ' ' || lat || ')', 4326);
        ALTER TABLE ${table} ADD COLUMN manuf VARCHAR;
        UPDATE ${table} SET manuf = manuf_lookup(mac);
        
            -- drop un-needed fields
        ALTER TABLE ${table} DROP COLUMN lat;
        ALTER TABLE ${table} DROP COLUMN lon;
        ALTER TABLE ${table} DROP COLUMN accuracy;
        \q
EOF
}

create_db
load_logs log1 ${log1}
load_logs log2 ${log2}
psql -f ${analyze} -d ${db}

