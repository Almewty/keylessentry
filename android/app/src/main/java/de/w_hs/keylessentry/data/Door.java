package de.w_hs.keylessentry.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.util.Arrays;
import java.util.UUID;

import static de.w_hs.keylessentry.Helper.*;

public class Door implements Parcelable {

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

    public byte[] generateOTPHash() {
        long time = System.currentTimeMillis();
        time -= (time % (30 * 1000));
        try {
            return HOTPAlgorithm.generateHash(sharedSecret, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        return (int)remoteIdentifier.getMostSignificantBits();
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
