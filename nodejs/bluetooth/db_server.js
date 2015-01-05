/*
#############################TO-DO##########################
    
    
*/
var Phone = require('../server/api/phone/phone.model');

module.exports = {
    getSecretFromDB: function(uuid, callback){
        Phone.findById(uuid, function (err, phone) {
            if (err || !phone) {
                callback(false);   
            }
            callback(phone.secret);
        });
    }
};