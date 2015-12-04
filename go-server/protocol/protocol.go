package protocol

const (
	// Protocol Message Types
	CLOSE_CONN        = 0x00
	BEACON_LOOKUP     = 0x01
	BEACON_REGISTER   = 0x02
	JOIN_CHANNEL      = 0x03
	LEAVE_CHANNEL     = 0x04
	SEND_BROADCAST    = 0x05
	RECEIVE_BROADCAST = 0x06
)
