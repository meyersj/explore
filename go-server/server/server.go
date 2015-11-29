package server

import (
	"../data"
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
func Communicate(
	conn net.Conn,
	redis_client *data.Client,
	dispatcher chan *Broadcast,
	router *Router,
) {
	defer conn.Close()
	fmt.Println("\nopen connection", time.Now(), "\n")
	//init := true
	handler := InitHandler(conn, redis_client, dispatcher, router)
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
			//case protocol.GET_MESSAGE:
			//	handler.GetMessage(p)
			case protocol.GET_BEACONS:
				handler.GetBeacons(p)
			case protocol.JOIN_CHANNEL:
				handler.JoinChannel(p)
			case protocol.LEAVE_CHANNEL:
				handler.LeaveChannel(p)
			case protocol.BROADCAST:
				handler.BroadcastMessage(p)
			}
		}
	}
}

// basic function to receive broadcasts and pass them to router
// for forwarding
func DispatchRouter(router *Router, dispatcher chan *Broadcast) {
	fmt.Println("\nStarting DispatchRouter", time.Now(), "\n")
	for {
		broadcast := <-dispatcher
		router.Broadcast(broadcast)
	}
}
