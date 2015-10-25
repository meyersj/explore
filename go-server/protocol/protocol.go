package protocol

const (
	// Flag used by clients
	CLOSE_CONN      = 0x00
	REGISTER_CLIENT = 0x01
	REGISTER_BEACON = 0x02
	CLIENT_UPDATE   = 0x03
	GET_STATUS      = 0x04
	DELIMITER       = 0xFF
)
