package payload

import (
	"fmt"
)

type Structure struct {
	AdvType byte
	Data    []byte
}

func (s *Structure) Print() {
	fmt.Print("  ", fmt.Sprintf("%0#2x", s.AdvType))
	fmt.Println(" ", fmt.Sprintf("% x", s.Data))
}

//func (s *Structure) ParseEddyStoneUID() (uid []byte, instance []byte) {
//	uid = s.Data[5 : 5+10]
//	instance = s.Data[5+10 : 5+10+6]
//	return uid, instance
//}

func InitStructure(bytes []byte) *Structure {
	return &Structure{AdvType: bytes[0], Data: bytes[1:len(bytes)]}
}

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

	// recursively parse advertisement data
	a.parse_rec(0, 0, bytes[1:len(bytes)])
}

//func (a *Advertisement) IsEddyStone() bool {
//	if len(a.Structures) >= 2 &&
//		a.Structures[1].AdvType == 0x03 &&
//		bytes.Equal(a.Structures[1].Data[0:2], EDDYSTONE_UID) {
//		return true
//	}
//	return false
//}

//func (a *Advertisement) ParseEddyStone() {
//	if !a.IsEddyStone() {
//		return 0xFF
//	}
//	return a.Structures[2].Data[2]
//}

//func (a *Advertisement) IsEddyStoneUID() bool {
//
//	frame_type := a.GetEddyStoneType()
//	if frame_type != 0xFF && frame_type == 0x00 {
//		return true
//	}

//advType := a.Structures[2].AdvType
//serviceUUID := a.Structures[2].Data

//if advType == 0x16 && serviceUUID[2] == 0x00 {
//EddyStoneUID
//	fmt.Println("EddyStoneUID"
//}
//bytes.Equal(a.Structures[1].Data[0:2], EDDYSTONE_UID) {
//}

func (a *Advertisement) parse_rec(offset int, count int, bytes []byte) {
	if offset >= len(bytes) || int(bytes[offset]) <= 0 {
		return
	}
	length := int(bytes[offset])
	offset = offset + 1
	a.Structures = append(a.Structures, InitStructure(bytes[offset:offset+length]))
	a.parse_rec(offset+length, count+1, bytes)
}

func (a *Advertisement) Print() {
	fmt.Println("Rssi:", a.Rssi)
	//if a.IsEddyStone() {
	fmt.Println("Structures:")
	for i := 0; i < len(a.Structures); i++ {
		a.Structures[i].Print()
	}
	//}
	fmt.Println("")
}

func InitAdvertisement(bytes []byte) *Advertisement {
	adv := Advertisement{}
	adv.Parse(bytes)
	return &adv
}
