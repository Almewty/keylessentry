package de.w_hs.keylessentry.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import de.w_hs.keylessentry.R;
import de.w_hs.keylessentry.data.Door;

import static de.w_hs.keylessentry.Constants.*;
import static de.w_hs.keylessentry.Helper.*;

public class ShowDoorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_door);

        Door door = getIntent().getParcelableExtra(INTENT_EXTRA_DOOR);
        if (door != null) {
            setText(R.id.door_name, door.getName());
            setText(R.id.door_ownid, door.getOwnIdentifier().toString());
            setText(R.id.door_remoteid, door.getRemoteIdentifier().toString());
            setText(R.id.door_secret, bytesToHex(door.getSharedSecret()));
        } else {
            finish();
        }
    }

    private void setText(int textId, String text) {
        ((TextView) findViewById(textId)).setText(text);
    }
}
