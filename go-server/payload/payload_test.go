package payload

import (
	"bufio"
	"bytes"
	"net"
	"testing"
	"time"
)

type Runner func(*testing.T, *bufio.Reader)

func socket_tester(t *testing.T, runner Runner, payload []byte) {
	// start server in separate thread and wait for it to start
	go start_server(t, runner)
	time.Sleep(time.Millisecond * 50)
	// open connection
	conn, _ := net.Dial("tcp", "127.0.0.1:8082")
	if conn == nil {
		t.Fatalf("Failed to accept client connection")
	}
	// write data to socket
	// the runner function will read the test data from that socket
	// to simulate communication with a client
	conn.Write(payload)
	// let runner finish reading data
	time.Sleep(time.Millisecond * 50)
}

func start_server(t *testing.T, runner Runner) {
	listener, _ := net.Listen("tcp", ":8082")
	if listener == nil {
		t.Fatalf("Failed to start server listener")
	}
	for {
		conn, _ := listener.Accept()
		if conn == nil {
			t.Fatalf("Failed to accept connection to client")
		}
		buffer := bufio.NewReader(conn)
		// run test function with opened buffer
		runner(t, buffer)
	}

}

func runner_InitPayload(t *testing.T, buffer *bufio.Reader) {
	p, _ := Read(buffer)
	if p == nil {
		t.Fatalf("payload is empty")
	}
	if len(p.Data) != 4 {
		t.Fatalf("payload.Data length incorrect")
	}
	if !bytes.Equal(p.Flags, []byte{0x01, 0x02, 0x03, 0x04}) {
		t.Fatalf("payload.Flags incorrect")
	}
	if !bytes.Equal(p.Data, []byte{0xFF, 0xFF, 0xFF, 0xFF}) {
		t.Fatalf("payload.Data incorrect")
	}
}

func Test_InitPayload(t *testing.T) {
	header := []byte{0x00, 0x00, 0x00, 0x04, 0x01, 0x02, 0x03, 0x04}
	data := []byte{0xFF, 0xFF, 0xFF, 0xFF}
	socket_tester(t, runner_InitPayload, append(header, data...))
}

func runner_Build_NoData(t *testing.T, buffer *bufio.Reader) {
	p, _ := Read(buffer)
	if p == nil {
		t.Fatalf("payload is empty")
	}
	if len(p.Data) != 4 {
		t.Fatalf("payload.Data length incorrect")
	}
	if !bytes.Equal(p.Flags, []byte{0x01, 0x02, 0x03, 0x04}) {
		t.Fatalf("payload.Flags incorrect")
	}
	if !bytes.Equal(p.Data, []byte{0xFF, 0xFF, 0xFF, 0xFF}) {
		t.Fatalf("payload.Data incorrect")
	}
}

func Test_Build_NoData(t *testing.T) {
	expected_header := []byte{0x00, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00}
	p := Build(0xFF, []byte{})
	if len(p) != 8 {
		t.Fatalf("built payload length incorrect")
	}
	if !bytes.Equal(p, expected_header) {
		t.Fatalf("built payload not correction")
	}
}
func Test_Build_Data(t *testing.T) {
	expected_header := []byte{0x00, 0x00, 0x00, 0x04, 0xFF, 0x00, 0x00, 0x00}
	data := []byte{0xFF, 0xFF, 0xFF, 0xFF}
	p := Build(0xFF, data)
	if len(p) != 12 {
		t.Fatalf("built payload length incorrect")
	}
	if !bytes.Equal(p, append(expected_header, data...)) {
		t.Fatalf("built payload not correction")
	}
}
