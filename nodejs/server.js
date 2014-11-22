    var dba = require('./dbaccess');
    var moment = require('moment');
    
    //Datenbank-Zeugs
    var sqlite3 = require('sqlite3').verbose();
    var databasepath = "keyless.db";
    var db = new sqlite3.Database(databasepath);
    var init = true;       //Flag damit die Datenbank nur einmal initialisiert wird

    //Datenbankzugriff muss komplett im db.serialize block stehen, damit der Datenbankzugriff sauber abläuft
    db.serialize(function() {              
    
        
        if(!init)
        {
            console.log("yeap db init");
            dba.initDB(db);     
            init = true;
        }
        
        dba.insertUUID(db, "debug",moment(), function(status){
            if(status)  //Wenn die Funktion "insertUUID" 'True' durch den Callback zurückgibt
                console.log("insertUUID: success!");
            else
                console.log("insertUUID: error!");
        });

        dba.getOTA(db, "debug");

    });