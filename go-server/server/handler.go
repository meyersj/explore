package server

import (
	"../data"
	"../payload"
	"fmt"
	"net"
	"strings"
)

type Handler struct {
	Client *data.Client
	Conn   net.Conn
}

func InitHandler(redis string, conn net.Conn) *Handler {
	client := data.InitClient(redis)
	return &Handler{Client: client, Conn: conn}
}

func (h *Handler) RegisterBeacon(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		name := string(message.Structures[0])
		key := data.BuildBeaconKey(message.Structures[2])
		lat := message.Structures[1][0:8]
		lon := message.Structures[1][8:16]
		coordinates := data.BuildCoordinates(lat, lon)
		h.Client.RegisterBeacon(key, name, coordinates)
		response := key + "\t" + name + "\t" + coordinates
		fmt.Println("REGISTER BEACON", key, name, coordinates)
		res = payload.Build(0x00, []byte(response))
	} else {
		fmt.Println("ERROR: REGISTER BEACON: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) ClientUpdate(p *payload.Payload) {
	res := payload.Build(0x02, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		rssi := int8(message.Structures[0][0])
		device := data.BuildClientKey(message.Structures[1])
		key := data.BuildBeaconKey(message.Structures[2])
		update := &data.ClientUpdate{Device: device, Beacon: key, Rssi: int(rssi)}
		flag, data := h.Client.ClientUpdate(update)
		fmt.Println("CLIENT UPDATE", device, key, rssi)
		res = payload.Build(flag, data)
	} else {
		fmt.Println("ERROR: CLIENT UPDATE: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) PutMessage(p *payload.Payload) {
	res := payload.Build(0x01, []byte("error"))
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 4 {
		device := data.BuildClientKey(message.Structures[0])
		client_name := string(message.Structures[1])
		client_message := string(message.Structures[2])
		key := data.BuildBeaconKey(message.Structures[3])
		fmt.Println("\n"+device, key, client_message, "\n")
		msg := &data.ClientMessage{
			Device:  device,
			User:    client_name,
			Beacon:  key,
			Message: client_message,
		}
		beacon_name := h.Client.PutMessage(msg)
		display := beacon_name + "\t" + client_name + "\t" + client_message
		res = payload.Build(0x00, []byte(display))
	}
	h.Conn.Write(res)
}

func (h *Handler) GetMessage(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 1 {
		key := data.BuildBeaconKey(message.Structures[0])
		messages, _ := h.Client.GetMessage(key)
		response := strings.Join(messages, "\n")
		fmt.Println("GET MESSAGE", key, response)
		res = payload.Build(0x00, []byte(response))
	} else {
		fmt.Println("GET MESSAGE", "failed to parse message")
	}
	h.Conn.Write(res)
}

func (h *Handler) GetBeacons(p *payload.Payload) {
	beacons := h.Client.GetBeacons()
	response := strings.Join(beacons, "\n")
	fmt.Println("GET BEACONS", response)
	h.Conn.Write(payload.Build(0x00, []byte(response)))
}
