/*
#############################TO-DO##########################
    
    
*/


    var dba = require('./dbaccess');

    //Datenbank-Zeugs
    var sqlite3 = require('sqlite3').verbose();
    var databasepath = "keyless.db";
    var db = new sqlite3.Database(databasepath);

    //Datenbankzugriff muss komplett im db.serialize block stehen, damit der Datenbankzugriff sauber abläuft
    //nur zu Debug-zwecken genutzt
    db.serialize(function() {              
    
    });


//Dieser Teil ist von aussen erreichbar
module.exports = {
    
    getSecretFromDB: function(uuid, callback){
        dba.getSecret(db, uuid, function(secret){
            if(secret != "getSecret failed") //Wenn kein Fehler aufgetreten ist, ist secret!="getSecret failed"
                callback(secret);
            else
            {
                console.log("Whoops! This is an error!\nMaybe the table doesn't exist?");
                callback(false);
            }
        }); 
    },
    
    initDatabase: function(){
        dba.initDB(db, function(resultcode){
            if(resultcode == "init_OK")
            {
                init = true;
                console.log("db init: success!");
            }
            else
                console.log("DB init: an error occurs: " + resultcode);   
        });
    },
    
    insertUUID: function(uuid, secret, callback){
        dba.insertUUID(db, uuid, secret, function(status){
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
    
    removeUUID: function(uuid, callback){
        dba.removeUUID(db, uuid, function(status){
            if(status)
                callback(true);
        });
    }
};