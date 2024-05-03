#!/bin/bash

DESTINATION=`df -h | awk '/\/dev\/sda1/{print $6}'`

if [[ -z "${DESTINATION}" ]]; then
#  dialog --backtitle "PhotoBox2" --title "Information" --infobox "Kein USB-Stick gefunden.\n\nPhotobox2 - ENDE!" 5 30
  export GDK_SCALE=2.0; zenity --error  --title PhotoBox2 --text 'Kein USB-Stick gefunden.\n\nPhotobox2 - ENDE!'
else
  # dialog --backtitle "PhotoBox2" --title "Information" --infobox "USB-Stick gefunden.\n$DESTINATION\nKopiere Bilder" 6 40
  # sleep 4
  export GDK_SCALE=2.0; zenity --info  --title PhotoBox2 --timeout 3 --text "USB-Stick gefunden.\n$DESTINATION\nKopiere Bilder"
  /usr/bin/rsync -rvu --progress /home/th/var/ $DESTINATION
  # dialog --backtitle "PhotoBox2" --title "Information" --infobox "Bilder auf USB-Stick kopiert.\n\nPhotobox2 - ENDE!" 5 40
  export GDK_SCALE=2.0; zenity --error  --title PhotoBox2 --text "Bilder auf USB-Stick kopiert.\n\nPhotobox2 - ENDE!"
fi

