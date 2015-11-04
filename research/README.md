Log data was collected with an android app that picks up BLE advertisements
as well as current location. The goal is to collect data from the same
location at different days/times and then intersect the data to find
persistant beacon that can be used for messaging.

Walked along SW 5th Avenue and collected BLE data on November 2nd and 3rd.

![Logged Data](https://github.com/meyersj/explore/blob/master/research/logged_data.png)

Loaded data into Postgres and ran some simple queries to find devices that sent
pings on both days. For those devices create some convex hulls showing where
they are active

![Persistant Signals](https://github.com/meyersj/explore/blob/master/research/persistant_signals.png)
    
