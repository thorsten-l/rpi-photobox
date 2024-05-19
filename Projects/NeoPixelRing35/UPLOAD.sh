#!/bin/bash
cd $HOME/.esptool
source ./bin/activate

# esptool.py --chip esp8266 --port /dev/ttyUSB0 erase_flash
esptool.py --chip esp8266 --port /dev/ttyUSB0 --baud 460800 --after hard_reset write_flash --flash_mode dout --flash_size detect 0x0 firmware.bin

picocom --imap lfcrlf -b 74880 /dev/ttyUSB0
