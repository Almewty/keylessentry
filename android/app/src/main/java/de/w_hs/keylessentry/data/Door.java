package de.w_hs.keylessentry.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.util.UUID;

public class Door implements Parcelable {
    private static final int TRUNCATION_OFFSET = 0;

    private UUID uniqueIdentifier;
    private byte[] sharedSecret;
    private String name;

    public Door(String name, UUID uniqueIdentifier, byte[] sharedSecret) {
        this.name = name;
        this.uniqueIdentifier = uniqueIdentifier;
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

    public UUID getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        UUID objID = null;
        if (object instanceof Door) {
            objID = ((Door)object).getUniqueIdentifier();
        } else if (object instanceof String) {
            try {
                objID = UUID.fromString((String) object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (object instanceof UUID) {
            objID = (UUID)object;
        } else {
            return false;
        }
        return uniqueIdentifier.equals(objID);
    }

    //region Parcel

    private Door(Parcel parcel) {
        this.name = parcel.readString();
        this.uniqueIdentifier = (UUID)parcel.readSerializable();
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
        parcel.writeSerializable(uniqueIdentifier);
        parcel.writeByteArray(sharedSecret);
    }
    //endregion
}
