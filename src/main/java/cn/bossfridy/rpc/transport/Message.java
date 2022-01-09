package cn.bossfridy.rpc.transport;

import cn.bossfridy.rpc.utils.ByteUtil;
import cn.bossfridy.rpc.utils.UUIDUtil;
import cn.bossfridy.rpc.utils.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class Message {
    /**
     * sessionId（可启XI的作用，16字节）
     **/
    private byte[] session;

    /**
     * 目标方法（最大长度：MAX_VALUE_UNSIGNED_INT8）
     **/
    private String targetMethod;

    /**
     * 源方法（最大长度：MAX_VALUE_UNSIGNED_INT8）
     **/
    private String sourceMethod;

    /**
     * 源IP（4字节）
     **/
    private String sourceHost;

    /**
     * 目标IP（4字节）
     **/
    private String targetHost;

    /**
     * 源端口（4字节）
     **/
    private int sourcePort;

    /**
     * 目标端口（4字节）
     **/
    private int targetPort;

    /**
     * 消息产生时间（8字节）
     **/
    private long timestamp;

    /**
     * 版本（为后续扩展留后手）
     **/
    private byte version;

    /**
     * 消息体（protostuff序列化，最大长度：MAX_VALUE_UNSIGNED_INT24）
     **/
    private byte[] data;

    public Message() {
        this.version = (byte) 1;
    }

    /**
     * getSessionShortString
     */
    public String getSessionShortString() {
        if (this.session == null || this.session.length == 0)
            throw new RuntimeException("Message.session is null or empty!");

        return UUIDUtil.getShortString(this.session);
    }

    /**
     * hasSource
     */
    public boolean hasSource() {
        return StringUtils.isNotEmpty(sourceHost) && sourcePort > 0 && !"0.0.0.0".equals(sourceHost);
    }

    /**
     * buildTimestamp
     */
    public void buildTimestamp() {
        if (this.timestamp > 0) {
            return;
        }

        this.timestamp = System.currentTimeMillis();
    }

    /**
     * encode
     */
    public static void encode(Message msg, ByteBuf out) throws Exception {
        out.writeBytes(msg.getSession());
        ByteBufUtil.writeString(msg.getTargetMethod(), out);
        ByteBufUtil.writeString(msg.getSourceMethod(), out);
        out.writeBytes(ByteUtil.ipToBytes(msg.getSourceHost()));
        out.writeBytes(ByteUtil.ipToBytes(msg.getTargetHost()));
        out.writeInt(msg.getSourcePort());
        out.writeInt(msg.getTargetPort());
        out.writeLong(msg.getTimestamp());
        out.writeByte(msg.getVersion());
        ByteBufUtil.writeData(msg.getData(), out);
    }

    /**
     * decode
     */
    public static Message decode(ByteBuf in) throws Exception {
        Message msg = new Message();

        byte[] sessionBytes = new byte[16];
        in.readBytes(sessionBytes);
        msg.setSession(sessionBytes);

        msg.setTargetMethod(ByteBufUtil.readString(in));
        msg.setSourceMethod(ByteBufUtil.readString(in));

        byte[] sourceHostBytes = new byte[4];
        in.readBytes(sourceHostBytes);
        msg.setSourceHost(ByteUtil.bytesToIp(sourceHostBytes));

        byte[] targetHostBytes = new byte[4];
        in.readBytes(targetHostBytes);
        msg.setTargetHost(ByteUtil.bytesToIp(targetHostBytes));

        msg.setSourcePort(in.readInt());
        msg.setTargetPort(in.readInt());
        msg.setTimestamp(in.readLong());
        msg.setVersion(in.readByte());

        msg.setData(ByteBufUtil.readData(in));

        return msg;
    }
}
