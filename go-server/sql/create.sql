DROP TABLE IF EXISTS beacons;
CREATE TABLE beacons (
    beacon varchar PRIMARY KEY,
    name varchar
);

DROP TABLE IF EXISTS broadcasts;
CREATE TABLE broadcasts (
    beacon varchar,
    client_id varchar,
    client_name varchar,
    message varchar
);

