package payload

import (
	"fmt"
)

type Advertisement struct {
	Rssi       int8
	Structures []*Structure
}

func (a *Advertisement) Parse(bytes []byte) {
	if len(bytes) < 2 {
		return
	}
	a.Rssi = int8(bytes[0])
	a.Structures = []*Structure{}
	a.recursive_parse(0, 0, bytes[1:len(bytes)])
}

func (a *Advertisement) recursive_parse(offset int, count int, bytes []byte) {
	if offset >= len(bytes) || int(bytes[offset]) <= 0 {
		return
	}
	length := int(bytes[offset])
	offset = offset + 1
	if len(bytes) < offset+length {
		a.Structures = append(a.Structures, InitStructure(bytes[offset:len(bytes)]))
		return
	}
	a.Structures = append(a.Structures, InitStructure(bytes[offset:offset+length]))
	a.recursive_parse(offset+length, count+1, bytes)
}

func (a *Advertisement) Print() {
	//fmt.Println("Rssi:", a.Rssi)
	fmt.Println("Raw Structures:")
	for i := 0; i < len(a.Structures); i++ {
		a.Structures[i].Print()
	}
	fmt.Println("")
}

func InitAdvertisement(bytes []byte) *Advertisement {
	adv := Advertisement{}
	adv.Parse(bytes)
	return &adv
}
