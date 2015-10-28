package data

import (
	"crypto/sha1"
	"encoding/binary"
	"fmt"
	"gopkg.in/redis.v3"
	"math"
	"strconv"
	"strings"
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

type ClientMessage struct {
	Device  string
	Beacon  string
	Message string
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
	err := c.client.SAdd(ACTIVE_CLIENTS, device).Err()
	if err != nil {
		fmt.Println("Error:", err)
	} else {
		c.client.HSet(device, CLIENT_NAME, name)
		c.client.HSet(device, LAST_ACTIVE, strconv.FormatInt(secs, 10))
	}
}

func (c *Client) RegisterBeacon(key string, name string, coordinates string) {
	c.client.HSet(BEACONS, key, name+":"+coordinates)
}

func (c *Client) ClientUpdate(update *ClientUpdate) (byte, []byte) {
	now := time.Now()
	secs := strconv.FormatInt(now.Unix(), 10)
	data := strconv.Itoa(update.Rssi) + " " + secs
	c.client.HSet(update.Device, LAST_ACTIVE, secs)
	c.client.HSet(update.Device, update.Beacon, data)
	data, e := c.client.HGet(BEACONS, update.Beacon).Result()
	if e == nil {
		response := update.Beacon + "|" + data
		return 0x00, []byte(response)
	}
	return 0x01, []byte{}
}

func (c *Client) Get(key string) string {
	result := c.client.Get(key).String()
	fmt.Println(result)
	return result
}

func (c *Client) PutMessage(message *ClientMessage) (string, string) {
	c.client.LPush("messages:"+message.Beacon, message.Device+"|"+message.Message)
	beacon, e := c.client.HGet(BEACONS, message.Beacon).Result()
	name := "beacon"
	if e == nil {
		data := strings.Split(beacon, ":")
		name = data[0]
	}
	client, e := c.client.HGet(message.Device, CLIENT_NAME).Result()
	if e == nil {
		return client, name
	}
	return "anon", name
}

func BuildCoordinates(lat []byte, lon []byte) string {
	lat_float := math.Float64frombits(binary.BigEndian.Uint64(lat))
	lon_float := math.Float64frombits(binary.BigEndian.Uint64(lon))
	lat_string := strconv.FormatFloat(lat_float, 'f', 5, 64)
	lon_string := strconv.FormatFloat(lon_float, 'f', 5, 64)
	return lat_string + " " + lon_string
}

func BuildClientKey(device []byte) string {
	return "client:" + string(device)
}

func BuildBeaconKey(advertisement []byte) string {
	hash := sha1.Sum(advertisement)
	hex := fmt.Sprintf("%0x", hash)
	return "beacon:" + hex
}
