package payload

import (
	"bytes"
	//"fmt"
	"testing"
)

// Each message must end in 0xFF
// First byte is Length field

func Test_InitPayload(t *testing.T) {
	payload := []byte{0x00, 0x00, 0x00, 0x03, 0x01, 0x01, 0xFF}
	p := InitPayload(payload)
	if p.Length != 7 {
		t.Fatalf("Length is payload is incorrect")
	}
}

func Test_AddBytes_basic(t *testing.T) {
	payload := []byte{0x00, 0x00, 0x00, 0x03, 0x01, 0x00, 0xFF}
	p := InitPayload(payload)
	complete := p.AddBytes(payload)
	if p.Length != 7 {
		t.Fatalf("Length is payload is incorrect")
	}
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if p.Flag != 0x01 {
		t.Fatalf("Flag does not eqaul 0x01")

	}
	if !bytes.Equal(p.Data, []byte{0x00}) {
		t.Fatalf("payload.Data does not match test data")
	}
}

func Test_AddBytes_delimiter1(t *testing.T) {
	payload1 := []byte{0x00, 0x00, 0x00, 0x05, 0x01, 0x00, 0xFF}
	payload2 := []byte{0x00, 0xFF}
	p := InitPayload(payload1)
	complete := p.AddBytes(payload1)
	if p.Length != 9 {
		t.Fatalf("Length is payload is incorrect")
	}
	if complete {
		t.Fatalf("payload should not be finished")
	}
	complete = p.AddBytes(payload2)
	if !complete {
		t.Fatalf("payload should be finished")
	}
	if !bytes.Equal(p.Data, []byte{0x00, 0xFF, 0x00}) {
		t.Fatalf("payload.Data does not match test data")
	}
}
