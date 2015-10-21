package main

import "net"
import "fmt"
import "bufio"
import "os"

// https://systembash.com/a-simple-go-tcp-server-and-tcp-client

func run_client(conn net.Conn) {
	reader := bufio.NewReader(os.Stdin)

	for {
		// read in input from stdin
		fmt.Print("Text to send: ")
		text, _ := reader.ReadString('\n')
		if text[0:len(text)-1] == "EXIT" {
			return
		}

		// send to socket
		fmt.Fprintf(conn, text+"\n")

		// listen for reply
		message, _ := bufio.NewReader(conn).ReadString('\n')
		fmt.Print("Message from server: " + message)
	}
}

func main() {

	// connect to socket and run client to send messages
	conn, _ := net.Dial("tcp", "127.0.0.1:8081")
	run_client(conn)
}
