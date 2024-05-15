#pragma once

#define SUDO "/usr/bin/sudo "
#define PIN_SETUP_COMMAND "/usr/bin/pinctrl set 5,7 ip pu"
#define PIN_POLL_COMMAND "/home/th/bin/pinctrl poll 5,7"
#define POWEROFF_COMMAND "/usr/sbin/poweroff"
#define REBOOT_COMMAND "/usr/sbin/reboot"

extern int main(void);
