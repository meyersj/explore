package server

import (
	"../data"
	"../payload"
	"fmt"
	"net"
	"strings"
)

type Handler struct {
	Client     *data.Client
	Conn       net.Conn
	Dispatcher chan *Broadcast
	Router     *Router
}

func InitHandler(
	conn net.Conn,
	redis_client *data.Client,
	dispatcher chan *Broadcast,
	router *Router,
) *Handler {
	return &Handler{
		Client:     redis_client,
		Conn:       conn,
		Dispatcher: dispatcher,
		Router:     router,
	}
}

func (h *Handler) RegisterBeacon(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		name := string(message.Structures[0])
		//key := data.BuildBeaconKey(message.Structures[2])
		key := string(message.Structures[2])
		lat, lon := data.BuildCoordinates(message.Structures[1])
		//h.Client.RegisterBeacon(key, name, coordinates)
		h.Client.RegisterBeacon(string(key), name, lat, lon)
		response := key + "\t" + name + "\t" + lat + "\t" + lon
		fmt.Println("REGISTER BEACON", key, name, lat, lon)
		res = payload.Build(0x00, []byte(response))
	} else {
		fmt.Println("ERROR: REGISTER BEACON: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) ClientUpdate(p *payload.Payload) {
	res := payload.Build(0x02, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 4 {
		rssi := int8(message.Structures[0][0])
		device := data.BuildClientKey(message.Structures[1])
		key := data.BuildBeaconKey(message.Structures[2])
		//advertisement := message.Structures[3]
		update := &data.ClientUpdate{Device: device, Beacon: key, Rssi: int(rssi)}
		flag, data := h.Client.ClientUpdate(update)
		fmt.Println("CLIENT UPDATE", device, key, rssi)
		res = payload.Build(flag, data)
	} else {
		fmt.Println("ERROR: CLIENT UPDATE: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) BroadcastMessage(p *payload.Payload) {
	res := payload.Build(0x01, []byte("error"))
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 4 {
		device := data.BuildClientKey(message.Structures[0])
		client_name := string(message.Structures[1])
		client_message := string(message.Structures[2])
		key := data.BuildBeaconKey(message.Structures[3])
		fmt.Println("BROADCAST MESSAGE", device, key, client_message)
		msg := &data.ClientMessage{
			Device:  device,
			User:    client_name,
			Beacon:  key,
			Message: client_message,
		}
		beacon_name := h.Client.PutMessage(msg)
		h.Client.BroadcastMessage(msg)
		display := beacon_name + "\t" + client_name + "\t" + client_message
		res = payload.Build(0x00, []byte(display))
		h.Conn.Write(res)
		h.Dispatcher <- &Broadcast{
			ClientId:   device,
			ClientName: client_name,
			Message:    client_message,
			Beacon:     key,
		}
	} else {
		h.Conn.Write(res)
	}
}

func (h *Handler) JoinChannel(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := data.BuildClientKey(message.Structures[0])
		key := data.BuildBeaconKey(message.Structures[1])
		h.Router.JoinChannel(InitActiveClient(device, h.Conn), key)
		fmt.Println("JOIN CHANNEL", device, key)
		res = payload.Build(0x00, []byte{})
	} else {
		fmt.Println("JOIN CHANNEL", "failed to parse message")
	}
	h.Conn.Write(res)
}

func (h *Handler) LeaveChannel(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := data.BuildClientKey(message.Structures[0])
		key := data.BuildBeaconKey(message.Structures[1])
		h.Router.LeaveChannel(device, key)
		fmt.Println("LEAVE CHANNEL", device, key)
		res = payload.Build(0x00, []byte{})
	} else {
		fmt.Println("LEAVE CHANNEL", "failed to parse message")
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
