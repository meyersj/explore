# ble-explorer

Android app to interact with your environment through BLE advertisements. A back-end
server written in Go and Redis supports the app.

### description

Communication between `android-client` and `go-server` is done using a TCP/IP sockets.
The app currently provides three modes.

##### Register Client

For this you simply enter a public username and it is associated with a unique
identifier for your device. 

##### Register Beacon

You conduct a BLE scan looking for advertisements. Once discovered you can enter
a name for the beacon and register it. This will create a unique identifier and associate
the name you provided and your current coordinates. Registering beacons allows them to be
discovered in **Explore** mode.

##### Explorer

You conduct a BLE scan looking for advertisements. If the discovered beacon is registered then
information will be displayed about it. Otherwise it will show up as unregistered.

### config
1. rename `ble-tester/test-config.toml` to `config.toml`
2. rename `/ble-tester/android-client/app/src/main/assets/default-config.properties` to `config.properties`
3. in both config files set **host** and **port** appropriately

go-server requires a toml and redis packages
```
go get github.com/BurntSushi/toml
go get gopkg.in/redis.v3
```

