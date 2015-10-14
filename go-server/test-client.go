package main

import "net"
import "fmt"
import "bufio"
import "os"

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
}

func main() {

	// connect to this socket
	conn, _ := net.Dial("tcp", "meyersj.com:8082")

	// send test data
	run_test(conn)
}
