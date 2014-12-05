#!/bin/bash

#DestinationAddress of the Raspberry Pi
DESTINATION="192.168.2.24"
USER="pi"
PASSWORD="pi"

echo Destination: $DESTINATION
echo User: $USER
echo Password: $PASSWORD

echo Starting File-Transfer
#Übertragen der nodejs-Dateien

echo 'transferring package.json'
sshpass -p "$PASSWORD" scp -r package.json $USER@$DESTINATION:/home/pi/nodejs
echo 'done!'

echo 'transferring node-files'
sshpass -p "$PASSWORD" scp -r dbaccess.js db_server.js index.js QRgenerator.js speakeasy.js $USER@$DESTINATION:/home/pi/nodejs
echo 'done!'

echo 'finished'
echo 'all Jobs are done!'
