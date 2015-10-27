package payload

import (
	"encoding/binary"
	"fmt"
)

// represents a single complete message
type Payload struct {
	id     int
	Length int
	Flag   byte
	Data   []byte
}

func (p *Payload) AddBytes(bytes []byte) bool {
	if p.Length == len(p.Data)+len(bytes) {
		// after input bytes are appended payload
		// will be complete
		p.Data = append(p.Data, bytes...)
		// Flag is fifth byte after length
		p.Flag = p.Data[4]
		if len(p.Data) > 4 {
			// skip length+flag and delimiter bytes
			p.Data = p.Data[5 : len(p.Data)-1]
		}
		return true
	} else {
		// more data will be recieved
		p.Data = append(p.Data, bytes...)
		return false
	}
}

func (p *Payload) Print() {
	fmt.Println("size", p.Length)
	fmt.Println("data", p.Data, "\n")
}

func InitPayload(bytes []byte) *Payload {
	// create new Payload object with correct defaults
	length := int(binary.BigEndian.Uint32(bytes[0:4])) + 4
	return &Payload{Length: length, Data: []byte{}}
}
