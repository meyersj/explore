package payload

// https://github.com/google/eddystone/blob/master/protocol-specification.md
var EDDYSTONE_SERVICE = []byte{0xAA, 0xFE}
var EDDYSTONE_UID_FRAME = byte(0x00)
var EDDYSTONE_URL_FRAME = byte(0x10)
var EDDYSTONE_TLM_FRAME = byte(0x20)
