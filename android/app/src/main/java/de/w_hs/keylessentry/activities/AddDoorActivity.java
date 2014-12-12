package de.w_hs.keylessentry.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.nio.BufferUnderflowException;
import java.util.UUID;

import de.w_hs.keylessentry.R;
import de.w_hs.keylessentry.data.DataStorage;
import de.w_hs.keylessentry.data.Door;

import static de.w_hs.keylessentry.Helper.*;
import static de.w_hs.keylessentry.Constants.*;

public class AddDoorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            final Door d = extractData(getIntent());
            buildMessage(getString(R.string.new_door), getString(R.string.new_door_info, d.getName()), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataStorage.getInstance(getApplicationContext()).insertDoor(d);
                    showMessage(getString(R.string.success), getString(R.string.new_door_conf), onClickFinisher);
                }
            })
                    .setNegativeButton(getString(android.R.string.cancel), onClickFinisher)
                    .create().show();
        } catch (BufferUnderflowException | StringIndexOutOfBoundsException e) {
            showMessage(getString(R.string.invalid_data_title), getString(R.string.invalid_data_message), android.R.drawable.ic_dialog_alert, onClickFinisher);
        }
    }

    private DialogInterface.OnClickListener onClickFinisher = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };

    private void showMessage(String title, String message, DialogInterface.OnClickListener onClickListener) {
        buildMessage(title, message, onClickListener).create().show();
    }

    private void showMessage(String title, String message, int icon, DialogInterface.OnClickListener onClickListener) {
        buildMessage(title, message, onClickListener)
                .setIcon(icon)
                .create().show();
    }

    private AlertDialog.Builder buildMessage(String title, String message, DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), onClickListener);
    }

    private Door extractData(Intent intent) {
        if (intent.getAction() == null || !intent.getAction().equals(Intent.ACTION_VIEW))
            throw new IllegalArgumentException("Intent empty or not expected");
        Uri data = intent.getData();
        String name = data.getQueryParameter(URI_QUERYPARAM_NAME);
        String ownid = data.getQueryParameter(URI_QUERYPARAM_PHONEID);
        String remoteid = data.getQueryParameter(URI_QUERYPARAM_DOORID);
        String secret = data.getQueryParameter(URI_QUERYPARAM_SECRET);
        UUID remote = b64ToUUID(remoteid);
        UUID own = b64ToUUID(ownid);
        byte[] secretDec = b64ToBytes(secret);
        return new Door(name, remote, own, secretDec);
    }
}
