package main

import (
	"fmt"
	"gopkg.in/redis.v3"
)

func NewClient() *redis.Client {
	client := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "", // no password set
		DB:       0,  // use default DB
	})

	pong, err := client.Ping().Result()
	fmt.Println(pong, err)
	return client
}

func set(client *redis.Client, key string, value string) {
	err := client.Set(key, value, 0).Err()
	if err != nil {
		fmt.Println("Error:", err)
	}
}

func get(client *redis.Client, key string) string {
	result := client.Get(key).String()
	fmt.Println(result)
	return result
}

func main() {
	client := NewClient()
	res := client.LRange("mylist", -10, -1)
	fmt.Println(res)

}
