package server

import (
	"../handler"
	"../payload"
	"../protocol"
	"bufio"
	"fmt"
	"net"
	"time"
)

func is_client_finished(bytes []byte, complete bool) bool {
	// if bytes is of length two with contents {0x00, 0xFF}
	// then that is the close connection message
	// from client that it is finished so
	// return from function to start
	// listening for another client
	if complete && len(bytes) == 2 && bytes[0] == protocol.CLOSE_CONN {
		return true
	}
	return false
}

func handle_bytes(p *payload.Payload, bytes []byte, done bool) (
	*payload.Payload, bool) {

	// if last bytes recieved finished the message
	// intialize a new payload object
	if done {
		p = payload.InitPayload(bytes)
	}
	// copy bytes and update if we are finished
	// for this object and wait for a new
	// payload, or for the exit signal to close connection
	return p, p.AddBytes(bytes)
}

// main worker thread that handles communication with the client
// bytes are parsed into separate payloads and passed
// to a consumer thread
func Communicate(conn net.Conn) {
	fmt.Println("\nopen connection", time.Now(), "\n")
	defer conn.Close()

	var p *payload.Payload
	done := true

	// create buffered io reader object with connection
	buffer := bufio.NewReader(conn)
	for {
		// read bytes until 0xFF is encountered
		// the first byte sent will be the length
		// so all bytes will be read into correct payload object
		bytes, error := buffer.ReadBytes(protocol.DELIMITER)
		if error == nil && len(bytes) > 0 {
			//fmt.Println(bytes)
			p, done = handle_bytes(p, bytes, done)
			if done {
				switch p.Flag {
				case protocol.CLOSE_CONN:
					fmt.Println("\nconnection closed", time.Now(), "\n")
					return
				case protocol.REGISTER_CLIENT:
					response := handler.RegisterClient(p)
					conn.Write(response)
				case protocol.REGISTER_BEACON:
					response := handler.RegisterBeacon(p)
					conn.Write(response)
				case protocol.CLIENT_UPDATE:
					response := handler.ClientUpdate(p)
					conn.Write(response)
				case protocol.GET_STATUS:
				}
			}
		}
	}
}
