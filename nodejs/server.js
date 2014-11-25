    var dba = require('./dbaccess');
    var moment = require('moment');
    
    //Datenbank-Zeugs
    var sqlite3 = require('sqlite3').verbose();
    var databasepath = "keyless.db";
    var db = new sqlite3.Database(databasepath);
    var init = false;       //Flag damit die Datenbank nur einmal initialisiert wird

    //Datenbankzugriff muss komplett im db.serialize block stehen, damit der Datenbankzugriff sauber abläuft
    db.serialize(function() {              
    
        if(!init)   //Wenn die Datenbank noch nicht initialisiert ist, initialisieren
        {
            dba.initDB(db, function(resultcode){
                if(resultcode == "init_OK")
                {
                    init = true;
                    console.log("db init: success!");
                }
               else
                {
                    console.log("DB init: an error occurs: " + resultcode);   
                }
            });     
        }
        
        dba.insertUUID(db, "debug",moment(), function(status){
            if(status)  //Wenn die Funktion "insertUUID" 'True' durch den Callback zurückgibt
                console.log("insertUUID: success!");
            else
                console.log("insertUUID: error!");
        });

        dba.getSecret(db, "debug", function(sharedsecret){
         if(sharedsecret) //Wenn kein Fehler aufgetreten ist, ist sharedsecret!=false
         {
             for(element in sharedsecret)
             {
                 console.log(element);
             }
             //console.log(sharedsecret);   
         }
        });

    });