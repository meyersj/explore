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

// main worker thread that handles communication with a client
// loop forever handling requests until client closes connection
func Communicate(
	conn net.Conn,
	db *data.DBClient,
	dispatcher chan *Broadcast,
	router *Router,
) {
	defer conn.Close()
	//defer db.Close()

	fmt.Println("\nopen connection", time.Now(), "\n")
	var p *payload.Payload
	device, beacon := "", ""
	handler := InitHandler(conn, db, dispatcher, router)

	// loop forever until CLOSE_CONN signal
	buffer := bufio.NewReader(conn)
loop:
	for {
		p, _ = payload.Read(buffer)
		if p != nil {
			switch p.Flags[0] {
			case protocol.CLOSE_CONN:
				fmt.Println("\nconnection closed", time.Now(), "\n")
				break loop
			case protocol.BEACON_LOOKUP:
				handler.BeaconLookup(p)
			case protocol.BEACON_REGISTER:
				handler.BeaconRegister(p)
			case protocol.JOIN_CHANNEL:
				device, beacon = handler.JoinChannel(p)
			case protocol.LEAVE_CHANNEL:
				device, beacon = "", ""
				handler.LeaveChannel(p)
			case protocol.SEND_BROADCAST:
				handler.SendBroadcast(p)
			}
		}
	}
	if device != "" && beacon != "" {
		router.LeaveChannel(device, beacon)
	}
}

// basic function to receive broadcasts and pass them to the router for forwarding
func DispatchRouter(router *Router, dispatcher chan *Broadcast) {
	fmt.Println("\nStarting DispatchRouter", time.Now(), "\n")
	for {
		broadcast := <-dispatcher
		router.Broadcast(broadcast)
	}
}
