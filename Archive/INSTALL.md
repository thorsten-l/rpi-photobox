# PhotoBox - Raspberry Pi OS (Debian 12, bookworm) - 64 Bit on Pi4

## Raspberry Pi Imager (1.8.5)

- Select Desktop
- write SD card

## First Boot RPI

### Remove ghotofs 

GPhotoFS automatically mounts USB damera devices, so `gphoto2` is not able to control the camera.

```
apt remove libgphoto2-6 libgphoto2-port12 gvfs-backends
apt autoremove -y
apt update
apt upgrade -y
```

### Install Bellsoft Java 11 LTS

```bash
wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | sudo apt-key add -
echo "deb [arch=arm64] https://apt.bell-sw.com/ stable main" | sudo tee /etc/apt/sources.list.d/bellsoft.list
mv /etc/apt/trusted.gpg /etc/apt/trusted.gpg.d/bellsoft.gpg 
```

```bash
apt update
apt install -y bellsoft-java17
apt install -y sysvbanner unclutter
```

### Disable Splashscreen ...

- edit `/boot/firmware/cmdline.txt`
    
  remove `quiet` and `splash` 

```text
console=serial0,115200 console=tty1 root=PARTUUID=89d32b3c-02 rootfstype=ext4 fsck.repair=yes rootwait cfg80211.ieee80211_regdom=DE
```

### Switch to legacy camera stack

https://forums.raspberrypi.com/viewtopic.php?t=323390

- edit `/boot/firmware/config.txt`
  
- disable camera auto detection
  
```
# camera_auto_detect=1
```

  - add at the end of the file

```text
[all]
start_x=1
gpu_mem=128
````

These changes switches to the legacy camera stack, required by JavaCV.

### Use X11 and NOT Wayland

- `raspi-config` 

With Wayland java fullscreen mode does not work. (black blank screen)

### RTC Hardware Clock

- `raspi-config` 

  enable I2C for rtc clock

- edit `/etc/modules`

```
i2c-dev
rtc-ds1307
```

- reboot

`sudo i2cdetect -y 1`

```
     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
00:                         -- -- -- -- -- -- -- --
10: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
20: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
30: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
40: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
50: -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
60: -- -- -- -- -- -- -- -- 68 -- -- -- -- -- -- --
70: -- -- -- -- -- -- -- --
```

- edit `/etc/rc.local`

append lines:
```
echo ds1307 0x68 > /sys/class/i2c-adapter/i2c-1/new_device
hwclock -s
```

- reboot 

check hwclock 

`hwclock -r; date`

```
2024-04-08 15:37:18.631617+02:00
Mon  8 Apr 15:37:18 CEST 2024
```

### Install Pi4J Package

- download deb package from https://github.com/Pi4J/download/tree/main

install: `dpkg -i pi4j-2.5.1.deb`

### Compile and Install gphoto2 to `/opt/ghoto2`

- `gphoto2-2.5.28 libgphoto2-2.5.31`

```
sudo apt install -y build-essential automake autoconf \
  libltdl-dev libexif-dev libusb-1.0-0-dev \
  libjpeg-dev libgd-dev libxml++2.6-dev libpopt-dev \
  libcdk5-dev libreadline-dev
```

use build scripts from `Projects/gphoto2`
