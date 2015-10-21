package data

import (
	"fmt"
	"gopkg.in/redis.v3"
)

type Client struct {
	client *redis.Client
}

func InitClient() *Client {
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "",
		DB:       0,
	})
	return &Client{client: client}
}

func (c *Client) Set(key string, value string) {
	err := c.client.Set(key, value, 0).Err()
	if err != nil {
		fmt.Println("Error:", err)
	}
}

func (c *Client) Get(key string) string {
	result := c.client.Get(key).String()
	fmt.Println(result)
	return result
}
