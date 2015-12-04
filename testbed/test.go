package main

import (
	"database/sql"
	"fmt"
	_ "github.com/lib/pq"
	"log"
)

func main() {
	fmt.Println("start")
	db, err := sql.Open("postgres", "postgres://jeff:password@localhost/explore")
	if err != nil {
		log.Fatal(db)
	} else {
		//age := 21
		_, err := db.Query("INSERT INTO gotest VALUES ($1, $2)", 3, "c")
		if err != nil {
			log.Fatal(err)
		}
		rows, err := db.Query("SELECT a, b FROM gotest")
		if err == nil {
			log.Println(rows.Columns())
			defer rows.Close()
			for rows.Next() {
				var a int
				var b string
				err = rows.Scan(&a, &b)
				log.Println(a, b)
			}
			err = rows.Err()
			if err != nil {
				log.Fatal(err)
			}
		} else {
			log.Fatal(err)
		}
	}
}
