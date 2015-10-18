package main

import (
	"bufio"
	"bytes"
	"encoding/hex"
	"fmt"
	"net"
	"time"
)

import "github.com/BurntSushi/toml"

// https://systembash.com/a-simple-go-tcp-server-and-tcp-clients
// http://www.golangpatterns.info/concurrency/producer-consumer

const DEFAULT_HOST string = "127.0.0.1"
const DEFAULT_PORT string = "8082"
const DEFAULT_UID string = "82C816B8CB37D896830F"

var EDDYSTONE_UID = []byte{0x16, 0xAA, 0xFE}

type Config struct {
	Host string
	Port string
	Uid  string
}

// create and return Config datai
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

type Advertisement struct {
	raw        []byte
	structures [][]byte
	eddystone  bool
	uid        []byte
	instance   []byte
}

func (a *Advertisement) parse() {
	a.structures = [][]byte{}
	if len(a.raw) < 2 {
		return
	}
	// recursively parse advertisement data
	a.parse_rec(0, 0)

	// if advertisement is an EddyStoneUID broadcast
	// grab uid and instnce fields
	// https://github.com/google/eddystone/blob/master/protocol-specification.md
	if len(a.structures) == 3 && len(a.structures[2]) >= 3 &&
		bytes.Equal(a.structures[2][0:3], EDDYSTONE_UID) {
		a.eddystone = true
		a.uid = a.structures[2][5 : 5+10]
		a.instance = a.structures[2][5+10 : 5+10+6]
	} else {
		a.eddystone = false
	}
}

func (a *Advertisement) parse_rec(offset int, count int) {
	if offset >= len(a.raw) || int(a.raw[offset]) <= 0 {
		return
	}

	length := int(a.raw[offset])
	offset = offset + 1
	a.structures = append(a.structures, []byte{})
	a.structures[count] = a.raw[offset : offset+length]
	a.parse_rec(offset+length, count+1)

	//fmt.Println("Data Bytes", count+1, ":", a.structures[count])
	//fmt.Println("Data Hex", count+1, ":", hex.Dump(a.structures[count]))

}

// represents a single complete message
type Payload struct {
	id     int
	length int
	data   []byte
	rssi   int8
	adv    *Advertisement
}

func (p *Payload) complete() bool {
	if p.length > 0 && p.length == len(p.data) {
		return true
	}
	return false
}

func (p *Payload) parse() {
	if len(p.data) > 1 {
		p.rssi = int8(p.data[0])
		p.data = p.data[1:len(p.data)]
		p.adv = &Advertisement{raw: p.data}
		p.adv.parse()
	}
}

func (p *Payload) add_bytes(bytes []byte) bool {
	if p.length == len(p.data)+len(bytes) {
		// after input bytes are appended payload
		// will be complete
		p.data = append(p.data, bytes...)
		p.data = p.data[1 : len(p.data)-1]
		return true
	} else {
		// more data will be recieved
		p.data = append(p.data, bytes...)
		return false
	}
}

func (p *Payload) pprint() {
	fmt.Println("rssi", p.rssi)
	fmt.Println("size", p.length)
	fmt.Println("data", p.data, "\n")
}

func payload_factory(bytes []byte) *Payload {
	// create new Payload object with correct defaults
	return &Payload{length: int(bytes[0]), data: []byte{}}
}

// main worker function that handles communication with the client
// bytes are parsed into separate payloads and passed
// to a consumer thread
func communicate(conn net.Conn, payload_channel chan *Payload) {

	defer conn.Close()

	var payload *Payload
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
				payload = payload_factory(bytes)
			}

			// copy bytes and update if we are finished
			// for this object and wait for a new
			// payload, or for the exit signal to close connection
			complete = payload.add_bytes(bytes)
			if complete {
				payload.parse()
				// send completed payload across channel to consumer
				payload_channel <- payload
			}
		}
	}
}

func payload_consumer(channel chan *Payload, uid []byte) {
	for {
		payload := <-channel
		if payload.adv.eddystone && bytes.Equal(uid, payload.adv.uid) {
			fmt.Println("Signal", payload.rssi, ", Instance", payload.adv.instance)
		} //else {
		//	fmt.Println("Non Eddy", payload.adv.structures, "\n")
		//}
	}
}

func main() {
	conf := read_config("test-config.toml")
	payload_channel := make(chan *Payload)

	// start listening for client connections
	listener, listener_error := net.Listen("tcp", ":"+conf.Port)

	if listener != nil {
		// start consumer thread
		fmt.Println(conf.Uid)
		uid, _ := hex.DecodeString(conf.Uid)
		go payload_consumer(payload_channel, uid)
		fmt.Println("Accepting connections...\n")
		for {
			// accept connection from client
			conn, _ := listener.Accept()
			if conn != nil {
				// start thread for communication with client
				go communicate(conn, payload_channel)
			}
		}
		listener.Close()
	} else {
		fmt.Println("Error:", listener_error)
	}
}
