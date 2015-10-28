package handler

import (
	"../data"
	"../payload"
	"crypto/sha1"
	"encoding/binary"
	"fmt"
	"math"
	"strconv"
)

func build_response(flag byte, data []byte) []byte {
	// payload
	//
	// | 4 bytes | 4 bytes | n bytes |
	// | length  | flags   | data    |
	length := make([]byte, 4)
	flags := []byte{flag, 0x00, 0x00, 0x00}
	binary.BigEndian.PutUint32(length, uint32(len(data)))
	response := append([]byte{}, length...)
	response = append(response, flags...)
	response = append(response, data...)
	fmt.Println(response)
	return response

}

func GetCoordinates(lat []byte, lon []byte) string {
	lat_float := math.Float64frombits(binary.BigEndian.Uint64(lat))
	lon_float := math.Float64frombits(binary.BigEndian.Uint64(lon))
	lat_string := strconv.FormatFloat(lat_float, 'f', 5, 64)
	lon_string := strconv.FormatFloat(lon_float, 'f', 5, 64)
	return lat_string + " " + lon_string
}

func generate_beacon_key(advertisement []byte) string {
	hash := sha1.Sum(advertisement)
	hex := fmt.Sprintf("%0x", hash)
	return "beacon:" + hex
}

func RegisterBeacon(p *payload.Payload) []byte {
	fmt.Println("REGISTER BEACON")
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		name := string(message.Structures[0])
		key := generate_beacon_key(message.Structures[2])
		lat := message.Structures[1][0:8]
		lon := message.Structures[1][8:16]
		coordinates := GetCoordinates(lat, lon)
		client := data.InitClient()
		client.RegisterBeacon(key, name, coordinates)
		return build_response(0x00, []byte(key))
	}
	return build_response(0x01, []byte{})
}

func RegisterClient(p *payload.Payload) []byte {
	fmt.Println("REGISTER CLIENT")
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := string(message.Structures[0])
		name := string(message.Structures[1])
		client := data.InitClient()
		client.RegisterClient(device, name)
		return build_response(0x00, []byte{})
	}
	return build_response(0x01, []byte{})
}

func ClientUpdate(p *payload.Payload) []byte {
	fmt.Println("CLIENT UPDATE")
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		rssi := int8(message.Structures[0][0])
		device := "client:" + string(message.Structures[1])
		key := generate_beacon_key(message.Structures[2])
		client := data.InitClient()
		update := &data.ClientUpdate{Device: device, Beacon: key, Rssi: int(rssi)}
		flag, data := client.ClientUpdate(update)
		return build_response(flag, data)
	}
	return build_response(0x02, []byte{})
}
