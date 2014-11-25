package de.w_hs.keylessentry;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import de.w_hs.keylessentry.activities.AboutActivity;
import de.w_hs.keylessentry.data.Door;


public class MainActivity extends Activity {
    private static final String TAG = ".KeylessEntryApplication";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int TRUNCATION_OFFSET = 0;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void toggleButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View startButton = findViewById(R.id.start);
                View stopButton = findViewById(R.id.stop);

                startButton.setVisibility(startButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                stopButton.setVisibility(stopButton.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                onClickStart(null);
            }
        }
    }

    public void onClickStart(View button) {
        toggleButtons();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            toggleButtons();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        mBluetoothAdapter.startLeScan(mScanCallback);
    }

    public void onClickStop(View button) {
        toggleButtons();

        mBluetoothAdapter.stopLeScan(mScanCallback);
    }

    private static int generateOTP(byte[] sharedSecret) {
        long time = System.currentTimeMillis();
        time -= (time % (30 * 1000));
        try {
            return HOTPAlgorithm.generateBinaryOTP(sharedSecret, time, TRUNCATION_OFFSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            //TODO: überprüfen ob es das richtige bluetooth gerät ist.
            Door d = Door.getDoorFromBLEAdv(bytes);
            mBluetoothAdapter.stopLeScan(mScanCallback);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bluetoothDevice.connectGatt(MainActivity.this, true, mGattCallback);
                }
            });
        }
    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else {
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString("542888d1-6a92-4d9b-9314-69882775001a"));
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("a7a09b5d-8374-445b-89cc-42b73dd164e8"));
                //TODO: one time code senden
                characteristic.setValue(generateOTP(characteristic.getUuid().toString().getBytes()), BluetoothGattCharacteristic.FORMAT_SINT32, 0);
                gatt.writeCharacteristic(characteristic);
            } else {
                Log.w(TAG, "service error: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            gatt.disconnect();
            gatt.close();
            toggleButtons();
        }
    };
}
