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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.util.UUID;

import de.w_hs.keylessentry.activities.AboutActivity;

import static de.w_hs.keylessentry.Helper.*;


public class MainActivity extends Activity {
    private static final String TAG = ".KeylessEntryApplication";
    private static final int REQUEST_ENABLE_BT = 0;

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

    private static byte[] generateOTP(byte[] sharedSecret) {
        long time = System.currentTimeMillis();
        time -= (time % (30 * 1000));
        try {
            return HOTPAlgorithm.generateHash(sharedSecret, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            //TODO: überprüfen ob es das richtige bluetooth gerät ist.
            UUID d = getUUIDFromBLEAdv(bytes);
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
                BluetoothGattService service = gatt.getService(Constants.DOOR_SERVICE);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(Constants.DOOR_CHARACTERISTIC);
                //TODO: one time code senden
                characteristic.setValue(generateOTP(characteristic.getUuid().toString().getBytes()));
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
