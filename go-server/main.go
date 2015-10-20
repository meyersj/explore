package main

import (
	"./payload"
	"./server"
	"encoding/hex"
	"fmt"
	"net"
)

func main() {
	conf := server.Read_config("test-config.toml")
	payload_channel := make(chan *payload.Payload)
	eddystone_channel := make(chan *payload.EddyStoneUID)

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		// start consumer thread and triangulator threads
		uid, _ := hex.DecodeString(conf.Uid)
		go server.Triangulator(eddystone_channel)
		go server.Payload_consumer(payload_channel, eddystone_channel, uid)
		fmt.Println("Accepting connections...")

		// infinite loop to accept connections from clients and
		// then handle communication concurrently
		//
		// completed messages are passed to the consumer thread
		// using payload_channel
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				// start thread
				go server.Communicate(conn, payload_channel)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
