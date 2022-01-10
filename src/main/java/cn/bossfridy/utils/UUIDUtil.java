package cn.bossfridy.utils;

import java.util.UUID;

public class UUIDUtil {
    private final static char[] DIGITS64 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

    /**
     * getUUID
     */
    public static UUID getUUID() {
        return UUID.randomUUID();
    }

    /**
     * getUUIDBytes
     */
    public static byte[] getUUIDBytes() {
        return toBytes(getUUID());
    }

    /**
     * getUUIDBytes
     */
    public static byte[] toBytes(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) ((msb >>> 8 * (7 - i)) & 0xFF);
            buffer[i + 8] = (byte) ((lsb >>> 8 * (7 - i)) & 0xFF);
        }
        return buffer;
    }

    /**
     * getUUIDBytes
     */
    public static String getShortString() {
        return getShortString((UUID) null);
    }

    /**
     * getShortString
     */
    public static String getShortString(UUID u) {
        if (u == null) {
            u = getUUID();
        }

        return toIDString(u.getMostSignificantBits()) + toIDString(u.getLeastSignificantBits());
    }

    /**
     * getShortString
     */
    public static String getShortString(byte[] bytes) {
        UUID u = parseUUID(bytes);
        return getShortString(u);
    }

    /**
     * toIDString
     */
    private static String toIDString(long l) {
        char[] buf = "00000000000".toCharArray(); // 限定11位长度
        int length = 11;
        long least = 63L; // 0x0000003FL 00111111
        do {
            buf[--length] = DIGITS64[(int) (l & least)]; // l & least取低6位
            l >>>= 6;
        } while (l != 0);

        return new String(buf);
    }

    /**
     * parseUUID
     */
    public static UUID parseUUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);

        return new UUID(msb, lsb);
    }
}
