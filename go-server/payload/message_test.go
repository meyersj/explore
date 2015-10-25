package payload

import (
	"bytes"
	"testing"
)

func Test_InitMessage_valid1(t *testing.T) {
	data := []byte{1, 4, 5, 1, 0, 0, 0, 0}

	adv := InitMessage(data)
	if len(adv.Structures) != 2 {
		t.Fatalf("Length of structures does not equal 2")
	}
	if len(adv.Structures[0]) == 1 && adv.Structures[0][0] != 0x04 {
		t.Fatalf("Failed to parse first message")
	}
	if len(adv.Structures[1]) == 5 && adv.Structures[1][0] != 0x01 {
		t.Fatalf("Failed to parse second message")
	}
}

func Test_InitMessage_valid2(t *testing.T) {
	data := []byte{1, 2, 3, 1, 0, 0, 3, 2, 0, 0}
	adv := InitMessage(data)
	if len(adv.Structures) != 3 {
		t.Fatalf("Length of structures does not equal 3")
	}
	if len(adv.Structures[0]) == 1 && adv.Structures[0][0] != 0x02 {
		t.Fatalf("Failed to parse first message")
	}
	if len(adv.Structures[1]) == 3 && adv.Structures[1][0] != 0x01 {
		t.Fatalf("Failed to parse second message")
	}
	if len(adv.Structures[2]) == 3 && adv.Structures[2][0] != 0x02 {
		t.Fatalf("Failed to parse third message")
	}

}

func Test_InitMessage_valid3(t *testing.T) {
	data := []byte{1, 100, 4, 1, 0, 0, 0, 2, 2, 0, 5, 3, 0, 0, 0, 0}
	adv := InitMessage(data)
	if len(adv.Structures) != 4 {
		t.Fatalf("Length of structures does not equal 4")
	}
}

func Test_InitMessage_invalid(t *testing.T) {
	// length of second message data does not match length field
	data := []byte{1, 100, 5, 1, 0, 0, 0}
	adv := InitMessage(data)
	if len(adv.Structures) != 2 {
		t.Fatalf("Length of structures does not equal 2")
	}
	// expected length to equal 5
	if !bytes.Equal(adv.Structures[1], []byte{0x01, 0x00, 0x00, 0x00}) {
		t.Fatalf("Short message does not match test data")
	}
}
