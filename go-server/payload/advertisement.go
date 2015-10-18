package payload

import (
	"bytes"
)

var EDDYSTONE_UID = []byte{0x16, 0xAA, 0xFE}

type Advertisement struct {
	raw        []byte
	structures [][]byte
	Eddystone  bool
	Uid        []byte
	Instance   []byte
}

func (a *Advertisement) parse() {
	a.structures = [][]byte{}
	if len(a.raw) < 2 {
		return
	}
	// recursively parse advertisement data
	a.parse_rec(0, 0)

	// if advertisement is an EddyStoneUID broadcast
	// grab uid and instnce fields
	// https://github.com/google/eddystone/blob/master/protocol-specification.md
	if len(a.structures) == 3 && len(a.structures[2]) >= 3 &&
		bytes.Equal(a.structures[2][0:3], EDDYSTONE_UID) {
		a.Eddystone = true
		a.Uid = a.structures[2][5 : 5+10]
		a.Instance = a.structures[2][5+10 : 5+10+6]
	} else {
		a.Eddystone = false
	}
}

func (a *Advertisement) parse_rec(offset int, count int) {
	if offset >= len(a.raw) || int(a.raw[offset]) <= 0 {
		return
	}

	length := int(a.raw[offset])
	offset = offset + 1
	a.structures = append(a.structures, []byte{})
	a.structures[count] = a.raw[offset : offset+length]
	a.parse_rec(offset+length, count+1)
}
