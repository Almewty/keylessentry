package de.w_hs.keylessentry;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import org.altbeacon.beacon.AltBeacon;

public class MyAltBeacon extends AltBeacon {
    protected BluetoothDevice bluetoothDevice;

    public MyAltBeacon() {
        super();
    }

    protected MyAltBeacon(Parcel in) {
        super(in);
        bluetoothDevice = in.readParcelable(MyAltBeacon.class.getClassLoader());
    }

    public static final Parcelable.Creator<MyAltBeacon> CREATOR
            = new Parcelable.Creator<MyAltBeacon>() {
        public MyAltBeacon createFromParcel(Parcel in) {
            return new MyAltBeacon(in);
        }
        public MyAltBeacon[] newArray(int size) {
            return new MyAltBeacon[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeParcelable(bluetoothDevice, flags);
    }

    public void setBluetoothDevice(BluetoothDevice device) {
        bluetoothDevice = device;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
}
