package main

import (
	"./payload"
	"bufio"
	"encoding/hex"
	"fmt"
	"github.com/BurntSushi/toml"
	"net"
	"time"
)

// https://systembash.com/a-simple-go-tcp-server-and-tcp-clients
// http://www.golangpatterns.info/concurrency/producer-consumer

const DEFAULT_HOST string = "127.0.0.1"
const DEFAULT_PORT string = "8082"
const DEFAULT_UID string = "82C816B8CB37D896830F"

type Config struct {
	Host string
	Port string
	Uid  string
}

// create and return Config data
func read_config(filename string) *Config {
	var conf Config
	if _, err := toml.DecodeFile(filename, &conf); err != nil {
		conf.Host = DEFAULT_HOST
		conf.Port = DEFAULT_PORT
		conf.Uid = DEFAULT_UID
		fmt.Println("Error processing", filename)
		fmt.Println("Default Host =", conf.Host)
		fmt.Println("Default Port =", conf.Port)
		fmt.Println("Default UID =", conf.Uid)
	}
	return &conf
}

// main worker thread that handles communication with the client
// bytes are parsed into separate payloads and passed
// to a consumer thread
func communicate(conn net.Conn, payload_channel chan *payload.Payload) {
	fmt.Println("\nopen connection", time.Now(), "\n")
	defer conn.Close()

	var p *payload.Payload
	complete := true

	// create buffered io reader object with connection
	buffer := bufio.NewReader(conn)
	for {
		// read bytes until 0xFF is encountered
		// the first byte sent will be the length
		// so all bytes will be read into correct payload object
		bytes, error := buffer.ReadBytes(0xFF)
		if error == nil && len(bytes) > 0 {

			//fmt.Println(bytes)

			// if bytes is of length two with contents {0x00, 0xFF}
			// then that is the closing connection flag
			// from client that it is finished so
			// return from function to start
			// listening for another client

			if complete && len(bytes) == 2 && bytes[0] == 0x00 {
				fmt.Println("\nclose connection", time.Now(), "\n")
				return
			}

			// if last bytes recieved finished the message
			// intialize a new payload object
			if complete {
				p = payload.InitPayload(bytes)
			}

			// copy bytes and update if we are finished
			// for this object and wait for a new
			// payload, or for the exit signal to close connection
			complete = p.AddBytes(bytes)
			if complete {
				//p.Parse()
				// send completed payload across channel to consumer
				payload_channel <- p
			}
		}
	}
}

func payload_consumer(channel chan *payload.Payload, uid []byte) {
	for {
		p := <-channel
		adv := payload.InitAdvertisement(p.Data)
		valid, frame := payload.ParseEddyStone(adv)
		if valid {
			// TODO do something real with parsed advertisement frames
			fmt.Println("EddyStone:", frame)
		} else {
			fmt.Println("Unknown", frame)
		}
		adv.Print()
	}
}

func main() {
	conf := read_config("test-config.toml")
	payload_channel := make(chan *payload.Payload)

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		// start consumer thread
		uid, _ := hex.DecodeString(conf.Uid)
		go payload_consumer(payload_channel, uid)
		fmt.Println("Accepting connections...")

		// infinite loop to accept connections from clients and
		// then handle communication concurrently

		// completed messages are passed to the consumer thread
		// using payload_channel
		for {
			conn, _ := listener.Accept()
			if conn != nil {
				// start thread
				go communicate(conn, payload_channel)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
