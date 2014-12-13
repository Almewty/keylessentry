/*
#############################TO-DO##########################
    
    
*/
    var dba = require('./dbaccess');

    //Datenbank-variablen
    var sqlite3 = require('sqlite3').verbose();
    var databasepath = "keyless.db"; //Name of Database
    var db = new sqlite3.Database(databasepath);

module.exports = {
    initDatabase: function(callback){
        dba.initDB(db, function(resultcode){
            if(resultcode == "init_OK")
            {
                console.log("db init: success!");
                callback(true);
            }
            else
            {
                console.log("DB init: an error occurs: " + resultcode);   
                callback (false);
            }
        });
    },

    insertUUID: function(uuid, name, secret, callback){
        dba.insertUUID(db, uuid, name, secret, function(status){
            if(status == "insert_OK")
                callback(status);
            else
            {
                console.log("Can't insert Value '" + uuid + "' :/ Does the UUID already exist?");
                console.log("This Error was thrown by DB: " + status);
                callback(false);
            }
        });
    },
    
    getSecretFromDB: function(uuid, callback){
        dba.getSecret(db, uuid, function(secret){
            if(secret != "failed") //Wenn kein Fehler aufgetreten ist, ist secret!="failed"
                callback(secret);
            else
            {
                console.log("Whoops! This is an error!\nMaybe the table doesn't exist?");
                callback(false);
            }
        }); 
    },
    
    getNameFromDB: function(uuid, callback){
        dba.getName(db, uuid, function(name){
            if(secret != "failed") //Wenn kein Fehler aufgetreten ist, ist name!=failed
                callback(name);
            else
            {
                console.log("Whoops! This is an error!\nMaybe the table doesn't exist?");
                callback(false);
            }
        }); 
    },
    
    removeUUID: function(uuid, callback){
        dba.removeUUID(db, uuid, function(status){
            if(status)
                callback(true);
            else
                callback(false);
        });
    }
};