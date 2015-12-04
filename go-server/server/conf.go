package server

import (
	"fmt"
	"github.com/BurntSushi/toml"
)

const DEFAULT_PORT string = "8082"
const DEFAULT_POSTGRES string = "postgres://jeff:password@localhost/explore"

type Config struct {
	Port     string
	Postgres string
}

// create and return Config data
func Read_config(filename string) *Config {
	var conf Config
	if _, err := toml.DecodeFile(filename, &conf); err != nil {
		conf.Port = DEFAULT_PORT
		conf.Postgres = DEFAULT_POSTGRES
		fmt.Println("Failure processing config file: ", filename)
		fmt.Println("  default Port =", conf.Port)
		fmt.Println("  default Postgres =", conf.Postgres)
	}
	return &conf
}
