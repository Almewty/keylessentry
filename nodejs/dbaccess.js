        
function checkTable(db, TableName, cb_checkTable){
    //check if table exists
   db.run("CREATE TABLE UUID_OTA (uuid TEXT, ota TEXT)", function(result) {
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

        initDB: function(db){

            db.run("DROP TABLE if exists UUID_OTA");
            db.run("CREATE TABLE UUID_OTA (uuid TEXT, ota TEXT)");  //Tabelle neu erstellen

            console.log("initDB: success!");
       },

        insertUUID: function(db, uuid, ota, callback){
            var moment = require('moment');
            var dateformat = "YYYY-MM-DD HH:mm:ss";

                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                    if(moment(ota).isValid())                       //Prüfen, ob OTA im Zeitformat ist
                    {
                        checkTable(db, "UUID_OTA",function(state){
                            if(state)
                            {
                                callback(true);
                                console.log("still working on =)");
                                var stmt = db.prepare("INSERT INTO UUID_OTA VALUES (?, ?)");

                                stmt.run(uuid, moment(ota).format(dateformat));       //Das Datum richtig formatieren

                                stmt.finalize();
                            }
                            else
                                callback(false);
                        });
                    }
                }
        },

        getOTA: function(db, uuid){
            
                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                    checkTable(db, "UUID_OTA",function(state){
                        if(state)
                        {
                            db.each("SELECT * FROM UUID_OTA", function(err, row) {
                                console.log(row);
                            });
                            console.log("getOTA: success!");
                        }
                    });
                }
        }
};