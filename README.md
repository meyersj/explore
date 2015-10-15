# ble-tester
Android client listens for BLE packets and sends to Go server using a TCP socket

### description

The `android-client` connects to the `go-server` using a TCP/IP socket.
Once connected the app will listen for advertisement packets from BLE
beacons. I have been testing with the
[RadBeacon Dot](http://store.radiusnetworks.com/products/radbeacon-dot).
Each advertisement the app receives is packaged up along
with meta-data such as signal strength and sent over a socket to
the server and printed to stdout.

### config
1. rename `ble-tester/test-config.toml` to `config.toml`
2. rename `/ble-tester/android-client/app/src/main/assets/default-config.properties` to `config.properties`
3. in both config files set Host and Port appropriately

go-server requires a toml parser package
```
go get github.com/BurntSushi/toml
```

