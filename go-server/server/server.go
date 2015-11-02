package server

import (
	"../payload"
	"../protocol"
	"bufio"
	"fmt"
	"net"
	"time"
)

// main worker thread that handles communication with the client
// bytes are parsed into separate payloads and passed
// to a consumer thread
func Communicate(conn net.Conn, config *Config) {
	fmt.Println("\nopen connection", time.Now(), "\n")
	defer conn.Close()

	handler := InitHandler(config.Redis, conn)
	var p *payload.Payload
	buffer := bufio.NewReader(conn)
	for {
		p, _ = payload.Read(buffer)
		if p != nil {
			//fmt.Println(bytes)
			switch p.Flags[0] {
			case protocol.CLOSE_CONN:
				fmt.Println("\nconnection closed", time.Now(), "\n")
				return
			case protocol.REGISTER_BEACON:
				handler.RegisterBeacon(p)
			case protocol.CLIENT_UPDATE:
				handler.ClientUpdate(p)
			case protocol.PUT_MESSAGE:
				handler.PutMessage(p)
			case protocol.GET_MESSAGE:
				handler.GetMessage(p)
			case protocol.GET_BEACONS:
				handler.GetBeacons(p)
			}
		}
	}
}
