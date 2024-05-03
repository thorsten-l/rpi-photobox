#!/bin/sh

cd /home/pi

echo turning screen blank off
sleep 2
xset s off
xset -dpms
xset s noblank

sleep 2
echo turning mouse cursor off
unclutter -idle 0 &

./gpiotest/bin/app

sleep 2

./photobox/bin/app
