CREATE TABLE log (
    tstamp timestamp without time zone,
    mac varchar,
    rssi integer,
    name varchar,
    advertisement bytea,
    lat varchar,
    lon varchar,
    accuracy numeric
);
