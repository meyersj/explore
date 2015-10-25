package main

import (
	"./data"
	//"./payload"
	"./server"
	//"encoding/hex"
	"fmt"
	"net"
)

func main() {
	conf := server.Read_config("test-config.toml")
	redis_chan := make(chan *data.Message)

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		//uid, _ := hex.DecodeString(conf.Uid)
		go server.RedisWriter(redis_chan)
		fmt.Println("Accepting connections...")

		// infinite loop to accept connections from clients and
		// then handle communication concurrently
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				// start communication thread
				go server.Communicate(conn, redis_chan)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
