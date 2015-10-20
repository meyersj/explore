package payload

import (
	"bytes"
	//"fmt"
	"testing"
)

// Each message must end in 0xFF
// First byte is Length field

func Test_InitPayload(t *testing.T) {
	payload := []byte{0x05, 0x01, 0x01, 0x01, 0xFF}
	p := InitPayload(payload)
	if p.Length != 5 {
		t.Fatalf("Length is payload is incorrect")
	}
}

func Test_AddBytes_basic(t *testing.T) {
	payload := []byte{0x05, 0x01, 0x01, 0x01, 0xFF}
	p := InitPayload(payload)
	complete := p.AddBytes(payload)
	if p.Length != 5 {
		t.Fatalf("Length is payload is incorrect")
	}
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if !bytes.Equal(p.Data, []byte{0x01, 0x01, 0x01}) {
		t.Fatalf("payload.Data does not match test data")
	}
}

func Test_AddBytes_delimiter1(t *testing.T) {
	payload1 := []byte{0x05, 0x01, 0xFF}
	payload2 := []byte{0x01, 0xFF}
	p := InitPayload(payload1)
	complete := p.AddBytes(payload1)
	if p.Length != 5 {
		t.Fatalf("Length is payload is incorrect")
	}
	if complete {
		t.Fatalf("payload should not be finished")
	}
	complete = p.AddBytes(payload2)
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if !bytes.Equal(p.Data, []byte{0x01, 0xFF, 0x01}) {
		t.Fatalf("payload.Data does not match test data")
	}
}

func Test_AddBytes_delimiter2(t *testing.T) {
	payload1 := []byte{0x05, 0x01, 0xFF}
	payload2 := []byte{0xFF}
	payload3 := []byte{0xFF}
	p := InitPayload(payload1)
	complete := p.AddBytes(payload1)
	complete = p.AddBytes(payload2)
	complete = p.AddBytes(payload3)
	if p.Length != 5 {
		t.Fatalf("Length is payload is incorrect")
	}
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if !bytes.Equal(p.Data, []byte{0x01, 0xFF, 0xFF}) {
		t.Fatalf("payload.Data does not match test data")
	}

	payload1 = []byte{0x05, 0x01, 0x01, 0x01, 0xFF}
	p = InitPayload(payload1)
	complete = p.AddBytes(payload1)
	if p.Length != 5 {
		t.Fatalf("Length is payload is incorrect")
	}
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if !bytes.Equal(p.Data, []byte{0x01, 0x01, 0x01}) {
		t.Fatalf("payload.Data does not match test data")
	}
}

/*
// represents a single complete message
type Payload struct {
	id     int
	Length int
	Data   []byte
}


func (p *Payload) AddBytes(bytes []byte) bool {
	if p.Length == len(p.Data)+len(bytes) {
		// after input bytes are appended payload
		// will be complete
		p.Data = append(p.Data, bytes...)
		p.Data = p.Data[1 : len(p.Data)-1]
		return true
	} else {
		// more data will be recieved
		p.Data = append(p.Data, bytes...)
		return false
	}
}

func (p *Payload) Print() {
	//fmt.Println("rssi", p.Rssi)
	fmt.Println("size", p.Length)
	fmt.Println("data", p.Data, "\n")
}

func InitPayload(bytes []byte) *Payload {
	// create new Payload object with correct defaults
	return &Payload{Length: int(bytes[0]), Data: []byte{}}
}
*/
