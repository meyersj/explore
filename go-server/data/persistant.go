package data

import (
	"database/sql"
	_ "github.com/lib/pq"
	"log"
)

type DBClient struct {
	DB *sql.DB
}

type BeaconLookup struct {
	Device string
	Beacon string
	Rssi   int
}

type Broadcast struct {
	Device  string
	User    string
	Beacon  string
	Message string
}

func InitDBClient(uri string) *DBClient {
	db, err := sql.Open("postgres", uri)
	if err != nil {
		log.Fatal(db)
	}
	err = db.Ping()
	if err != nil {
		log.Fatal("Failed to ping postgres:", err)
	}
	return &DBClient{DB: db}
}

func (c *DBClient) BeaconRegister(key string, name string) {
	insert := "INSERT INTO beacons (beacon, name) VALUES ($1, $2)"
	_, err := c.DB.Exec(insert, key, name)
	if err != nil {
		log.Fatal(err)
	}
}

func (c *DBClient) BeaconLookup(update *BeaconLookup) (byte, []byte) {
	query := "SELECT name FROM beacons WHERE beacon = $1"
	name := ""
	c.DB.QueryRow(query, update.Beacon).Scan(&name)
	if name != "" {
		return 0x00, []byte(name)
	}
	return 0x01, []byte{}
}

func (c *DBClient) SendBroadcast(message *Broadcast) string {
	insert := "INSERT INTO broadcasts VALUES ($1, $2, $3, $4)"
	_, err := c.DB.Query(
		insert,
		message.Beacon, message.Device, message.User, message.Message,
	)
	if err != nil {
		log.Fatal("broadcast", err)
	}
	return ""
}

func (c *DBClient) Close() {
	c.DB.Close()
}
