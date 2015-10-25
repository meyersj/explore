package payload

import (
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
		// Flag is second byte after Length
		p.Flag = p.Data[1]
		if len(p.Data) > 1 {
			// skip Length+Flag and Delimiter bytes
			p.Data = p.Data[2 : len(p.Data)-1]
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
	return &Payload{Length: int(bytes[0]), Data: []byte{}}
}
