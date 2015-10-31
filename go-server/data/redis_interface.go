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
	MESSAGES       = "messages"
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
	User    string
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

func (c *Client) RegisterBeacon(key string, name string, coordinates string) {
	c.client.HSet(BEACONS, key, name+"\t"+coordinates)
}

func (c *Client) ClientUpdate(update *ClientUpdate) (byte, []byte) {
	now := time.Now()
	secs := strconv.FormatInt(now.Unix(), 10)
	data := strconv.Itoa(update.Rssi) + " " + secs
	c.client.HSet(update.Device, LAST_ACTIVE, secs)
	c.client.HSet(update.Device, update.Beacon, data)
	data, e := c.client.HGet(BEACONS, update.Beacon).Result()
	if e == nil {
		name := strings.Split(data, "\t")[0]
		response := secs + "\t" + update.Beacon + "\t" + name
		return 0x00, []byte(response)
	}
	return 0x01, []byte{}
}

func (c *Client) GetMessage(beacon string) ([]string, int) {
	key := MESSAGES + ":" + beacon
	size, e := c.client.LLen(key).Result()
	results := []string{}
	if e != nil {
		return results, 1
	}
	fmt.Println("size", size)
	data, e := c.client.LRange(key, 0, 20).Result()
	if e == nil {
		return data, 0
	}
	return results, 0
}

func (c *Client) PutMessage(message *ClientMessage) string {
	key := MESSAGES + ":" + message.Beacon
	now := time.Now()
	secs := strconv.FormatInt(now.Unix(), 10)
	value := message.Device + "\t"
	value += message.User + "\t"
	value += message.Message + "\t"
	value += secs
	c.client.LPush(key, value)
	beacon, e := c.client.HGet(BEACONS, message.Beacon).Result()
	name := "beacon"
	if e == nil {
		data := strings.Split(beacon, "\t")
		name = data[0]
	}
	return name
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
