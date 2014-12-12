package de.w_hs.keylessentry.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import static de.w_hs.keylessentry.Helper.*;

public class Door implements Parcelable, Comparable<Door> {

    private UUID remoteIdentifier;
    private UUID ownIdentifier;
    private byte[] sharedSecret;
    private String name;

    public Door(String name, UUID remoteIdentifier, UUID ownIdentifier, byte[] sharedSecret) {
        if (name == null || remoteIdentifier == null || ownIdentifier == null || sharedSecret == null || sharedSecret.length == 0)
            throw new IllegalArgumentException("No argument may be null and secret must be longer than 0");
        this.name = name;
        this.remoteIdentifier = remoteIdentifier;
        this.ownIdentifier = ownIdentifier;
        this.sharedSecret = sharedSecret;
    }

    public byte[] getCharacteristicData() {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.put(UUIDToBytes(ownIdentifier));
        bb.putInt(generateOTPBinary());
        return bb.array();
    }

    public int generateOTPBinary() {
        long time = System.currentTimeMillis();
        long steps = time / 30000;
        try {
            return HOTPAlgorithm.generateHash(sharedSecret, steps, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
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
            return remoteIdentifier.equals(d.getRemoteIdentifier()) && ownIdentifier.equals(d.getOwnIdentifier()) && Arrays.equals(sharedSecret, d.getSharedSecret());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) remoteIdentifier.getMostSignificantBits();
    }

    @Override
    public String toString() {
        return name;
    }

    public UUID getOwnIdentifier() {
        return ownIdentifier;
    }

    @Override
    public int compareTo(Door another) {
        if (name == null && another.getName() == null)
            return 0;
        if (name == null)
            return 1;
        if (another.getName() == null)
            return -1;
        return name.compareTo(another.getName());
    }

    //region Parcel

    private Door(Parcel parcel) {
        this.name = parcel.readString();
        this.remoteIdentifier = (UUID) parcel.readSerializable();
        this.ownIdentifier = (UUID) parcel.readSerializable();
        int blength = parcel.readInt();
        this.sharedSecret = new byte[blength];
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
        parcel.writeSerializable(ownIdentifier);
        parcel.writeInt(sharedSecret.length);
        parcel.writeByteArray(sharedSecret);
    }
    //endregion
}
