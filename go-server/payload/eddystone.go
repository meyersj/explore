package payload

// https://github.com/google/eddystone/blob/master/protocol-specification.md

import (
	"bytes"
	"fmt"
)

type EddyStoneInterface interface {
	String() string
}

type EddyStoneUID struct {
	Uid      []byte
	Instance []byte
	Rssi     int8
	*Message
}

func InitEddyStoneUID(serviceUUID []byte, m *Message, rssi int8) *EddyStoneUID {
	uid := serviceUUID[4 : 4+10]           // 10 bytes from offset 4
	instance := serviceUUID[4+10 : 4+10+6] // 6 bytes from offset 14
	return &EddyStoneUID{Uid: uid, Instance: instance, Message: m, Rssi: rssi}
}

func (e *EddyStoneUID) String() string {
	return "uid: " + fmt.Sprintf("%0 x", e.Uid) +
		", instance: " + fmt.Sprintf("%0 x", e.Instance)
}

type EddyStoneURL struct {
	raw []byte
}

func InitEddyStoneURL(serviceUUID []byte) *EddyStoneURL {
	return &EddyStoneURL{raw: serviceUUID}
}

func (e *EddyStoneURL) String() string {
	return "url: " + fmt.Sprintf("%0 x", e.raw)
}

type EddyStoneTLM struct {
	raw []byte
}

func InitEddyStoneTLM(serviceUUID []byte) *EddyStoneTLM {
	return &EddyStoneTLM{raw: serviceUUID}
}

func (e *EddyStoneTLM) String() string {
	return "tlm: " + fmt.Sprintf("%0 x", e.raw)
}

func ParseEddyStone(rssi int8, m *Message) (bool, EddyStoneInterface) {
	if len(m.Structures) >= 3 &&
		m.Structures[1][0] == 0x03 &&
		bytes.Equal(m.Structures[1][1:3], EDDYSTONE_SERVICE) {

		if m.Structures[2][0] == 0x16 {
			if m.Structures[2][3] == EDDYSTONE_UID_FRAME {
				return true, InitEddyStoneUID(m.Structures[2][1:], m, rssi)
			} else if m.Structures[2][3] == EDDYSTONE_URL_FRAME {
				return true, InitEddyStoneURL(m.Structures[2][1:])
			} else if m.Structures[2][3] == EDDYSTONE_TLM_FRAME {
				return true, InitEddyStoneTLM(m.Structures[2][1:])
			}
		}
	}
	return false, nil
}
