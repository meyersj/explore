package server

import (
	"fmt"
	"github.com/BurntSushi/toml"
)

const DEFAULT_HOST string = "127.0.0.1"
const DEFAULT_PORT string = "8082"
const DEFAULT_UID string = "82C816B8CB37D896830F"
const DEFAULT_REDIS string = "127.0.0.1:6379"

type Config struct {
	Host  string
	Port  string
	Uid   string
	Redis string
}

// create and return Config data
func Read_config(filename string) *Config {
	var conf Config
	if _, err := toml.DecodeFile(filename, &conf); err != nil {
		conf.Host = DEFAULT_HOST
		conf.Port = DEFAULT_PORT
		conf.Uid = DEFAULT_UID
		conf.Redis = DEFAULT_REDIS
		fmt.Println("Error processing", filename)
		fmt.Println("Default Host =", conf.Host)
		fmt.Println("Default Port =", conf.Port)
		fmt.Println("Default UID =", conf.Uid)
	}
	return &conf
}
