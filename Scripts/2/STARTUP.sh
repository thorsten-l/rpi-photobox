#!/bin/sh

export DISPLAY=:0.0
cd /home/th

banner `date '+%Y-%m-%d'`
banner `date '+%H:%M:%S'`

echo sleep 5s
sleep 5
/usr/sbin/ip -br a

echo turning screen blank off
sleep 2
xset s off
xset -dpms
xset s noblank

sleep 2
echo turning mouse cursor off
# nohup unclutter -idle 0 &
sleep 5
