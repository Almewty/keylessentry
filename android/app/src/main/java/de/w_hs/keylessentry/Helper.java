package de.w_hs.keylessentry;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class Helper {
    private Helper() {
    }

    public static UUID getUUIDFromBLEAdv(byte[] data) throws BufferUnderflowException {
        ByteBuffer bb = ByteBuffer.wrap(data);
        int adLength;
        while ((adLength = bb.get()) > 0) {
            int adType = bb.get();
            if (adType != 0xFF)
                continue;

            ByteBuffer buffer = ByteBuffer.wrap(data, bb.position(), adLength - 1);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (buffer.getShort() != Constants.COMPANY_IDENTIFIER)
                break;
            buffer.order(ByteOrder.BIG_ENDIAN);
            if (buffer.getShort() != Constants.DOOR_ID)
                break;
            return new UUID(buffer.getLong(), buffer.getLong());
        }

        return null;
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // http://stackoverflow.com/questions/18714616/convert-hex-string-to-byte
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    public static UUID bytesToUUID(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static byte[] UUIDToBytes(UUID uuid) {
        return ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
    }


    public static byte[] generateOTP(byte[] sharedSecret) {
        long time = System.currentTimeMillis();
        time -= (time % (30 * 1000));
        try {
            return HOTPAlgorithm.generateHash(sharedSecret, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
