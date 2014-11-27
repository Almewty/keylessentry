/*jshint node:true*/
var bleno = require('bleno'),
    util = require('util'),
    PrimaryService = bleno.PrimaryService,
    Characteristic = bleno.Characteristic,
    Descriptor = bleno.Descriptor;

var server = require('./db_server');
var ota = require('./speakeasy');


checkSmartphone("debug;2014-11-27 11:22:48");

var uuid = 'E20A39F473F54BC4A12F17D1AD07A961';
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
    advertisementData[i++] = 0xBE;
    advertisementData[i++] = 0xAC;
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

  
    console.log(data);
    
    server.checkSmartphone(data,function(uuid,otp){
            
        callback(Characteristic.RESULT_SUCCESS, data);          //Rückgabe der Daten an das BLE-Gerät
  
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

bleno.updateRssi(function(error, rssi){
    
});
    

bleno.on('stateChange', function (state) {
    console.log('on -> stateChange: ' + state);

    if (state === 'poweredOn') {
        startAdvertisingAltBeacon(0xFFFF, uuid, major, minor, measuredPower, 0x00);
    } else {
        bleno.stopAdvertising();
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

function calculateOTA(secret, callback){
    console.log("database secret: " + secret);
    console.log("I've calculated some shit"+ ota.totp({key: secret}));
    callback(ota.totp({key: secret}));
}

function checkSmartphone(receivedString, callback){         //receivedString is in the format:  uuid;ota
 
        receivedString = "debug;66666";             

            var arr = receivedString.split(";");    //Split String into array of uuid and ota
            if(arr.length==2 && typeof(arr[0]) == 'string' && typeof(arr[1]) == 'string')
            {
                var UUIDreceived = arr[0];  //uuid of asking smartphone
                var OTAreceived = arr[1];   //calculated ota from smartphone
                
                console.log(UUIDreceived);
                console.log(OTAreceived);
                    
                server.getSecretFromDB(UUIDreceived,function(secretDB){               //secretDB = Secret from DB
                    if(secretDB)
                    {
                        calculateOTA(secretDB, function(OTAcalculated){               //OTAcalculated = calculated OTA based on secretDB
                        //callback(true);
                        if(OTAcalculated == OTAreceived)
                            console.log("Access granted!");
                        else
                            console.log("Get out of my way!");
                        });
                    }
                });
            }
        
}
