package main

import (
	"./data"
	"./server"
	"fmt"
	"net"
	"os"
)

func main() {
	config_file := os.Getenv("EXPLORE_CONFIG")
	if len(config_file) == 0 {
		config_file = "config.toml"
	}

	conf := server.Read_config(config_file)
	// will keep track which clients are connected to which beacons
	router := server.InitRouter()
	// dispatcher will receive broadcasts from all clients and pass them
	// to the router to be forwarded
	dispatcher := make(chan *server.Broadcast)
	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		go server.DispatchRouter(router, dispatcher)
		redis_client := data.InitClient(conf.Redis)
		fmt.Println("Accepting connections...")
		// infinite loop to accept connections from clients and
		// then handle communication concurrently
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				// start communication thread with client
				go server.Communicate(conn, redis_client, dispatcher)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
