nodejs muss installiert sein
npm muss installiert sein
mongodb muss installiert sein
das distribution packet von keylessentry muss in /opt/keylessentry liegen
installiere forever mit "sudo npm install -g forever"
kopiere die datei keylessentry nach /etc/init.d/ "sudo cp keylessentry /etc/init.d/"
setzte die rechte mit "sudo chown 755 /etc/init.d/"
registriere für startup "sudo update-rc.d keylessentry"
reboot "sudo reboot"