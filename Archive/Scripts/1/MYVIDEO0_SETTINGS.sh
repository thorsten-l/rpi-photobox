#!/bin/sh
v4l2-ctl -c white_balance_temperature_auto=0
v4l2-ctl -c white_balance_temperature=5600
v4l2-ctl -c brightness=120
v4l2-ctl -c saturation=100
v4l2-ctl -c contrast=144
