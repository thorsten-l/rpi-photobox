#!/bin/bash

cd /home/th

banner `date '+%Y-%m-%d'`
banner `date '+%H:%M:%S'`

echo turning screen blank off
sleep 2
xset s off
xset -dpms
xset s noblank

CAMERA="`lsusb | grep Canon | grep EOS`"
if [[ -z "${CAMERA}" ]]; then
  export GDK_SCALE=2.0; zenity --error --title PhotoBox2 --text "Keine Camera gefunden!" --ok-label=Abbruch
  exit 0
fi
echo "CAMERA=$CAMERA"

PRINTER=`./FIND_PRINTER.sh | grep PRINTER`

if [[ "$PRINTER" == "NO_PRINTER_DETECTED" ]]
then
  export GDK_SCALE=2.0; zenity --question  --title PhotoBox2 --text "Kein Drucker gefunden." --ok-label=Weiter --cancel-label=Abbruch
  DIALOG_STATUS=$?
  echo "DIALOG_STATUS=$DIALOG_STATUS"

  if [ $DIALOG_STATUS -ne 0 ]; then
    exit 0
  fi
fi

echo "PRINTER=$PRINTER"

# dialog --backtitle "PhotoBox2" --title "Information" --infobox "GPIO-Test (Taster)" 3 30
sleep 2
# ./gpiotestpi4j/bin/app

dialog --backtitle "PhotoBox2" --title "Information" --infobox "Starte PhotoBox2" 3 30
sleep 2
unset GDK_SCALE
cd /home/th/PhotoBox2 
./bin/app
if [ $? -eq 0 ]; then
  echo "photobox stopped successfully"
else
  echo "photobox stopped abnormally"
fi

/home/th/RSYNC_TO_USBSTICK.sh

sudo poweroff

