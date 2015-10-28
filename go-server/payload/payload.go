package payload

import (
	"bufio"
	"encoding/binary"
	"fmt"
)

// represents a single complete message
type Payload struct {
	Flags []byte
	Data  []byte
}

func ReadPayload(buffer *bufio.Reader) *Payload {
	length := make([]byte, 4)
	flags := make([]byte, 4)
	buffer.Read(length)
	buffer.Read(flags)
	data := make([]byte, int(binary.BigEndian.Uint32(length)))
	buffer.Read(data)
	return &Payload{Flags: flags, Data: data}
}

func (p *Payload) Print() {
	fmt.Println("flags", p.Flags)
	fmt.Println("data", p.Data, "\n")
}
