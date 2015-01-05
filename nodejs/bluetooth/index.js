/*
#########################TO-DO#################################

-OTPAuthenticationCharacteristic nachschauen, unsinnige Werte abfangen!
-bolzen schließen, nach einem TimeOut von 1 Minute
-Webformular um auf die init-Funktion usw. zuzugreifen

-Fragwürdigen Code entfernen:
    -bleno.updateRssi
    -

*/

/*jshint node:true*/

//Requirements
var bleno = require('bleno'),
    util = require('util'),
    PrimaryService = bleno.PrimaryService,
    Characteristic = bleno.Characteristic,
    Descriptor = bleno.Descriptor;

var server = require('./db_server'); //Handles Database-connection
var otp = require('./speakeasy'); //Calculates OTP-Password based on secret
var qr = require('./QRgenerator'); //Generates QR-Code with App-Link inside
var gpio = require("pi-gpio"); //Allows access to Raspberry Pi's GPIO-Pins
var uuid_util = require('node-uuid'); //Allows uuid generation and handling
var crypto = require('crypto');
var base64 = require('base64');//Encodes and decodes to/from base64
var config = require('../server/config/environment'); // configurations


//##################################################################################################################
//Manuelles aufrufen von Funktionen START
//This Section is just for debugging purposes

//server.initDatabase();                    //reinitialize Database

/*
registerSmartphone(function (QRdatapath) {
    if (QRdatapath != "failed")
        console.log("The QR-Code is stored here: " + QRdatapath);
    else
        console.log("Error!");
});

checkSmartphone("N2YxYzc3NDMtMjliYS00ODkwLWIzNjctZDA1NWJhNjQ2NjIy66667777", function (result) {
    if (result)
        console.log("YES! Access granted =)");
});
*/
//##################################################################################################################
//Manuelles aufrufen von Funktionen END


//##################################################################################################################
//Bleno START
//var uuid = 'E20A39F473F54BC4A12F17D1AD07A961';
var uuid = config.doorId.replace(/-/g, ''); // wird generiert wenn nicht vorhanden. sollte einzigarteig sein pro tür
var major = 0x0001;
var minor = 0x0001;
var measuredPower = -59; // -128 - 127

var startAdvertisingAltBeacon = function (manId, uuid, major, minor, measuredPower, manData) {
    var scanDataLength = 0;
    var i = 0;
    if (manData === undefined)
        manData = 0x00;
    if (typeof uuid === 'string') {
        var tmpUuid = new Buffer(16);
        for (i = 0; i < 16; i++) {
            tmpUuid[i] = parseInt(uuid.substr(i * 2, 2), 16);
        }
        uuid = tmpUuid;
    }
    i = 0;
    var advertisementData = new Buffer(0x1F);
    // flags
    advertisementData[i++] = 0x02;
    advertisementData[i++] = 0x01;
    advertisementData[i++] = 0x1B;
    // manufacturer data
    advertisementData[i++] = 0x1B;
    advertisementData[i++] = 0xFF;
    advertisementData[i++] = manId & 0xFF; // 0x4c; // Apple Company Identifier LE (16 bit)
    advertisementData[i++] = (manId & 0xFF00) >> 8; // 0x00;
    advertisementData[i++] = 0xD0;
    advertisementData[i++] = 0x02;
    for (var j = 0; j < 16; j++) {
        advertisementData[i++] = uuid[j];
    }
    advertisementData[i++] = (major & 0xFF00) >> 8;
    advertisementData[i++] = major & 0xFF;
    advertisementData[i++] = (minor & 0xFF00) >> 8;
    advertisementData[i++] = minor & 0xFF;
    advertisementData[i++] = measuredPower & 0xFF;
    advertisementData[i++] = manData & 0xFF;
    i = 0;
    var scanData = new Buffer(scanDataLength);
    bleno.startAdvertisingWithEIRData(advertisementData, scanData);
};

function OTPAuthenticationCharacteristic() {
    OTPAuthenticationCharacteristic.super_.call(this, {
        uuid: 'a7a09b5d8374445b89cc42b73dd164e8',
        properties: ['write'],
        descriptors: [
            new Descriptor({
                uuid: '792e3fa0bb8711e390640002a5d5c51c',
                value: 'One-time-password write'
            })]
    });
}

util.inherits(OTPAuthenticationCharacteristic, Characteristic);

OTPAuthenticationCharacteristic.prototype.onWriteRequest = function (data, offset, withoutResponse, callback) {
    if (offset) {
        callback(this.RESULT_ATTR_NOT_LONG);
    }

    checkSmartphone(data, function (result) {
        if (result) {
            console.log("Access granted!");
            callback(Characteristic.RESULT_SUCCESS, "access granted"); //Rückgabe von "access granted ans smartphone
        } else {
            console.log("Get out of my way!");
            callback(Characteristic.RESULT_SUCCESS, "access not granted"); //Rückgabe von "access not granted ans smartphone
        }
    });
};

function OTPService() {
    OTPService.super_.call(this, {
        uuid: '542888d16a924d9b931469882775001a',
        characteristics: [
            new OTPAuthenticationCharacteristic()
        ]
    });
}

util.inherits(OTPService, PrimaryService);

bleno.updateRssi(function (error, rssi) {

});


bleno.on('stateChange', function (state) {
    console.log('on -> stateChange: ' + state);

    if (state === 'poweredOn') {
        startAdvertisingAltBeacon(0xFFFF, uuid, major, minor, measuredPower, 0x00);
    } else {
        bleno.stopAdvertising();
        console.log("Advertising stopped");
    }
});

bleno.on('advertisingStart', function (error) {
    console.log('on -> advertisingStart: ' + (error ? 'error ' + error : 'success'));

    if (!error) {
        bleno.setServices([
            new OTPService()
        ]);
    }
});
//##################################################################################################################
//Bleno END



//##################################################################################################################
//Keyless-Zeugs START

function calculateOTPs(secret, callback) {
    var time = parseInt(Date.now() / 1000);

    // calculate counter value
    var counter = Math.floor(time / 30);

    //calculate 3 OTP's, to be sure that one should met the Smartphone OTP
    callback([otp.hotp_bin({
            key: secret,
            counter: counter        //Actual OTP
        }),
           otp.hotp_bin({
            key: secret,
            counter: counter - 1    //Previous OTP
        }), otp.hotp_bin({
            key: secret,
            counter: counter + 1    //Next OTP
        })]);
}

function checkSmartphone(receivedData, callback) { //receivedData is in the format: uuid(16 bytes) otp(4 bytes)
    receivedData = new Buffer(receivedData);
    var UUIDreceived = uuid_util.unparse(receivedData);
    var OTPreceived = receivedData.readInt32BE(16);
    
    server.getSecretFromDB(UUIDreceived, function (secretDB) { //secretDB = Secret read from DB
        if (secretDB) {
            calculateOTPs(
                secretDB,     //Converts base64 in Binary
                function (OTPcalculated) { //OTPcalculated = calculated OTPs based on secretDB
                if (OTPcalculated.indexOf(OTPreceived) >= 0) { //check if one of the 3 OTPs equals
                    gpio.open(7, "output", function (err) { //open pin 7 in output mode
                        gpio.write(7, 1, function () { //write on pin 7, true (HIGH)
                            gpio.close(7); //close pin 7
                        });
                    });
                    setTimeout(function(){          //After 1 minute (60000 milliseconds) shut the door anyway
                        gpio.open(7, "output", function (err) { //open pin 7 in output mode
                            gpio.write(7, 0, function () { //write on pin 7, false (LOW)
                                gpio.close(7); //close pin 7
                            });
                        });
                    },60000);
                    callback(true);
                } else {
                    gpio.open(7, "output", function (err) { //open pin 7 in output mode
                        gpio.write(7, 0, function () { //write on pin 7, false (LOW)
                            gpio.close(7); //close pin 7
                        });
                    });
                    callback(false);
                }
            });
        } else
            callback(false);
    });
}

//##################################################################################################################
//Keyless-Zeugs END