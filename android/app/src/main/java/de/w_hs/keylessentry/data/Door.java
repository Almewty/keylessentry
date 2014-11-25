package de.w_hs.keylessentry.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import org.apache.http.util.ByteArrayBuffer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Door implements Parcelable {
    private static final int TRUNCATION_OFFSET = 0;
    private static final byte[] COMPANY_IDENTIFIER = {(byte) 0xFF, (byte) 0xFF};
    private static final byte[] DOOR_IDENTIFIER = {(byte) 0xD0, (byte) 0x02};

    private UUID remoteIdentifier;
    private UUID ownIdentifier;
    private byte[] sharedSecret;
    private String name;

    public Door(String name, UUID remoteIdentifier, UUID ownIdentifier, byte[] sharedSecret) {
        this.name = name;
        this.remoteIdentifier = remoteIdentifier;
        this.ownIdentifier = ownIdentifier;
        this.sharedSecret = sharedSecret;
    }

    public int generateOTP() {
        long time = System.currentTimeMillis();
        time -= (time % (30 * 1000));
        try {
            return HOTPAlgorithm.generateBinaryOTP(sharedSecret, time, TRUNCATION_OFFSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public UUID getRemoteIdentifier() {
        return remoteIdentifier;
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Door) {
            Door d = (Door) object;
            return remoteIdentifier.equals(d.getRemoteIdentifier()) && ownIdentifier.equals(d.getOwnIdentifier());
        }
        return false;
    }

    public static Door getDoorFromBLEAdv(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        
        return null;
    }

    //region Parcel

    private Door(Parcel parcel) {
        this.name = parcel.readString();
        this.remoteIdentifier = (UUID) parcel.readSerializable();
        parcel.readByteArray(this.sharedSecret);
    }

    public static final Creator<Door> CREATOR = new Creator<Door>() {
        @Override
        public Door createFromParcel(Parcel parcel) {
            return new Door(parcel);
        }

        @Override
        public Door[] newArray(int i) {
            return new Door[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeSerializable(remoteIdentifier);
        parcel.writeByteArray(sharedSecret);
    }

    public UUID getOwnIdentifier() {
        return ownIdentifier;
    }
    //endregion
}
