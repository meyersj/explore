package server

import (
	"testing"
)

func Test_is_client_finished(t *testing.T) {
	// is_client_finished(bytes []byte, complete bool) bool
	var complete bool
	finished := []byte{0x00, 0xFF}
	not_finished := []byte{0x01, 0xFF}

	// new message
	complete = is_client_finished(finished, true)
	if !complete {
		t.Fatalf("Failed to recognize finish message (complete=true: %v", finished)
	}
	complete = is_client_finished(finished, false)
	if complete {
		t.Fatalf("False positive for finished signal (complete=false): %v",
			not_finished)
	}
	complete = is_client_finished(not_finished, true)
	if complete {
		t.Fatalf("False positive for finished signal (complete=true): %v",
			not_finished)
	}
	complete = is_client_finished(not_finished, false)
	if complete {
		t.Fatalf("False positive for finished signal (complete=false): %v",
			not_finished)
	}
}
