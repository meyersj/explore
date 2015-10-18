package payload

// https://github.com/google/eddystone/blob/master/protocol-specification.md

import (
	"bytes"
	"fmt"
)

type EddyStone interface {
	String() string
}

type EddyStoneUID struct {
	uid      []byte
	instance []byte
}

func InitEddyStoneUID(serviceUUID []byte) *EddyStoneUID {
	uid := serviceUUID[4 : 4+10]
	instance := serviceUUID[4+10 : 4+10+6]
	return &EddyStoneUID{uid: uid, instance: instance}
}

func (e *EddyStoneUID) String() string {
	return "uid: " + fmt.Sprintf("%0 x", e.uid) +
		", instance: " + fmt.Sprintf("%0 x", e.instance)
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

func ParseEddyStone(a *Advertisement) (bool, EddyStone) {
	if len(a.Structures) >= 3 &&
		a.Structures[1].Type == 0x03 &&
		bytes.Equal(a.Structures[1].Data[0:2], EDDYSTONE_UID) {

		if a.Structures[2].Type == 0x16 {
			if a.Structures[2].Data[2] == 0x00 {
				return true, InitEddyStoneUID(a.Structures[2].Data)
			} else if a.Structures[2].Data[2] == 0x10 {
				return true, InitEddyStoneURL(a.Structures[2].Data)
			} else if a.Structures[2].Data[2] == 0x20 {
				return true, InitEddyStoneTLM(a.Structures[2].Data)
			}
		}
	}
	return false, nil
}
