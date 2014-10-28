package de.w_hs.keylessentry;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class KeylessEntryApplication extends Application implements RangeNotifier {
    private static final String TAG = ".KeylessEntryApplication";
    private static final String UNIQUE_ID = "com.w_hs.keylessentry.KeylessEntryApplicationID";
    private static final long BETWEEN_SCAN = 7 * 1000;
    private static final long SCAN_DURATION = 3 * 1000;


    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private ArrayList<Region> regions = new ArrayList<Region>();

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new MyAltBeaconParser());
        beaconManager.setForegroundScanPeriod(SCAN_DURATION);
        beaconManager.setBackgroundScanPeriod(SCAN_DURATION);
        beaconManager.setForegroundBetweenScanPeriod(BETWEEN_SCAN);
        beaconManager.setBackgroundBetweenScanPeriod(BETWEEN_SCAN);

        //TODO: identifier dynamisch laden
        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        // um folgenden identifier zu finden dieses kommando benutzen
        // sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1B 1B FF FF FF BE AC E2 0A 39 F4 73 F5 4B C4 A1 2F 17 D1 AD 07 A9 61 00 01 00 01 C8 00
        identifiers.add(Identifier.parse("E20A39F4-73F5-4BC4-A12F-17D1AD07A961"));

        for (int i = 0; i < identifiers.size(); i++) {
            regions.add(new Region(UNIQUE_ID + i, identifiers.get(i), null, null));
        }
        beaconManager.bind(new InternalBeaconConsumer());
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon b : beacons) {
            if (b instanceof MyAltBeacon) {
                //TODO: verbinde mit raspberrypi und öffne türe
                MyAltBeacon beacon = (MyAltBeacon)b;
                Toast.makeText(this, "Gesuchtes beacon gefunden", Toast.LENGTH_SHORT).show();
                Looper.loop(); // ohne das funktioniert toast nicht (keine ahnung was es bringt)
                Log.d(TAG, "Found beacon: " + b.getId1().toHexString());
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
            beaconManager.setRangeNotifier(KeylessEntryApplication.this);
            try {
                for (Region region : regions) {
                    BeaconManager.logDebug(TAG, "Background region monitoring activated for region " + region);
                    beaconManager.startRangingBeaconsInRegion(region);
                    if (beaconManager.isBackgroundModeUninitialized()) {
                        beaconManager.setBackgroundMode(true);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Can't set up bootstrap regions due to " + e);
            }
        }

        /**
         * Method reserved for system use
         */
        @Override
        public boolean bindService(Intent intent, ServiceConnection conn, int arg2) {
            return KeylessEntryApplication.this.getApplicationContext().bindService(intent, conn, arg2);
        }

        /**
         * Method reserved for system use
         */
        @Override
        public Context getApplicationContext() {
            return KeylessEntryApplication.this.getApplicationContext();
        }

        /**
         * Method reserved for system use
         */
        @Override
        public void unbindService(ServiceConnection conn) {
            KeylessEntryApplication.this.getApplicationContext().unbindService(conn);
        }
    }
}
