#!/bin/bash
sudo umount /dev/sda1
echo type=b | sudo sfdisk /dev/sda
sudo mkfs.vfat -n PHOTOBOX /dev/sda1

