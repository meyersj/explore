package payload

import (
	"bufio"
	"encoding/binary"
	"fmt"
)

type Payload struct {
	Flags []byte
	Data  []byte
}

// payload
//
// | 4 bytes | 4 bytes | n bytes |
// | length  | flags   | data    |

func Read(buffer *bufio.Reader) (*Payload, string) {
	length := make([]byte, 4)
	flags := make([]byte, 4)
	n, e := buffer.Read(length)
	if n != 4 || e != nil {
		return nil, "Failed to read length field"
	}
	n, e = buffer.Read(flags)
	if n != 4 || e != nil {
		return nil, "Failed to read flags field"
	}
	data := make([]byte, int(binary.BigEndian.Uint32(length)))
	n, e = buffer.Read(data)
	if e != nil {
		return nil, "Failed to read flags field"
	}
	return &Payload{Flags: flags, Data: data}, ""
}

func Build(flag byte, data []byte) []byte {
	length := make([]byte, 4)
	flags := []byte{flag, 0x00, 0x00, 0x00}
	binary.BigEndian.PutUint32(length, uint32(len(data)))
	payload := append([]byte{}, length...)
	payload = append(payload, flags...)
	payload = append(payload, data...)
	return payload
}

func (p *Payload) Print() {
	fmt.Println("flags", p.Flags)
	fmt.Println("data", p.Data, "\n")
}
