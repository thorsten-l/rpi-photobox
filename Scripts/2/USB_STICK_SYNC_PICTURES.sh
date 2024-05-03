#!/bin/bash
if [ -d /media/pi/PHOTOBOX ]; then
  echo sync pictures to photobox usb stick
  rsync -uvr --size-only --progress /home/pi/photobox/var/ /media/pi/PHOTOBOX/
else
  echo no photobox usb stick found
fi

