package de.w_hs.keylessentry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.w_hs.keylessentry.R;

import static de.w_hs.keylessentry.Helper.*;

public class DataStorage {
    private static DataStorage dataStorage;

    private SQLiteOpenHelper storageHelper;
    private SQLiteDatabase database;

    public static DataStorage getInstance(Context context) {
        if (dataStorage == null) {
            dataStorage = new DataStorage(context);
        }
        return dataStorage;
    }

    private DataStorage(Context context) {
        storageHelper = new DataStorageHelper(context);
        database = storageHelper.getWritableDatabase();
    }

    public void close() {
        database.close();
    }


    public List<Door> getDoors() {
        SQLiteDatabase db = storageHelper.getReadableDatabase();
        Cursor cursor = db.query("doors",
                new String[]{
                        "remoteIdentifier",
                        "ownIdentifier",
                        "sharedSecret",
                        "name"},
                null, null, null, null,
                "id DESC");

        List<Door> doors = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            doors.add(cursorToDoor(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return doors;
    }

    public void insertDoor(Door door) {
        deleteDoor(door);

        ContentValues values = new ContentValues();
        values.put("ownIdentifier", bytesToHex(UUIDToBytes(door.getOwnIdentifier())));
        values.put("remoteIdentifier", bytesToHex(UUIDToBytes(door.getRemoteIdentifier())));
        values.put("sharedSecret", bytesToHex(door.getSharedSecret()));
        values.put("name", door.getName());
        database.insert("doors", null, values);
    }

    public void deleteDoor(Door door) {
        database.delete("doors", "remoteIdentifier = ? AND ownIdentifier = ?",
                new String[]{
                        bytesToHex(UUIDToBytes(door.getRemoteIdentifier())),
                        bytesToHex(UUIDToBytes(door.getOwnIdentifier()))});
    }

    private Door cursorToDoor(Cursor cursor) {
        UUID remoteID = bytesToUUID(hexToBytes(cursor.getString(0)));
        UUID ownID = bytesToUUID(hexToBytes(cursor.getString(1)));
        byte[] sharedSecret = hexToBytes(cursor.getString(2));
        String name = cursor.getString(3);
        return new Door(name, remoteID, ownID, sharedSecret);
    }

    private class DataStorageHelper extends SQLiteOpenHelper {
        private Context context;

        public DataStorageHelper(Context context) {
            super(context, context.getResources().getString(R.string.database_name), null, context.getResources().getInteger(R.integer.database_version));
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String sql : context.getResources().getStringArray(R.array.database_create)) {
                db.execSQL(sql);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String[] sqls = context.getResources().getStringArray(R.array.database_update);
            int startindex = sqls.length - 2;
            if (startindex < 0)
                return;
            for (int i = startindex; i < sqls.length; i++) {
                db.execSQL(sqls[i]);
            }
        }
    }
}
