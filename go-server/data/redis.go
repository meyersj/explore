package data

import (
	"encoding/binary"
	"fmt"
	"gopkg.in/redis.v3"
	"strconv"
	"time"
)

const (
	ACTIVE_CLIENTS = "active_clients"
	CLIENT_NAME    = "name"
	LAST_ACTIVE    = "last_active"
	BEACONS        = "registered_beacons"
)

type Client struct {
	client *redis.Client
}

type ClientUpdate struct {
	Device string
	Beacon string
	Rssi   int
}

func InitClient() *Client {
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "",
		DB:       0,
	})
	return &Client{client: client}
}

func (c *Client) Set(key string, value string, timeout time.Duration) {
	err := c.client.Set(key, value, timeout).Err()
	if err != nil {
		fmt.Println("Error:", err)
	}
}

func (c *Client) RegisterClient(device string, name string) {
	now := time.Now()
	secs := now.Unix()
	client_key := "client:" + device
	err := c.client.SAdd(ACTIVE_CLIENTS, client_key).Err()
	if err != nil {
		fmt.Println("Error:", err)
	} else {
		c.client.HSet(client_key, CLIENT_NAME, name)
		c.client.HSet(client_key, LAST_ACTIVE, strconv.FormatInt(secs, 10))
	}
	c.GetStatus()
}

func (c *Client) RegisterBeacon(key string, name string, coordinates string) {
	c.client.HSet(BEACONS, "beacon:"+key, name+":"+coordinates)
}

func (c *Client) ClientUpdate(update *ClientUpdate) []byte {
	// update last_active entry for client
	client_key := "client:" + update.Device
	beacon_key := "beacon:" + update.Beacon
	now := time.Now()
	secs := strconv.FormatInt(now.Unix(), 10)
	data := strconv.Itoa(update.Rssi) + " " + secs
	c.client.HSet(client_key, LAST_ACTIVE, secs)
	c.client.HSet(client_key, beacon_key, data)

	data, e := c.client.HGet(BEACONS, beacon_key).Result()
	if e == nil {
		bytes := []byte(data)
		length := make([]byte, 4)
		binary.BigEndian.PutUint32(length, uint32(len(bytes)))
		return append(append(length, 0x00), bytes...)
	}
	return []byte{0x00, 0x00, 0x00, 0x01, 0x01}
}

func (c *Client) Get(key string) string {
	result := c.client.Get(key).String()
	fmt.Println(result)
	return result
}

func (c *Client) GetStatus() {
	members, _ := c.client.SMembers(ACTIVE_CLIENTS).Result()
	beacons, _ := c.client.HGetAll(BEACONS).Result()
	if members != nil && beacons != nil {
		for i := 0; i < len(beacons); i += 2 {
			for j := 0; j < len(members); j++ {
				data, e := c.client.HGet(members[j], beacons[i]).Result()
				if e == nil {
					fmt.Println(members[j], beacons[i], data)
				}
			}
		}
		//fmt.Println(beacons)
	}
}
