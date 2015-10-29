package main

import (
	"bufio"
	"fmt"
	"os"
)

func main() {
	fmt.Println("start")

	w := 
	w := bufio.NewWriter(os.Stdout)
	w.Write([]byte("hello\n"))
	w.Flush()
	//r := bufio.NewReader(os.Stdin)
	//fmt.Println(r)
	//data := make([]byte, 5)
	//r.Read(data)
	//fmt.Println(data)
	//x := r.ReadByte()
	//fmt.Println(x)
	//w.Flush()
}
