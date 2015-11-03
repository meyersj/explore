# Explore

Android app to interact with your environment through BLE advertisements. A back-end
server written in Go with Redis supports the app.

### description

Communication between `android-client` and `go-server` is done using a TCP/IP sockets.
The app currently provides ability to scan for BLE advertisements. A location
name can be associated with that chip's MAC address. Once associated users
can store messages at that location (assuming the advertisment is from a beacon
that stays at that location). Other users retrieve those messages when scanning near that beacon.

### config
1. rename `explore/go-server/test-config.toml` to `config.toml`
2. rename `explore/android-client/app/src/main/assets/default-config.properties` to `config.properties`
3. in both config files set **host** and **port** appropriately
4. change **redis** connection param for `go-server` if not running redis on local machine with default port 

go-server requires toml and redis packages
```
go get github.com/BurntSushi/toml
go get gopkg.in/redis.v3
```

### running

Install go dependencies and start redis on port `6379`, then start the server
```
cd go-server
go run main.go &
```

To run on LAN you need to figure out what your computers internal IP for your network is. Running `ifconfig`
on my computer shows `inet addr:192.168.1.101` under the `wlan0` entry. That is the IP you will want to use
as the **host** param in the android config. Keep the default **port** `8082`.

Build android app then install `app-debug.apk` found in `android-client/app/build/outputs/apk`
```
cd android-client
./gradlew assembleDebug
```

After installing open up device and open up *Settings*. Confirm **host** and **port** match your server.
You can then *Start Scan* and nearby beacons will be displayed.
