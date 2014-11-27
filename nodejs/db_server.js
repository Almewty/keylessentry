    /*
    #############################TO-DO##########################
    
    -Webformular um auf die init-Funktion usw. zuzugreifen
    
    */


    var dba = require('./dbaccess');
    var moment = require('moment');
    
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
            if(secret) //Wenn kein Fehler aufgetreten ist, ist secret!=false
                callback(secret);
            else
            {
                console.log("Whoops! This is an error!\nMaybe the table doesn't exist?");
                callback(false);
            }
        }); 
    },
    
    initDatabase: function(){
    
    var init = true;        //Flag damit die Datenbank nur einmal initialisiert wird
                            //wird nur zu debug-zwecken benötigt!
    if(!init)   //Wenn die Datenbank noch nicht initialisiert ist, initialisieren
        {
            dba.initDB(db, function(resultcode){
                if(resultcode == "init_OK")
                {
                    init = true;
                    console.log("db init: success!");
                }
                else
                    console.log("DB init: an error occurs: " + resultcode);   
            });
        }
    },
    
    insertUUID: function(uuid, secret, callback){
            
        
        dba.insertUUID(db, uuid, secret, function(status){
            if(!status)  //Wenn die Funktion "insertUUID" 'False' zurückgibt, ist kein Error aufgetreten
                callback(true);
            else
            {
                console.log("Can't insert Value '" + uuid + "' :/ Does the UUID already exist?");
                console.log(status);
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