DROP TABLE IF EXISTS mac_intersection;
CREATE TEMP TABLE mac_intersection AS
SELECT DISTINCT mac FROM log1
INTERSECT 
SELECT DISTINCT mac FROM log2;


DROP TABLE IF EXISTS persistant_data;
CREATE TABLE persistant_data AS
SELECT 1::integer AS log, *
FROM log1
WHERE mac IN (SELECT * FROM mac_intersection)
UNION ALL
SELECT 2::integer AS log, *
FROM log2
WHERE mac IN (SELECT * FROM mac_intersection);

DROP TABLE IF EXISTS persistant_clusters;
CREATE TABLE persistant_clusters AS
SELECT
    mac,
    manuf,
    count(*) AS count,
    ST_Centroid(ST_Union(geom)) AS centroid,
    ST_ConvexHull(ST_Union(geom)) AS geom
FROM persistant_data
GROUP BY mac, manuf;
