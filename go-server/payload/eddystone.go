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
	*Advertisement
}

func InitEddyStoneUID(serviceUUID []byte, a *Advertisement) *EddyStoneUID {
	uid := serviceUUID[4 : 4+10]           // 10 bytes from offset 4
	instance := serviceUUID[4+10 : 4+10+6] // 6 bytes from offset 14
	return &EddyStoneUID{Uid: uid, Instance: instance, Advertisement: a}
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

func ParseEddyStone(a *Advertisement) (bool, EddyStoneInterface) {
	if len(a.Structures) >= 3 &&
		a.Structures[1].Type == 0x03 &&
		bytes.Equal(a.Structures[1].Data[0:2], EDDYSTONE_SERVICE) {

		if a.Structures[2].Type == 0x16 {
			if a.Structures[2].Data[2] == EDDYSTONE_UID_FRAME {
				return true, InitEddyStoneUID(a.Structures[2].Data, a)
			} else if a.Structures[2].Data[2] == EDDYSTONE_URL_FRAME {
				return true, InitEddyStoneURL(a.Structures[2].Data)
			} else if a.Structures[2].Data[2] == EDDYSTONE_TLM_FRAME {
				return true, InitEddyStoneTLM(a.Structures[2].Data)
			}
		}
	}
	return false, nil
}
