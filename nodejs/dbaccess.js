/*
####################################TO-DO###################################

-checkTable verallgemeinern
-checkTable mit Select statt mit create
*/


function checkTable(db, TableName, cb_checkTable){
    //check if table exists
   db.run("CREATE TABLE UUID_OTA (uuid TEXT not null unique, ota TEXT not null,  PRIMARY KEY(uuid, ota))", function(result) {
            if(result && result.message.indexOf("already exists") != -1) {      //check if the error contains 'already exists'
                cb_checkTable(true);            //Table already exists
            }
            else
            {
                db.run("DROP TABLE if exists UUID_OTA");        //Table did not exist, but now => drop table
                cb_checkTable(false);
            }
        });
}

module.exports = {

        initDB: function(db, callback){

            db.run("DROP TABLE if exists UUID_OTA");

            db.run("CREATE TABLE UUID_OTA (uuid TEXT not null unique, ota TEXT not null,  PRIMARY KEY(uuid, ota))", function(result) {
                if(result && result.message.indexOf("SQLITE_ERROR") != -1) { //if error occurs
                    callback(result);
                }
                else
                    callback("init_OK");
                
            });
       },

        insertUUID: function(db, uuid, sharedsecret, callback){
            var moment = require('moment');
            var dateformat = "YYYY-MM-DD HH:mm:ss";

                if(typeof(uuid) === 'string')                       //Prüfen, ob der Typ passt
                {    
                    if(moment(sharedsecret).isValid())              //Prüfen, ob OTA im Zeitformat ist
                    {
                        checkTable(db, "UUID_OTA",function(state){  //Prüfen, ob die Tabelle existiert
                        if(state)
                        {
                        db.run("INSERT INTO UUID_OTA VALUES ('" 
                               + uuid + "', '"                 
                               + moment(sharedsecret).format(dateformat) + "')",    //Das richtig formatierte sharedsecret
                               function(result) {

                                    if(result && result.message.indexOf("SQLITE_CONSTRAINT") != -1) { //if error occurs
                                        callback(result);           //Error aufgetreten
                                    }
                                    else
                                        callback(false);            //Kein Error Aufgetreten
                                });
                        }
                        });
                    }
                }
        },
    
        removeUUID: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
            {    
                checkTable(db, "UUID_OTA",function(state){     //Prüfen, ob Tabelle existiert
                    db.run("DELETE FROM UUID_OTA WHERE uuid = '" + uuid + "'", function(result) {
                        callback(true);                         //result is always 'null'
                    });
                });
            }
        },

        getSecret: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {
                    checkTable(db, "UUID_OTA",function(state){
                        if(state)       //Tabelle existiert =)
                        {    
                            db.each("SELECT uuid, ota FROM UUID_OTA where uuid = '" + uuid + "' limit 1", function(err, row) {
                                if(!err)        //Wenn kein Fehler aufgetreten ist, ota zurückgeben
                                    callback(row.ota);
                                else
                                {               //Falls Fehler aufgetreten ist, diesen ausgeben und "false" zurückgeben
                                    callback(false);
                                    console.log(err);    
                                }
                            });
                        }
                        else
                        {
                            callback(false);
                        }
                    });
                }
        }
};