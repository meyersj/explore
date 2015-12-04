package server

import (
	"../data"
	"../payload"
	"fmt"
	"net"
)

type Handler struct {
	DBClient   *data.DBClient
	Conn       net.Conn
	Dispatcher chan *Broadcast
	Router     *Router
}

func InitHandler(
	conn net.Conn,
	db *data.DBClient,
	dispatcher chan *Broadcast,
	router *Router,
) *Handler {
	return &Handler{
		DBClient:   db,
		Conn:       conn,
		Dispatcher: dispatcher,
		Router:     router,
	}
}

func (h *Handler) BeaconLookup(p *payload.Payload) {
	res := payload.Build(0x02, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 4 {
		rssi := int8(message.Structures[0][0])
		device := string(message.Structures[1])
		key := string(message.Structures[2])
		update := &data.BeaconLookup{Device: device, Beacon: key, Rssi: int(rssi)}
		flag, data := h.DBClient.BeaconLookup(update)
		fmt.Println("BEACON LOOKUP", device, rssi, string(data))
		res = payload.Build(flag, data)
	} else {
		fmt.Println("ERROR: BEACON UPDATE: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) BeaconRegister(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		name := string(message.Structures[0])
		key := string(message.Structures[1])
		h.DBClient.BeaconRegister(string(key), name)
		response := key + "\t" + name
		fmt.Println("REGISTER BEACON", key, name)
		res = payload.Build(0x00, []byte(response))
	} else {
		fmt.Println("ERROR: REGISTER BEACON: not parsed properly")
	}
	h.Conn.Write(res)
}

func (h *Handler) SendBroadcast(p *payload.Payload) {
	res := payload.Build(0x01, []byte("error"))
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 4 {
		device := string(message.Structures[0])
		client_name := string(message.Structures[1])
		client_message := string(message.Structures[2])
		key := string(message.Structures[3])
		fmt.Println("BROADCAST MESSAGE", device, key, client_message)
		msg := &data.Broadcast{
			Device:  device,
			User:    client_name,
			Beacon:  key,
			Message: client_message,
		}
		beacon_name := "Test"
		h.DBClient.SendBroadcast(msg)
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

func (h *Handler) JoinChannel(p *payload.Payload) (string, string) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := string(message.Structures[0])
		key := string(message.Structures[1])
		h.Router.JoinChannel(InitActiveClient(device, h.Conn), key)
		fmt.Println("JOIN CHANNEL", device, key)
		res = payload.Build(0x00, []byte{})
		h.Conn.Write(res)
		return device, key
	} else {
		fmt.Println("JOIN CHANNEL", "failed to parse message")
		h.Conn.Write(res)
		return "", ""
	}
}

func (h *Handler) LeaveChannel(p *payload.Payload) {
	res := payload.Build(0x01, []byte{})
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := string(message.Structures[0])
		key := string(message.Structures[1])
		h.Router.LeaveChannel(device, key)
		fmt.Println("LEAVE CHANNEL", device, key)
		res = payload.Build(0x00, []byte{})
	} else {
		fmt.Println("LEAVE CHANNEL", "failed to parse message")
	}
	h.Conn.Write(res)
}
