/*
####################################TO-DO###################################


*/


function checkTable(db, TableName, callback){
    //check if table exists
   db.run("select * from " + TableName, function(result) {
     if(result && result.message.indexOf("no such table") !=-1)       //check if the error contains 'no such table'
                callback(false);            //Table doesn't exist
            else
                callback(true);             //Table already exists
        });
}

module.exports = {

        initDB: function(db, callback){

            db.run("DROP TABLE if exists UUID_SECRET", function() {

                db.run("CREATE TABLE UUID_SECRET (id integer primary key autoincrement, " +
                       "uuid text not null unique, " +
                       "secret text not null)", function(result) {
                            if(result && result.message.indexOf("SQLITE_ERROR") != -1) { 
                                callback(result);
                            }
                            else
                                callback("init_OK");
                        });
            });
       },

        insertUUID: function(db, uuid, secret, callback){
            if(typeof(uuid) === 'string' && typeof(secret) === 'string')            //Prüfen, ob der Typ passt
            {    
                checkTable(db, "UUID_SECRET",function(state){  //Prüfen, ob die Tabelle existiert
                    if(state)
                    {  
                        db.run("INSERT INTO UUID_SECRET VALUES (NULL, '"
                            + uuid + "', '"                
                            + secret + "')",
                            function(result) {
                            if(result && result.message.indexOf("SQLITE_CONSTRAINT") != -1) {
                                    callback(result);           //usually Error occurs, when uuid is already in Database
                            }
                                else
                                    callback("insert_OK");
                            });
                    }
                });
            }
        },
    
        removeUUID: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
            {    
                checkTable(db, "UUID_SECRET",function(state){  //Prüfen, ob Tabelle existiert
                    if(state)
                    {
                        db.run("DELETE FROM UUID_SECRET WHERE uuid = '" + uuid + "'", function(result) {
                            callback(true);                         //result of delete is always 'null'
                        });
                    }
                });
            }
        },

        getSecret: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Prüfen, ob der Typ passt
                {
                    checkTable(db, "UUID_SECRET",function(state){
                        if(state)       //Tabelle existiert =)
                        {    
                            db.each("SELECT secret FROM UUID_SECRET where uuid = '" + uuid + "' limit 1", function(err, row) {
                                if(!err)        //Wenn kein Fehler aufgetreten ist, secret zurückgeben
                                {
                                    callback(row.secret);
                                }
                                else
                                {               //Falls Fehler aufgetreten ist, diesen ausgeben und "false" zurückgeben
                                    console.log(err);    
                                    callback("getSecret failed");
                                }
                            });
                        }
                        else
                        {
                            callback("getSecret failed");
                        }
                    });
                }
        }
};