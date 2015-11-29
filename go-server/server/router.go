package server

import (
	"../payload"
	"fmt"
	"net"
)

type Broadcast struct {
	ClientId   string
	ClientName string
	Message    string
	Beacon     string
}

type ActiveClient struct {
	Id   string
	Conn net.Conn
}

type Router struct {
	Clients map[string]*ActiveClient
	Beacons map[string][]string
}

func InitActiveClient(id string, conn net.Conn) *ActiveClient {
	return &ActiveClient{Id: id, Conn: conn}
}

func InitRouter() *Router {
	return &Router{
		Clients: make(map[string]*ActiveClient),
		Beacons: make(map[string][]string),
	}
}

func (r *Router) JoinChannel(client *ActiveClient, beacon string) {
	r.Clients[client.Id] = client
	_, bexists := r.Beacons[beacon]
	if !bexists {
		r.Beacons[beacon] = []string{client.Id}
	} else {
		// TODO check that client is not already in list
		r.Beacons[beacon] = append(r.Beacons[beacon], client.Id)
	}
}

func (r *Router) LeaveChannel(client string, beacon string) {
	_, exists := r.Clients[client]
	if exists {
		beacons, bexists := r.Beacons[beacon]
		if bexists {
			updated := []string{}
			for i := 0; i < len(beacons); i++ {
				if beacons[i] != client {
					updated = append(updated, beacons[i])
				}
			}
			r.Beacons[beacon] = updated
		}
		delete(r.Clients, client)
	}
}

func (r *Router) Broadcast(broadcast *Broadcast) {
	clients, exists := r.Beacons[broadcast.Beacon]
	if exists {
		msg := buildBroadcast(broadcast)
		fmt.Println("BROADCAST", broadcast.Beacon, broadcast.Message)
		for i := 0; i < len(clients); i++ {
			client, cexists := r.Clients[clients[i]]
			if cexists {
				fmt.Println("    -", client.Id)
				client.Conn.Write(msg)
			}
		}
	}
}

func buildBroadcast(broadcast *Broadcast) []byte {
	msg := broadcast.ClientName + "\t" + broadcast.Message
	return payload.Build(0x00, []byte(msg))
}
