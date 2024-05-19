#!/bin/sh
service cups stop
rm -f /var/spool/cups/[cd]*
rm -f /var/spool/cups/tmp/*
service cups start
cupsenable CP910
echo done.
