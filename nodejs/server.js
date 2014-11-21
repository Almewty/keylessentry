    var dbc = require('./dbaccess');
    var moment = require('moment');
    
    //Datenbank-Zeugs
    var sqlite3 = require('sqlite3').verbose();
    var databasepath = "keyless.db";
    var db = new sqlite3.Database(databasepath);
    var init = true;       //Sorgt dafür, dass die Datenbank nur einmal initialisiert wird

    //Datenbankzugriff muss komplett im db.serialize block stehen, damit der Datenbankzugriff sauber abläuft
    db.serialize(function() {              
    
        
        if(!init)
        {
            dbc.initDB(db);     
            init = true;
        }
        
        dbc.insertUUID(db, "debug",moment());

        dbc.getOTA(db, "debug");

    });