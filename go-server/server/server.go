package server

import (
	"../data"
	"../payload"
	"../protocol"
	"bufio"
	"encoding/binary"
	"fmt"
	"net"
	"strconv"
	//"strings"
	"time"
)

func is_client_finished(bytes []byte, complete bool) bool {
	// if bytes is of length two with contents {0x00, 0xFF}
	// then that is the close connection message
	// from client that it is finished so
	// return from function to start
	// listening for another client
	if complete && len(bytes) == 2 && bytes[0] == protocol.CLOSE_CONN {
		return true
	}
	return false
}

func handle_bytes(p *payload.Payload, bytes []byte, done bool) (
	*payload.Payload, bool) {

	// if last bytes recieved finished the message
	// intialize a new payload object
	if done {
		p = payload.InitPayload(bytes)
	}
	// copy bytes and update if we are finished
	// for this object and wait for a new
	// payload, or for the exit signal to close connection
	return p, p.AddBytes(bytes)
}

// main worker thread that handles communication with the client
// bytes are parsed into separate payloads and passed
// to a consumer thread
func Communicate(conn net.Conn, redis_chan chan *data.Message) {
	fmt.Println("\nopen connection", time.Now(), "\n")
	defer conn.Close()

	var p *payload.Payload
	done := true

	// create buffered io reader object with connection
	buffer := bufio.NewReader(conn)
	for {
		// read bytes until 0xFF is encountered
		// the first byte sent will be the length
		// so all bytes will be read into correct payload object
		bytes, error := buffer.ReadBytes(protocol.DELIMITER)
		if error == nil && len(bytes) > 0 {
			//fmt.Println(bytes)
			p, done = handle_bytes(p, bytes, done)
			if done {
				switch p.Flag {
				case protocol.CLOSE_CONN:
					fmt.Println("\nclose connection", time.Now(), "\n")
					return
				case protocol.REGISTER_CLIENT:
					conn.Write([]byte{handle_register_client(p)})
				case protocol.REGISTER_BEACON:
					conn.Write([]byte{handle_register_beacon(p)})
				case protocol.CLIENT_UPDATE:
					conn.Write([]byte{handle_client_update(p, redis_chan)})
				case protocol.GET_STATUS:
				}
			}
		}
	}
}

func get_eddystone_ident(e *payload.EddyStoneUID) (string, int) {
	instance := int(binary.BigEndian.Uint32(e.Instance[2:len(e.Instance)]))
	uid := fmt.Sprintf("%0x", e.Uid)
	return uid, instance
}

func RedisWriter(redis_chan chan *data.Message) {
	client := data.InitClient()
	for {
		message := <-redis_chan
		client.Set(message.Key, message.Value, message.Timeout)
		fmt.Println(message.Key, message.Value, message.Timeout)
	}
}

func handle_register_beacon(p *payload.Payload) byte {
	fmt.Println("REGISTER BEACON")
	message := payload.InitMessage(p.Data)

	if len(message.Structures) == 2 {
		name := string(message.Structures[0])
		adv := payload.InitMessage(message.Structures[1])
		valid, frame := payload.ParseEddyStone(0, adv)
		key := "beacon:" + fmt.Sprintf("%0x", message.Structures[1])
		if valid {
			switch frame.(type) {
			case *payload.EddyStoneUID:
				uid, instance := get_eddystone_ident(frame.(*payload.EddyStoneUID))
				key = "beacon:" + uid + "-" + strconv.Itoa(instance)
			}
		}
		client := data.InitClient()
		if client != nil {
			client.Set(key, name, 0)
			fmt.Println("SET", key, name, 0)
			return 0x00
		} else {
			return 0x01
		}
	}
	return 0x02
}

func handle_register_client(p *payload.Payload) byte {
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 2 {
		device := string(message.Structures[0])
		name := string(message.Structures[1])
		key := "client:" + device
		fmt.Println("REGISTER CLIENT:", device+"="+name)
		client := data.InitClient()
		client.Set(key, name, 0)
		return 0x00
	}
	return 0x01
}

func handle_client_update(p *payload.Payload, redis_chan chan *data.Message) byte {
	fmt.Println("CLIENT UPDATE")
	message := payload.InitMessage(p.Data)
	if len(message.Structures) == 3 {
		rssi := int8(message.Structures[0][0])
		device := string(message.Structures[1])
		key := "update:" + device + ":" + fmt.Sprintf("%0x", message.Structures[2])
		adv := payload.InitMessage(message.Structures[2])
		valid, frame := payload.ParseEddyStone(rssi, adv)
		if valid {
			switch frame.(type) {
			case *payload.EddyStoneUID:
				uid, instance := get_eddystone_ident(frame.(*payload.EddyStoneUID))
				key = "update:" + device + ":"
				key = key + uid + "-" + strconv.Itoa(instance)
			}
		}
		value := strconv.Itoa(int(rssi))
		redis_chan <- &data.Message{Key: key, Value: value, Timeout: time.Second * 60}
		return 0x00
	}
	return 0x01
}

/*
func PayloadReciever(
	input_chan chan *payload.Payload,
	redis_chan chan *payload.EddyStoneUID,
	uid []byte,
) {
	for {
		// recieve finished Payload from Communicate thread
		p := <-input_chan
		// parse Payload data into Advertisement structures
		adv := payload.InitAdvertisement(p.Data)
		// parse Advertisement using EddyStone protocol
		valid, frame := payload.ParseEddyStone(adv)
		if valid {
			switch frame.(type) {
			case *payload.EddyStoneUID:
				redis_chan <- frame.(*payload.EddyStoneUID)
			default:
			}
		}
	}
}
*/
