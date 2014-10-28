package de.w_hs.keylessentry;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;

import org.altbeacon.beacon.AltBeaconParser;
import org.altbeacon.beacon.Beacon;

import java.lang.annotation.Target;

public class MyAltBeaconParser extends AltBeaconParser {
    public static final String TAG = "MyAltBeaconParser";

    @Override
    public Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
        MyAltBeacon beacon = new MyAltBeacon();
        beacon.setBluetoothDevice(device);
        return fromScanData(scanData, rssi, device, beacon);
    }
}
