#!/bin/sh

ssh pi@photobox 'export DISPLAY=:0.0; ./gpiotest/bin/app'