#!/bin/bash
export PATH=/usr/bin

USB_ID=`lsusb | grep -iE "SELPHY|Photo Printer" | awk '{print $6}'`

if [[ -z "${USB_ID}" ]]; then
  echo NO_PRINTER_DETECTED
else
  echo "USB_ID=$USB_ID"
  SERIAL=`lsusb -v -d $USB_ID 2> /dev/null | grep -i serial | awk '{print $3}'`
  echo "SERIAL=$SERIAL"
  PRINTER_NAME=`lpstat -a -t | grep $SERIAL | awk '{print $3}' | sed -e 's/\://g'`
  echo "PRINTER_NAME=$PRINTER_NAME"
fi


