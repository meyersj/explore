package main

import "net"
import "fmt"
import "bufio"
import "strings"

// https://systembash.com/a-simple-go-tcp-server-and-tcp-client
// http://www.golangpatterns.info/concurrency/producer-consumer

func communicate(count int, conn net.Conn, channel chan string) {
	defer conn.Close()

	// run loop until empty messages are sent
	for {
		// will listen for message to process ending in newline (\n)
		message, _ := bufio.NewReader(conn).ReadString('\n')
		if len(message) == 0 {
			return
		}

		//fmt.Println("Message Received:", count, string(message))

		// send message to channel
		channel <- message[0 : len(message)-1]

		// echo message back to client
		conn.Write([]byte(strings.ToUpper(message) + "\n"))
	}
}

func consumer(channel chan string) {
	messages := []string{}
	for {
		message := <-channel
		messages = append(messages, message)
		fmt.Println("Message:", strings.Join(messages, " "))
	}
}

func main() {

	fmt.Println("Starting server...")

	// create channel to send messages from multiple tcp socket
	// communication threads to consumer thread
	channel := make(chan string)
	count := 0

	// listen on all interfaces
	listener, _ := net.Listen("tcp", ":8081")

	// start consumer thread
	go consumer(channel)

	// accept connections forever
	for {
		conn, _ := listener.Accept()
		if conn != nil {
			count += 1
			// communiate with client
			go communicate(count, conn, channel)
		}
	}

}
