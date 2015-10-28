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
		fmt.Println("REGISTER BEACON", key, name, coordinates)
		return payload.Build(0x00, []byte(key))
	}
	fmt.Println("ERROR: REGISTER BEACON: not parsed properly")
	return payload.Build(0x01, []byte{})
}

func RegisterClient(p *payload.Payload) []byte {
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := "client:" + string(message.Structures[0])
		name := string(message.Structures[1])
		client := data.InitClient()
		client.RegisterClient(device, name)
		fmt.Println("REGISTER CLIENT", device, name)
		return payload.Build(0x00, []byte{})
	}
	fmt.Println("ERROR: REGISTER CLIENT: not parsed properly")
	return payload.Build(0x01, []byte{})
}

func ClientUpdate(p *payload.Payload) []byte {
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		rssi := int8(message.Structures[0][0])
		device := "client:" + string(message.Structures[1])
		key := generate_beacon_key(message.Structures[2])
		client := data.InitClient()
		update := &data.ClientUpdate{Device: device, Beacon: key, Rssi: int(rssi)}
		flag, data := client.ClientUpdate(update)
		fmt.Println("CLIENT UPDATE", device, key, rssi)
		return payload.Build(flag, data)

	}
	fmt.Println("ERROR: CLIENT UPDATE: not parsed properly")
	return payload.Build(0x02, []byte{})
}

func PutMessage(p *payload.Payload) []byte {
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		device := "client:" + string(message.Structures[0])
		client_message := string(message.Structures[1])
		key := generate_beacon_key(message.Structures[2])
		fmt.Println("\n"+device, key, client_message, "\n")
		client := data.InitClient()
		msg := &data.ClientMessage{Device: device, Beacon: key, Message: client_message}
		client_name, beacon_name := client.PutMessage(msg)
		display := "<" + beacon_name + "> " + client_name + ": " + client_message
		return payload.Build(0x00, []byte(display))
	}
	return payload.Build(0x01, []byte("error"))

}
