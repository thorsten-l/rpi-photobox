#pragma once

#define GPIO_PIN_REBOOT "5"
#define GPIO_PIN_POWEROFF "7"

#define SUDO "/usr/bin/sudo "
#define PIN_SETUP_COMMAND "/usr/bin/pinctrl set " GPIO_PIN_REBOOT "," GPIO_PIN_POWEROFF " ip pu"
#define PIN_POLL_COMMAND "/home/th/bin/pinctrl poll " GPIO_PIN_REBOOT "," GPIO_PIN_POWEROFF
#define POWEROFF_COMMAND "/usr/sbin/poweroff"
#define REBOOT_COMMAND "/usr/sbin/reboot"

extern int main(void);
