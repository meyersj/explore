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

func (c *Config) endpoint() string {
	return c.Host + ":" + c.Port
}

func run_test(conn net.Conn) {
	// payload = {1, 1, 1, 1, 1, 0xFF, 1, 1, 1, 1, 1} (contains delimiter)
	bytes := []byte{16, 99, 3, 1, 6, 0xFF} // 7
	conn.Write(bytes)

	bytes = []byte{4, 2, 2, 2, 0xFF} // 6
	conn.Write(bytes)

	bytes = []byte{3, 3, 3, 3, 0xFF} // 6
	conn.Write(bytes)

	// payload = {1, 1, 1, 1}
	bytes = []byte{6, 99, 2, 1, 1, 0xFF}
	conn.Write(bytes)

	// payload #1 = {1, 1, 1, 1, 1}
	// payload #2 = {3, 20}
	//bytes = []byte{7, 99, 1, 1, 1, 1, 0xFF, 4, 1, 20, 0xFF}
	//conn.Write(bytes)

	// payload = {30, 0xFF, 35, 40} (contains delimiter)
	//bytes = []byte{6, 99, 3}
	//conn.Write(bytes)
	//bytes = []byte{1, 1, 0xFF}
	//conn.Write(bytes)

	// send byte sequence to close connection
	bytes = []byte{0x00, 0xFF}
	conn.Write(bytes)
}

func main() {
	conf := read_config("../test-config.toml")

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
