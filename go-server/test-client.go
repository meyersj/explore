package main

import "net"
import "fmt"

import "github.com/BurntSushi/toml"

const DEFAULT_HOST string = "127.0.0.1"
const DEFAULT_PORT string = "8082"

type Config struct {
	Host string
	Port string
}

func (c *Config) endpoint() string {
	return c.Host + ":" + c.Port
}

func run_test(conn net.Conn) {
	// payload = {1, 1, 1, 1, 1, 0xFF, 1, 1, 1, 1, 1} (contains delimiter)
	bytes := []byte{13, 1, 1, 1, 1, 1, 0xFF}
	conn.Write(bytes)

	bytes = []byte{1, 1, 1, 1, 1, 0xFF}
	conn.Write(bytes)

	// payload = {1, 1, 1, 1}
	bytes = []byte{6, 1, 1, 1, 1, 0xFF}
	conn.Write(bytes)

	// payload #1 = {1, 1, 1, 1, 1}
	// payload #2 = {3, 20}
	bytes = []byte{7, 1, 1, 1, 1, 1, 0xFF, 3, 20, 0xFF}
	conn.Write(bytes)

	// payload = {30, 0xFF, 35, 40} (contains delimiter)
	bytes = []byte{6, 30, 0xFF}
	conn.Write(bytes)
	bytes = []byte{35, 40, 0xFF}
	conn.Write(bytes)

	// send byte sequence to close connection
	bytes = []byte{0x00, 0xFF}
	conn.Write(bytes)
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

func main() {
	conf := read_config("test-config.toml")

	// connect to this socket
	conn, _ := net.Dial("tcp", conf.endpoint())

	if conn != nil {
		// send test data
		run_test(conn)
		conn.Close()
	} else {
		fmt.Println("Error: unable to connect to socket at", conf.endpoint())
	}
}
