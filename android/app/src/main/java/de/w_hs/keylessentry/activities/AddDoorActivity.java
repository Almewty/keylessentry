package de.w_hs.keylessentry.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.nio.Buffer;
import java.nio.BufferUnderflowException;

import de.w_hs.keylessentry.R;
import de.w_hs.keylessentry.data.DataStorage;
import de.w_hs.keylessentry.data.Door;

import static de.w_hs.keylessentry.Helper.*;

public class AddDoorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_door);

        Intent intent = getIntent();
        // urls are http://w-hs.de/keylessentry?name=<name>&ownid=<ownid>&remoteid=<remoteid>&secret=<secret>
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            String name = data.getQueryParameter("name");
            String ownid = data.getQueryParameter("ownid");
            String remoteid = data.getQueryParameter("remoteid");
            String secret = data.getQueryParameter("secret");
            try {
                Door d = new Door(name, bytesToUUID(hexToBytes(remoteid)), bytesToUUID(hexToBytes(ownid)), hexToBytes(secret));
                DataStorage.getInstance(getApplicationContext()).insertDoor(d);
                new AlertDialog.Builder(this)
                        .setTitle("Success")
                        .setMessage("Door successfully added")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create().show();
            } catch (BufferUnderflowException | StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                new AlertDialog.Builder(this)
                        .setTitle("Invalid data")
                        .setMessage("The entered data is invalid")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create().show();
            }
//            ((EditText)findViewById(R.id.edit_name)).setText(name);
//            ((EditText)findViewById(R.id.edit_own_id)).setText(ownid);
//            ((EditText)findViewById(R.id.edit_remote_id)).setText(remoteid);
//            ((EditText)findViewById(R.id.edit_secret)).setText(secret);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_door, menu);
        return true;
    }

    public void onAddClicked(View view) {
        String name = ((EditText)findViewById(R.id.edit_name)).getText().toString();
        String ownid = ((EditText)findViewById(R.id.edit_own_id)).getText().toString();
        String remoteid = ((EditText)findViewById(R.id.edit_remote_id)).getText().toString();
        String secret = ((EditText)findViewById(R.id.edit_secret)).getText().toString();

        // public Door(String name, UUID remoteIdentifier, UUID ownIdentifier, byte[] sharedSecret) {
        try {
            Door d = new Door(name, bytesToUUID(hexToBytes(remoteid)), bytesToUUID(hexToBytes(ownid)), hexToBytes(secret));
            DataStorage.getInstance(getApplicationContext()).insertDoor(d);
        } catch (BufferUnderflowException | StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid data")
                    .setMessage("The entered data is invalid")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", null)
                    .create().show();
        }
    }
}
