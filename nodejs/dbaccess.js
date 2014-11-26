/*
####################################TO-DO###################################

-sicherstellen, dass zu jeder UUID nur 1 secret existiert!!

*/


function checkTable(db, TableName, cb_checkTable){
    //check if table exists
   db.run("CREATE TABLE UUID_OTA (uuid TEXT PRIMARY KEY, ota TEXT)", function(result) {
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

            db.run("CREATE TABLE UUID_OTA (uuid TEXT, ota TEXT)", function(result) {
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

                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                    if(moment(sharedsecret).isValid())                       //Prüfen, ob OTA im Zeitformat ist
                    {
                        checkTable(db, "UUID_OTA",function(state){
                            if(state)
                            {
                                var stmt = db.prepare("INSERT INTO UUID_OTA VALUES (?, ?)");

                                stmt.run(uuid, moment(sharedsecret).format(dateformat));       //Das Datum richtig formatieren

                                stmt.finalize();
                                callback(true);
                            }
                            else
                                callback(false);
                        });
                    }
                }
        },

        getSecret: function(db, uuid, callback){
            
                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                    checkTable(db, "UUID_OTA",function(state){
                        if(state)       //Tabelle existiert =)
                        {
                            db.each("SELECT uuid, ota FROM UUID_OTA where uuid = '" + uuid + "' limit 1", function(err, row) {
                                callback(row.ota);
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