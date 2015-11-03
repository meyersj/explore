-- return manufacture name based on prefix of MAC address
-- using wireshark manufacture database
-- https://www.wireshark.org/tools/oui-lookup.html

DROP FUNCTION IF EXISTS manuf_lookup(varchar);
CREATE OR REPLACE FUNCTION manuf_lookup(in_mac varchar)
    RETURNS text AS
$$
DECLARE
    out_manuf text;
BEGIN
    out_manuf := '';
    SELECT m.shortm INTO out_manuf
    FROM manuf AS m
    WHERE prefix = substring(upper(in_mac) FROM 1 FOR 8)
    LIMIT 1;
    RETURN out_manuf;
END;
$$
LANGUAGE 'plpgsql'
IMMUTABLE;
