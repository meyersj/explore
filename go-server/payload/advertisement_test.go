package payload

import (
	"bytes"
	//"fmt"
	"testing"
)

// a raw byte sequence is parsed as follows

//	1 byte			1 byte		n bytes		1 byte	n bytes
//	rssi(signed)	length		data		length	data

func Test_InitAdvertisement_valid1(t *testing.T) {
	data := []byte{100, 5, 1, 0, 0, 0, 0}

	adv := InitAdvertisement(data)
	if adv.Rssi != 100 {
		t.Fatalf("RSSI should be 100")
	}
	if len(adv.Structures) != 1 {
		t.Fatalf("Length of structures does not equal 3")
	}
	if adv.Structures[0].Type != 0x01 {
		t.Fatalf("Failed to parse Type=0x01 for first Structure")
	}
	if !bytes.Equal(adv.Structures[0].Data, []byte{0, 0, 0, 0}) {
		t.Fatalf("Parsed data does not match test data")
	}
}

func Test_InitAdvertisement_valid2(t *testing.T) {
	data := []byte{100, 3, 1, 0, 0, 3, 2, 0, 0}

	adv := InitAdvertisement(data)
	if adv.Rssi != 100 {
		t.Fatalf("RSSI should be 100")
	}
	if len(adv.Structures) != 2 {
		t.Fatalf("Length of structures does not equal 3")
	}
	if adv.Structures[0].Type != 0x01 {
		t.Fatalf("Failed to parse Type=0x01 for first Structure")
	}
	if adv.Structures[1].Type != 0x02 {
		t.Fatalf("Failed to parse Type=0x03 for first Structure")
	}
}

func Test_InitAdvertisement_invalid(t *testing.T) {
	data := []byte{100, 5, 1, 0, 0, 0} // length of data does not match length field
	adv := InitAdvertisement(data)
	if adv.Rssi != 100 {
		t.Fatalf("RSSI should be 100")
	}
	if len(adv.Structures) != 1 {
		t.Fatalf("Length of structures does not equal 1")
	}
}
