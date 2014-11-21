module.exports = {

        initDB: function(db){

            db.run("DROP TABLE if exists UUID_OTA");
            db.run("CREATE TABLE UUID_OTA (uuid TEXT, ota TEXT)");  //Tabelle neu erstellen

            console.log("initDB: success!");
       },

        insertUUID: function(db, uuid, ota){
            var moment = require('moment');

            var dateformat = "YYYY-MM-DD HH:mm:ss";

                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                    if(moment(ota).isValid())                       //Prüfen, ob OTA im Zeitformat ist
                    {
                        //#####################################################################
                        //#Hier fehlt noch eine Abfrage, ob die Tabelle überhaupt existiert!!!#
                        //#####################################################################
                       
                        //check if table exists
                        db.run("CREATE TABLE UUID_OTA (uuid TEXT, ota TEXT)", function(error) {
                        if(error)
                        {
                            if (error.message.indexOf("already exists") != -1) {        //if 
                                console.log("Table 'UUID_OTA' found, working on");
                               
                                
                                var stmt = db.prepare("INSERT INTO UUID_OTA VALUES (?, ?)");
                                    
                                stmt.run(uuid, moment(ota).format(dateformat));       //Das Datum richtig formatieren

                                stmt.finalize();

                                console.log("insertUUID: success!");
                            }
                        }
                        }); 
                    }
                }
        },

        getOTA: function(db, uuid){
            
                if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {    
                        //#####################################################################
                        //#Hier fehlt noch eine Abfrage, ob die Tabelle überhaupt existiert!!!#
                        //#####################################################################

                    db.each("SELECT * FROM UUID_OTA", function(err, row) {
                            console.log(row);
                    });
                }
            console.log("getOTA: success!");
        }
};