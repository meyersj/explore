package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// http://www.freshblurbs.com/blog/2013/12/07/hello-web-golang.html

type Message struct {
	Title string
	Body  string
	Time  int32
}

func viewHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Content-type", "application/json")

	jsonMsg, err := getResponse()
	if err != nil {
		http.Error(w, "Oops", http.StatusInternalServerError)
	}
	fmt.Fprintf(w, jsonMsg)
}

func getResponse() (string, error) {
	unixtime := int32(time.Now().Unix())
	msg := Message{"Hi", "Hello Web!", unixtime}
	jbMsg, err := json.Marshal(msg)

	if err != nil {
		return "", err
	}

	jsonMsg := string(jbMsg[:]) // converting byte array to string
	return jsonMsg, nil
}

func start_server() {
	http.HandleFunc("/hello", viewHandler)
	http.ListenAndServe(":8083", nil)
}

func main() {
	start_server()
}
