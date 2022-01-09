package cn.bossfridy.rpc.utils;

import io.netty.buffer.ByteBuf;

import java.io.UTFDataFormatException;

/**
 * Netty ByteBuf读写不支持类似dataOutputStream.writeUTF()和dataInputStream.readUTF()的方法，因此使用该工具类
 */
public class ByteBufUtil {
    private static final String CHARSET = "UTF-8";
    private static final int MAX_VALUE_UNSIGNED_INT8 = 255;
    private static final int MAX_VALUE_UNSIGNED_INT16 = 65535;
    private static final int MAX_VALUE_UNSIGNED_INT24 = 16777215;

    /**
     * writeString
     */
    public static int writeString(String str, ByteBuf out) throws Exception {
        if (str == null)
            throw new Exception("input string is null");

        byte[] bytes = str.getBytes(CHARSET);
        int length = bytes.length;

        if (length > MAX_VALUE_UNSIGNED_INT8)
            throw new Exception("encoded string too long: " + length + " bytes");

        out.writeByte((byte) length);
        if (length > 0) {
            out.writeBytes(bytes);
        }

        return length + 1;
    }

    /**
     * writeUTF
     */
    public static int writeUTF(String str, ByteBuf out) throws Exception {
        if (str == null)
            throw new Exception("input string is null");

        byte[] bytes = str.getBytes(CHARSET);
        int length = bytes.length;

        if (length > MAX_VALUE_UNSIGNED_INT16)
            throw new Exception("encoded string too long: " + length + " bytes");

        out.writeBytes(unsignedInt16ToBytes(length));
        if (length > 0) {
            out.writeBytes(bytes);
        }

        return length + 2;
    }

    /**
     * writeData
     */
    public static int writeData(byte[] data, ByteBuf out) throws Exception {
        int dataLen = (data == null || data.length == 0) ? 0 : data.length;

        if (dataLen > MAX_VALUE_UNSIGNED_INT24)
            throw new Exception("encoded data too long: " + dataLen + " bytes");

        out.writeBytes(unsignedInt24ToBytes(dataLen));
        if (dataLen > 0)
            out.writeBytes(data);

        return dataLen + 3;
    }

    /**
     * readString
     */
    public static String readString(ByteBuf in) throws Exception {
        int length = Byte.toUnsignedInt(in.readByte());
        if (length > 0) {
            byte[] bytes = new byte[length];
            in.readBytes(bytes);

            return new String(bytes, CHARSET);
        }

        return "";
    }

    /**
     * readUTF
     */
    public static String readUTF(ByteBuf in) throws Exception {
        byte[] lenBytes = new byte[2];
        in.readBytes(lenBytes);
        int length = bytesToUnsignedInt16(lenBytes);
        if (length > 0) {
            byte[] bytes = new byte[length];
            in.readBytes(bytes);

            return new String(bytes, CHARSET);
        }

        return "";
    }

    /**
     * readData
     */
    public static byte[] readData(ByteBuf in) throws Exception {
        byte[] lenBytes = new byte[3];
        in.readBytes(lenBytes);
        int length = bytesToUnsignedInt24(lenBytes);
        if (length > 0) {
            byte[] bytes = new byte[length];
            in.readBytes(bytes);

            return bytes;
        }

        return null;
    }

    /**
     * bytesToUnsignedInt16
     */
    public static int bytesToUnsignedInt16(byte[] bytes) throws Exception {
        if (bytes.length != 2)
            throw new Exception("bytes.length must be 2.");

        return bytes[0] << 8 & 0xFF00 | bytes[1] & 0xFF;
    }

    /**
     * unsignedInt16ToBytes
     */
    public static byte[] unsignedInt16ToBytes(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) ((value & 0xFF00) >> 8);
        bytes[1] = (byte) (value & 0xFF);

        return bytes;
    }

    /**
     * bytesToUnsignedInt24
     */
    public static int bytesToUnsignedInt24(byte[] bytes) throws Exception {
        if (bytes.length != 3)
            throw new Exception("bytes.length must be 3.");

        return (bytes[2] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[0] & 0x0F) << 16);
    }

    /**
     * unsignedInt24ToBytes
     */
    public static byte[] unsignedInt24ToBytes(int value) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);

        return bytes;
    }

    public static void main(String[] args) throws Exception {
        int value = 16777216;
        byte[] bytes = unsignedInt24ToBytes(value);
        int result = bytesToUnsignedInt24(bytes);
        System.out.println(result);
    }
}
