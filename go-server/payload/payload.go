package payload

import (
	"fmt"
)

// represents a single complete message
type Payload struct {
	id     int
	Length int
	Data   []byte
	Rssi   int8
	Adv    *Advertisement
}

func (p *Payload) Parse() {
	if len(p.Data) > 1 {
		p.Rssi = int8(p.Data[0])
		p.Data = p.Data[1:len(p.Data)]
		p.Adv = &Advertisement{raw: p.Data}
		p.Adv.parse()
	}
}

func (p *Payload) Add_bytes(bytes []byte) bool {
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

func (p *Payload) PPrint() {
	fmt.Println("rssi", p.Rssi)
	fmt.Println("size", p.Length)
	fmt.Println("data", p.Data, "\n")
}

func InitPayload(bytes []byte) *Payload {
	// create new Payload object with correct defaults
	return &Payload{Length: int(bytes[0]), Data: []byte{}}
}
