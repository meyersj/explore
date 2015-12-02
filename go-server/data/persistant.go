package data

import (
	"database/sql"
	"encoding/binary"
	"fmt"
	_ "github.com/lib/pq"
	"gopkg.in/redis.v3"
	"log"
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

func InitClient(address string) *Client {
	client := redis.NewClient(&redis.Options{
		Addr:     address,
		Password: "",
		DB:       0,
	})

	_, err := client.Ping().Result()
	if err != nil {
		panic(fmt.Sprintf(" Failed to ping redis: %v", err))
	}
	return &Client{client: client}
}

func (c *Client) RegisterBeacon(key string, name string, lat string, lon string) {
	db, err := sql.Open("postgres", "postgres://jeff:password@localhost/explore")
	if err != nil {
		log.Fatal(db)
	}
	insert := "INSERT INTO beacons VALUES ($1, $2, $3, $4)"
	result, err := db.Query(insert, key, name, lat, lon)
	if err == nil {
		log.Println(result)
	} else {
		log.Fatal(err)
	}
}

func (c *Client) ClientUpdate(update *ClientUpdate) (byte, []byte) {
	name := c.ClientUpdatePostgres(update)
	now := time.Now()
	secs := strconv.FormatInt(now.Unix(), 10)
	//data := strconv.Itoa(update.Rssi) + " " + secs
	//c.client.HSet(update.Device, LAST_ACTIVE, secs)
	//c.client.HSet(update.Device, update.Beacon, data)
	//data, e := c.client.HGet(BEACONS, update.Beacon).Result()
	if name != "" {
		//	name := strings.Split(data, "\t")[0]
		response := secs + "\t" + update.Beacon + "\t" + name
		return 0x00, []byte(response)
	}
	return 0x01, []byte{}
}

func (c *Client) ClientUpdatePostgres(update *ClientUpdate) string {
	db, oerr := sql.Open("postgres", "postgres://jeff:password@localhost/explore")
	if oerr != nil {
		log.Fatal(oerr)
	}
	query := "SELECT name, lat, lon FROM beacons WHERE beacon = $1"
	log.Println(update.Beacon)
	var name string
	var lat, lon float64
	qerr := db.QueryRow(query, update.Beacon).Scan(&name, &lat, &lon)
	if qerr == sql.ErrNoRows {
		log.Print(qerr)
		return ""
	} else {
		log.Print(name)
		return name
	}
}

func (c *Client) GetBeacons() []string {
	beacons, e := c.client.HGetAll(BEACONS).Result()
	results := []string{}
	if e == nil {
		for i := 0; i < len(beacons); i += 2 {
			key := beacons[i]
			data := beacons[i+1]
			results = append(results, key+"\t"+data)
			fmt.Println(beacons[i])
		}
		return results
	}
	return []string{}
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

func (c *Client) BroadcastMessage(message *ClientMessage) string {
	db, err := sql.Open("postgres", "postgres://jeff:password@localhost/explore")
	if err != nil {
		log.Fatal(db)
		return ""
	}
	insert := "INSERT INTO broadcasts VALUES ($1, $2, $3, $4)"
	result, err := db.Query(
		insert,
		message.Beacon, message.Device, message.User, message.Message,
	)
	if err == nil {
		log.Println(result)
	} else {
		log.Fatal(err)
	}
	return ""
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

func BuildCoordinates(coord []byte) (string, string) {
	lat := math.Float64frombits(binary.BigEndian.Uint64(coord[0:8]))
	lon := math.Float64frombits(binary.BigEndian.Uint64(coord[8:16]))
	lat_string := strconv.FormatFloat(lat, 'f', 5, 64)
	lon_string := strconv.FormatFloat(lon, 'f', 5, 64)
	return lat_string, lon_string
}

func BuildClientKey(device []byte) string {
	return string(device) //"client:" + string(device)
}

func BuildBeaconKey(mac []byte) string {
	return string(mac) //"beacon:" + strings.Replace(string(mac), ":", "", -1)
}
