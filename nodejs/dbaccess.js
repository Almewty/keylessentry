/*
####################################TO-DO###################################


*/

function checkTable(db, TableName, callback){
    //check if table exists
   db.run("select * from " + TableName, function(result) {
       if(result && result.message.indexOf("no such table") !=-1) //check if the error contains 'no such table'
           callback(false); //Table doesn't exist
       else
           callback(true); //Table already exists
    });
}

module.exports = {

        initDB: function(db, callback){

            db.run("DROP TABLE if exists UUID_SECRET", function() {

                db.run("CREATE TABLE UUID_SECRET (id integer primary key autoincrement, " +
                       "uuid text not null unique, " +
                       "name text, " + 
                       "secret text not null)", function(result) {
                            if(result && result.message.indexOf("SQLITE_ERROR") != -1) { 
                                callback(result);
                            }
                            else
                                callback("init_OK");
                        });
            });
       },

        insertUUID: function(db, uuid, name, secret, callback){
            if(typeof(uuid) === 'string' && typeof(secret) === 'string' && typeof(name) === 'string') //Check if Type's are string
            {    
                checkTable(db, "UUID_SECRET",function(state){  //Check, if Table exists
                    if(state)
                    {
                        db.run("INSERT INTO UUID_SECRET VALUES (NULL, '"
                            + uuid + "', '"
                            + name + "', '"
                            + secret + "')",
                            function(result) {
                            if(result && result.message.indexOf("SQLITE_CONSTRAINT") != -1) {
                                    callback(result);           //usually Error occurs, when uuid is already in Database
                            }
                                else
                                    callback("insert_OK");
                            });
                    }
                    else
                    {
                        console.log("Error occurs");
                        callback(false);
                    }
                });
            }
            else
            {
                console.log("Type of uuid, name or secret isn't string");   
                callback(false);
            }
        },
    
        removeUUID: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Check if Type is string
            {    
                checkTable(db, "UUID_SECRET",function(state){  //Check, if Table exists
                    if(state)
                    {
                        db.run("DELETE FROM UUID_SECRET WHERE uuid = '" + uuid + "'", function(result) {
                            callback(true); //result of delete is always 'null', so you don't have to check
                        });
                    }
                    else
                    {
                        console.log("Table UUID_SECRET doesn't exist!");
                        console.log("Please reinitialize Database");
                        callback(false);
                    }
                });
            }
            else
            {
                console.log("Type of uuid isn't string");
                callback(false);
            }
        },

        getSecret: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Check if Type is string
            {
                checkTable(db, "UUID_SECRET",function(state){ //Check if Table exists
                    if(state)
                    {    
                        db.each("SELECT secret FROM UUID_SECRET where uuid = '" + uuid + "' limit 1", function(err, row) {
                            if(!err)
                            {
                                callback(row.secret);
                            }
                            else
                            {
                                console.log(err);    
                                callback("failed");
                            }
                        });
                    }
                    else
                    {
                        callback("failed");
                    }
                });
            }
            else
            {
                console.log("Type of uuid isn't string");
                callback("failed");
            }
        },
    
        getName: function(db, uuid, callback){
            if(typeof(uuid) === 'string')                      //Check if Type is string
            {
                checkTable(db, "UUID_SECRET",function(state){ //Check if Table exists
                    if(state)
                    {    
                        db.each("SELECT name FROM UUID_SECRET where uuid = '" + uuid + "' limit 1", function(err, row) {
                            if(!err)
                            {
                                callback(row.name);
                            }
                            else
                            {
                                console.log(err);    
                                callback("failed");
                            }
                        });
                    }
                    else
                    {
                        callback("failed");
                    }
                });
            }
            else
            {
                console.log("Type of uuid isn't string");
                callback("failed");
            }
        }
};