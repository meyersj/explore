package main

import (
	"./data"
	"./server"
	"fmt"
	"net"
)

func main() {
	conf := server.Read_config("config.toml")

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		redis_client := data.InitClient(conf.Redis)
		fmt.Println("Accepting connections...")
		// infinite loop to accept connections from clients and
		// then handle communication concurrently
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				// start communication thread with client
				go server.Communicate(conn, redis_client)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
