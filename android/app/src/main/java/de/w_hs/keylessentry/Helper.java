package de.w_hs.keylessentry;

import android.util.Base64;

import com.sun.identity.authentication.modules.hotp.HOTPAlgorithm;

import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class Helper {
    private Helper() {
    }

    public static UUID getUUIDFromBLEAdv(byte[] data) throws BufferUnderflowException {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte adLength;
        while ((adLength = bb.get()) > 0) {
            byte adType = bb.get();
            if (adType != (byte) 0xFF) {
                bb.position(bb.position() + adLength - 1);
                continue;
            }

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

    public static byte[] b64ToBytes(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public static String fixUUID(String uuid) {
        if (uuid.contains("-"))
            return uuid;
        StringBuffer sb = new StringBuffer(uuid);
        sb.insert(8, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(13, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(18, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

    public static UUID b64ToUUID(String base64) {
        byte[] bytes = b64ToBytes(base64);
        try {
            String uuidString = new String(bytes, "UTF-8");
            uuidString = fixUUID(uuidString);
            return UUID.fromString(uuidString);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return null;
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
//
//
//    public static byte[] generateOTP(byte[] sharedSecret) {
//        long time = System.currentTimeMillis();
//        time -= (time % (30 * 1000));
//        try {
//            return HOTPAlgorithm.generateHash(sharedSecret, time);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
