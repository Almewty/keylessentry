package de.w_hs.keylessentry;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;

import de.w_hs.keylessentry.activities.AboutActivity;


public class MainActivity extends Activity implements RangeNotifier {
    private static final String TAG = ".KeylessEntryApplication";
    private static final String UNIQUE_ID = "com.w_hs.keylessentry.KeylessEntryApplicationID";
    private static final long BETWEEN_SCAN = 7 * 1000;
    private static final long SCAN_DURATION = 3 * 1000;


    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new MyAltBeaconParser());
        beaconManager.setForegroundScanPeriod(SCAN_DURATION);
        beaconManager.setBackgroundScanPeriod(SCAN_DURATION);
        beaconManager.setForegroundBetweenScanPeriod(BETWEEN_SCAN);
        beaconManager.setBackgroundBetweenScanPeriod(BETWEEN_SCAN);

        //TODO: identifier dynamisch laden
        // um folgenden identifier zu finden dieses kommando benutzen
        // sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1B 1B FF FF FF BE AC E2 0A 39 F4 73 F5 4B C4 A1 2F 17 D1 AD 07 A9 61 00 01 00 01 C8 00

        region = new Region(UNIQUE_ID, Identifier.parse("E20A39F4-73F5-4BC4-A12F-17D1AD07A961"), null, null);

        beaconManager.bind(mInternalConsumer);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //**********************************************************************************************
    public void onClickStart(View button){
        findViewById(R.id.start).setVisibility(View.GONE);
        findViewById(R.id.stop).setVisibility(View.VISIBLE);

        try {
            BeaconManager.logDebug(TAG, "Background region monitoring activated for region " + region);
            beaconManager.startRangingBeaconsInRegion(region);
            if (beaconManager.isBackgroundModeUninitialized()) {
                beaconManager.setBackgroundMode(true);
                ((TextView)findViewById(R.id.text)).setText("Status: start");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Can't set up bootstrap regions due to " + e);
            ((TextView)findViewById(R.id.text)).setText("Status: error");
        }
    }

    public void onClickStop(View button){
        findViewById(R.id.stop).setVisibility(View.GONE);
        findViewById(R.id.start).setVisibility(View.VISIBLE);

        try {
            BeaconManager.logDebug(TAG, "Background region monitoring activated for region " + region);
            beaconManager.stopRangingBeaconsInRegion(region);
            if (beaconManager.isBackgroundModeUninitialized()) {
                beaconManager.setBackgroundMode(true);
                ((TextView)findViewById(R.id.text)).setText("Status: stop");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Can't set up bootstrap regions due to " + e);
            ((TextView)findViewById(R.id.text)).setText("Status: error");
        }
    }


    //**********************************************************************************************
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon b : beacons) {
            if (b instanceof MyAltBeacon) {
                //TODO: verbinde mit raspberrypi und öffne türe
                MyAltBeacon beacon = (MyAltBeacon) b;
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                beacon.getBluetoothDevice().connectGatt(this, false, mInternalGatt);
                Log.d(TAG, "Found beacon");
                ((TextView)findViewById(R.id.text)).setText("Status: erfolgreich");
                // gebe one-time-code aus wenn passendes beacon gefunden
//                long moving = System.currentTimeMillis();
//                moving = moving - (moving % (30 * 1000));
//                try {
//                    Log.d(TAG, "Found beacon: " + HOTPAlgorithm.generateOTP(b.getId1().toHexString().getBytes(), moving, 4, false, 0));
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                } catch (InvalidKeyException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private InternalBeaconConsumer mInternalConsumer = new InternalBeaconConsumer();
    private InternalGattCallback mInternalGatt = new InternalGattCallback();

    private class InternalGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState != BluetoothProfile.STATE_CONNECTED) {
                gatt.disconnect();
                ((TextView)findViewById(R.id.text)).setText("Status: getrennt");
            } else {
                gatt.connect();
                gatt.discoverServices();
                ((TextView)findViewById(R.id.text)).setText("Status: verbunden");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString("542888d1-6a92-4d9b-9314-69882775001a"));
                if (service == null)
                    return;
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("a7a09b5d-8374-445b-89cc-42b73dd164e8"));
                if (characteristic == null)
                    return;
                long moving = System.currentTimeMillis();
                moving = moving - (moving % (30 * 1000));
                try {
                    characteristic.setValue(HOTPAlgorithm.generateOTP("E20A39F4-73F5-4BC4-A12F-17D1AD07A961".getBytes(), moving, 4, false, 0));
                    gatt.writeCharacteristic(characteristic);
                    Log.d(TAG, "Wrote characteristic");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "error on discovering services");
            }
        }
    }

    private class InternalBeaconConsumer implements BeaconConsumer {
        /**
         * Method reserved for system use
         */
        @Override
        public void onBeaconServiceConnect() {
            BeaconManager.logDebug(TAG, "Activating background region monitoring");
            beaconManager.setRangeNotifier(MainActivity.this);

        }

        /**
         * Method reserved for system use
         */
        @Override
        public boolean bindService(Intent intent, ServiceConnection conn, int arg2) {
            return MainActivity.this.getApplicationContext().bindService(intent, conn, arg2);
        }

        /**
         * Method reserved for system use
         */
        @Override
        public Context getApplicationContext() {
            return MainActivity.this.getApplicationContext();
        }

        /**
         * Method reserved for system use
         */
        @Override
        public void unbindService(ServiceConnection conn) {
            MainActivity.this.getApplicationContext().unbindService(conn);
        }
    }
}
