package main

import "net"
import "fmt"
import "bufio"
import "time"

import "github.com/BurntSushi/toml"

// https://systembash.com/a-simple-go-tcp-server-and-tcp-clients

const DEFAULT_HOST string = "127.0.0.1"
const DEFAULT_PORT string = "8082"

type Config struct {
	Host string
	Port string
}

func read_config(filename string) *Config {
	var conf Config
	if _, err := toml.DecodeFile(filename, &conf); err != nil {
		conf.Host = DEFAULT_HOST
		conf.Port = DEFAULT_PORT
		fmt.Println("Error processing", filename)
		fmt.Println("Default Host =", conf.Host)
		fmt.Println("Default Port =", conf.Port)
	}
	return &conf
}

type Payload struct {
	id     int
	length int
	data   []byte
}

func (p *Payload) complete() bool {
	if p.length > 0 && p.length == len(p.data) {
		return true
	}
	return false
}

func (p *Payload) add_bytes(bytes []byte) bool {
	if p.length == len(p.data)+len(bytes) {
		// after input bytes are appended payload
		// will be complete
		p.data = append(p.data, bytes...)
		p.data = p.data[1 : len(p.data)-1]
		p.length = p.length - 2
		return true
	} else {
		// more data will be recieved
		p.data = append(p.data, bytes...)
		return false
	}
}

func init_payload(bytes []byte) *Payload {
	// create new Payload objec
	return &Payload{length: int(bytes[0]), data: []byte{}}
}

func open_socket(port string) (net.Conn, net.Listener) {
	// listen on all interfaces
	listen, _ := net.Listen("tcp", ":"+port)
	// accept connection on port
	conn, _ := listen.Accept()
	return conn, listen
}

func payload_recieved_message(payload *Payload, total int) {
	fmt.Println("\nrecieved packet")
	fmt.Println("rssi", int8(payload.data[0]))
	fmt.Println("size", payload.length, len(payload.data))
	fmt.Println("data", payload.data)
	fmt.Println("count", total, "\n")
}

func read_data(conn net.Conn) {

	var payload *Payload
	payloads := []*Payload{}
	complete := true
	new_stream := true

	// create buffered io reader object with connection
	buffer := bufio.NewReader(conn)
	for {
		// read bytes until 0xFF is encountered
		// the first byte sent will be the length
		// so all bytes will be read into correct payload object
		bytes, error := buffer.ReadBytes(0xFF)
		if error == nil {
			fmt.Println(bytes)
			if new_stream == true && len(bytes) == 2 {
				if bytes[0] == 0x00 && bytes[1] == 0xFF {
					fmt.Println("\nclose connection", time.Now(), "\n")
					break
				}
			}
			new_stream = false

			// if last bytes recieved finished the message
			// intialize a new payload object
			if complete {
				payload = init_payload(bytes)
			}
			// copy bytes and update if we are finished
			// for this object
			complete = payload.add_bytes(bytes)
			if complete {
				// add completed packets to be parsed
				payloads = append(payloads, payload)
				payload_recieved_message(payload, len(payloads))
				new_stream = true
			}
		}
	}
}

func main() {
	conf := read_config("test-config.toml")

	listener, _ := net.Listen("tcp", ":"+conf.Port)
	for {
		conn, _ := listener.Accept()
		if conn != nil {
			read_data(conn)
			conn.Close()
		}
	}
	listener.Close()
}
