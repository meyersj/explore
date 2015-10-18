package payload

import (
	"fmt"
)

type Structure struct {
	Type byte
	Data []byte
}

func (s *Structure) Print() {
	fmt.Print("  ", fmt.Sprintf("%0#2x", s.Type))
	fmt.Println(" ", fmt.Sprintf("% x", s.Data))
}

func InitStructure(bytes []byte) *Structure {
	return &Structure{Type: bytes[0], Data: bytes[1:len(bytes)]}
}
