package payload

import (
	"fmt"
)

type Message struct {
	Structures [][]byte
}

func (m *Message) Parse(bytes []byte) {
	if len(bytes) < 2 {
		return
	}
	m.Structures = [][]byte{}
	m.recursive_parse(0, 0, bytes[0:len(bytes)])
}

func (m *Message) recursive_parse(offset int, count int, bytes []byte) {
	if offset >= len(bytes) || int(bytes[offset]) <= 0 {
		return
	}
	length := int(bytes[offset])
	offset = offset + 1
	if len(bytes) < offset+length {
		m.Structures = append(m.Structures, bytes[offset:len(bytes)])
		return
	}
	m.Structures = append(m.Structures, bytes[offset:offset+length])
	m.recursive_parse(offset+length, count+1, bytes)
}

func (m *Message) Print() {
	fmt.Println("Structures:")
	for i := 0; i < len(m.Structures); i++ {
		fmt.Println(m.Structures[i])
		//m.Structures[i].Print()
	}
	fmt.Println("")
}

func InitMessage(bytes []byte) *Message {
	message := Message{}
	message.Parse(bytes)
	return &message
}
