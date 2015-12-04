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

	// dispatcher will receive broadcasts from all clients and pass them
	// to the router to be forwarded
	dispatcher := make(chan *server.Broadcast)

	// router will keep track which clients are connected to which beacons
	router := server.InitRouter()

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		db := data.InitDBClient(conf.Postgres)
		// start background channel to handle broadcasting messages
		go server.DispatchRouter(router, dispatcher)
		defer listener.Close()
		defer db.Close()
		// infinite loop to accept connections from clients and
		// then handle communication concurrently
		fmt.Println("Accepting connections...")
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				go server.Communicate(conn, db, dispatcher, router)
			}
		}
	} else {
		fmt.Println("Error:", listener_error)
	}
}
