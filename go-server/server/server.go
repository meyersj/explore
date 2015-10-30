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
func Communicate(conn net.Conn) {
	fmt.Println("\nopen connection", time.Now(), "\n")
	defer conn.Close()

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
				response := RegisterBeacon(p)
				conn.Write(response)
			case protocol.CLIENT_UPDATE:
				response := ClientUpdate(p)
				conn.Write(response)
			case protocol.PUT_MESSAGE:
				response := PutMessage(p)
				conn.Write(response)
			case protocol.GET_MESSAGE:
				response := GetMessage(p)
				conn.Write(response)

			}
		}
	}
}
